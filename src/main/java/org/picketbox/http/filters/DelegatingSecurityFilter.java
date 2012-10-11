/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketbox.http.filters;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.UserContext;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.authorization.AuthorizationManager;
import org.picketbox.core.authorization.impl.SimpleAuthorizationManager;
import org.picketbox.core.config.ConfigurationBuilder;
import org.picketbox.core.ctx.PicketBoxSecurityContext;
import org.picketbox.core.ctx.SecurityContext;
import org.picketbox.core.ctx.SecurityContextPropagation;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.core.exceptions.ProcessingException;
import org.picketbox.http.HTTPUserContext;
import org.picketbox.http.PicketBoxHTTPManager;
import org.picketbox.http.authentication.HTTPBasicCredential;
import org.picketbox.http.authentication.HTTPClientCertCredential;
import org.picketbox.http.authentication.HTTPDigestCredential;
import org.picketbox.http.authentication.HTTPFormCredential;
import org.picketbox.http.authentication.HttpServletCredential;
import org.picketbox.http.authorization.resource.WebResource;
import org.picketbox.http.config.ConfigurationBuilderProvider;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;
import org.picketbox.http.wrappers.RequestWrapper;
import org.picketbox.http.wrappers.ResponseWrapper;

/**
 * <p>
 * A {@link Filter} that delegates to the PicketBox Security Infrastructure.
 * </p>
 *
 * <p>
 * This filter can be configured in two ways: using PicketBox default configuration or providing a
 * {@link ConfigurationBuilderProvider}.
 * </p>
 *
 * @author anil saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 * @since Jul 10, 2012
 */
public class DelegatingSecurityFilter implements Filter {

    private PicketBoxHTTPManager securityManager;
    private Class<? extends HttpServletCredential> credentialType;

    @Override
    public void init(FilterConfig fc) throws ServletException {
        // no need to configure a PicketBoxManager. A valid instance was used to create this filter.
        if (this.securityManager != null) {
            return;
        }

        // configures the credential to be used during authentication
        this.credentialType = getSupporttedCredential(fc.getServletContext());

        // gets the configuration that will be used to configure and start the manager
        HTTPConfigurationBuilder configuration = getConfigurationBuilder(fc.getServletContext());

        // create and start the manager
        this.securityManager = new PicketBoxHTTPManager((PicketBoxHTTPConfiguration) configuration.build());
        this.securityManager.start();

        // sets the manager as a context attribute and make it available for the application
        fc.getServletContext().setAttribute(PicketBoxConstants.PICKETBOX_MANAGER, this.securityManager);
    }

    /**
     * <p>
     * Returns a {@link ConfigurationBuilder} with the necessary configuration to build a {@link PicketBoxManager} instance. If
     * a specific {@link ConfigurationBuilderProvider} was specified by the
     * <code>PicketBoxConstants.HTTP_CONFIGURATION_PROVIDER</code> context parameter it will be used. Othwewise a default
     * {@link HTTPConfigurationBuilder} will be created.
     * </p>
     *
     * @param servletContext
     * @return
     */
    private HTTPConfigurationBuilder getConfigurationBuilder(ServletContext servletContext) {
        String authzValue = servletContext.getInitParameter(PicketBoxConstants.AUTHZ_MGR);
        String configurationProvider = servletContext.getInitParameter(PicketBoxConstants.HTTP_CONFIGURATION_PROVIDER);
        String userAttributeName = servletContext.getInitParameter(PicketBoxConstants.USER_ATTRIBUTE_NAME);

        HTTPConfigurationBuilder configuration = null;

        // a ConfigurationBuilderProvider was provided, let's build the configuration using it.
        if (configurationProvider != null) {
            configuration = ((ConfigurationBuilderProvider) SecurityActions.instance(getClass(), configurationProvider))
                    .getBuilder(servletContext);
        } else {
            configuration = new HTTPConfigurationBuilder();
        }

        configuration.authorization().manager(getAuthorizationManager(authzValue));
        configuration.sessionManager().userAttributeName(userAttributeName);
        return configuration;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // wraps the request and response with PicketBox wrapper classes
        RequestWrapper wrappedRequest = new RequestWrapper(httpRequest, this.securityManager);
        ResponseWrapper wrappedResponse = new ResponseWrapper(httpResponse, this.securityManager);

        try {
            propagateSecurityContext(wrappedRequest);

            logout(wrappedRequest, wrappedResponse);

            authenticate(wrappedRequest, wrappedResponse);

            authorize(wrappedRequest, wrappedResponse);

            if (!response.isCommitted()) {
                chain.doFilter(wrappedRequest, wrappedResponse);
            }
        } finally {
            clearPropagatedSecurityContext();
        }

    }

    /**
     * <p>
     * Clear the propagated {@link SecurityContext}.
     * </p
     *
     * @throws ServletException
     */
    private void clearPropagatedSecurityContext() throws ServletException {
        try {
            SecurityContextPropagation.clear();
        } catch (ProcessingException e) {
            throw new ServletException(e);
        }
    }

    /**
     * <p>
     * Propagates the authenticated {@link UserContext}.
     * </p>
     *
     * @param httpRequest
     * @throws ServletException
     */
    private void propagateSecurityContext(HttpServletRequest httpRequest) throws ServletException {
        UserContext subject = this.securityManager.getUserContext(httpRequest);

        if (subject != null) {
            try {
                SecurityContextPropagation.setContext(new PicketBoxSecurityContext(subject));
            } catch (ProcessingException e) {
                throw new ServletException(e);
            }
        }
    }

    private void authorize(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        if (httpResponse.isCommitted()) {
            return;
        }

        boolean authorize = this.securityManager.authorize(getAuthenticatedUser(httpRequest),
                createWebResource(httpRequest, httpResponse));

        if (!authorize && !httpResponse.isCommitted()) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private WebResource createWebResource(HttpServletRequest request, HttpServletResponse response) {
        WebResource resource = new WebResource();

        resource.setContext(request.getServletContext());
        resource.setRequest(request);
        resource.setResponse(response);

        return resource;
    }

    public UserContext getAuthenticatedUser(HttpServletRequest request) {
        return this.securityManager.getUserContext(request);
    }

    private void authenticate(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
        if (httpResponse.isCommitted()) {
            return;
        }

        // user is already authenticated
        if (this.securityManager.getUserContext(httpRequest) != null
                && this.securityManager.getUserContext(httpRequest).isAuthenticated()) {
            return;
        }

        try {
            HttpServletCredential credential = this.credentialType.getConstructor(
                    new Class[] { HttpServletRequest.class, HttpServletResponse.class }).newInstance(
                    new Object[] { httpRequest, httpResponse });

            this.securityManager.authenticate(new HTTPUserContext(httpRequest, httpResponse, credential));
        } catch (AuthenticationException e) {
            throw new ServletException(e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
        if (isLogoutRequest(httpRequest)) {
            this.securityManager.logout(getAuthenticatedUser(httpRequest));
            try {
                httpResponse.sendRedirect(httpRequest.getContextPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>
     * Checks if the request is asking for a logout.
     * </p>
     *
     * @param request
     * @return
     */
    private boolean isLogoutRequest(HttpServletRequest request) {
        return request.getRequestURI().contains(PicketBoxConstants.LOGOUT_URI);
    }

    @Override
    public void destroy() {
        if (this.securityManager != null) {
            this.securityManager.stop();
        }
    }

    /**
     * <p>
     * Returns a {@link HttpServletCredential} class that should be used to create user credentials when authenticating users.
     * The specific class is returned according with the <code>PicketBoxConstants.AUTHENTICATION_KEY</code> context parameter.
     * Possible values are BASIC, DIGEST AND CLIENT_CERT. If none of them is provided the default credential will be of type
     * {@link HTTPFormCredential}.
     * </p>
     *
     * @param servletContext
     * @return
     * @throws ServletException
     */
    private Class<? extends HttpServletCredential> getSupporttedCredential(ServletContext servletContext)
            throws ServletException {
        String authenticationType = servletContext.getInitParameter(PicketBoxConstants.AUTHENTICATION_KEY);

        if (authenticationType != null) {
            if (authenticationType.equalsIgnoreCase(PicketBoxConstants.BASIC)) {
                this.credentialType = HTTPBasicCredential.class;
            } else if (authenticationType.equalsIgnoreCase(PicketBoxConstants.DIGEST)) {
                this.credentialType = HTTPDigestCredential.class;
            } else if (authenticationType.equalsIgnoreCase(PicketBoxConstants.CLIENT_CERT)) {
                this.credentialType = HTTPClientCertCredential.class;
            }
        }

        if (this.credentialType == null) {
            this.credentialType = HTTPFormCredential.class;
        }

        return this.credentialType;
    }

    /**
     * <p>
     * Returns a {@link AuthorizationManager} instance given the specified value. Possible values are drools and simple.
     * </p>
     *
     * @param value
     * @return
     */
    private AuthorizationManager getAuthorizationManager(String value) {
        if (value != null) {
            if (value.equalsIgnoreCase("drools")) {
                return (AuthorizationManager) SecurityActions.instance(getClass(),
                        "org.picketbox.drools.authorization.PicketBoxDroolsAuthorizationManager");
            } else if (value.equalsIgnoreCase("simple")) {
                return new SimpleAuthorizationManager();
            }
        }
        return null;
    }
}