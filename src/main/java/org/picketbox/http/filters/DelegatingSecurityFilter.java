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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authentication.AuthenticationManager;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.authentication.manager.DatabaseAuthenticationManager;
import org.picketbox.core.authentication.manager.LDAPAuthenticationManager;
import org.picketbox.core.authentication.manager.PropertiesFileBasedAuthenticationManager;
import org.picketbox.core.authentication.manager.SimpleCredentialAuthenticationManager;
import org.picketbox.core.authorization.AuthorizationManager;
import org.picketbox.core.authorization.impl.SimpleAuthorizationManager;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.http.PicketBoxHTTPManager;
import org.picketbox.http.PicketBoxHTTPMessages;
import org.picketbox.http.authentication.HTTPAuthenticationScheme;
import org.picketbox.http.authentication.HTTPBasicAuthentication;
import org.picketbox.http.authentication.HTTPClientCertAuthentication;
import org.picketbox.http.authentication.HTTPDigestAuthentication;
import org.picketbox.http.authentication.HTTPFormAuthentication;
import org.picketbox.http.authorization.resource.WebResource;
import org.picketbox.http.config.ConfigurationBuilderProvider;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;

/**
 * A {@link Filter} that delegates to the PicketBox Security Infrastructure
 *
 * @author anil saldhana
 * @since Jul 10, 2012
 */
public class DelegatingSecurityFilter implements Filter {
    private PicketBoxHTTPManager securityManager;

    private FilterConfig filterConfig;

    private HTTPAuthenticationScheme authenticationScheme;

    @Override
    public void init(FilterConfig fc) throws ServletException {
        this.filterConfig = fc;

        ServletContext sc = filterConfig.getServletContext();

        Map<String, Object> contextData = new HashMap<String, Object>();
        contextData.put(PicketBoxConstants.SERVLET_CONTEXT, sc);

        // Let us try the servlet context
        String authValue = sc.getInitParameter(PicketBoxConstants.AUTHENTICATION_KEY);
        AuthorizationManager authorizationManager = null;
        AuthenticationManager am = null;
        HTTPConfigurationBuilder configuration = null;

        String configurationProvider = sc.getInitParameter(PicketBoxConstants.HTTP_CONFIGURATION_PROVIDER);
        String userAttributeName = sc.getInitParameter(PicketBoxConstants.USER_ATTRIBUTE_NAME);

        if (authValue != null && !authValue.isEmpty()) {
            if (configurationProvider == null) {
                // Look for auth mgr also
                String authMgrStr = sc.getInitParameter(PicketBoxConstants.AUTH_MGR);
                // Look for auth mgr also
                String authzMgrStr = sc.getInitParameter(PicketBoxConstants.AUTHZ_MGR);

                if (authzMgrStr != null) {
                    authorizationManager = getAuthzMgr(authzMgrStr);
                    authorizationManager.start();
                    contextData.put(PicketBoxConstants.AUTHZ_MGR, authorizationManager);
                }

                am = getAuthMgr(authMgrStr);

                contextData.put(PicketBoxConstants.AUTH_MGR, am);
            }

            authenticationScheme = getAuthenticationScheme(authValue, contextData);
        } else {
            String loader = filterConfig.getInitParameter(PicketBoxConstants.AUTH_SCHEME_LOADER);

            if (loader == null) {
                throw PicketBoxHTTPMessages.MESSAGES.missingRequiredInitParameter(PicketBoxConstants.AUTH_SCHEME_LOADER);
            }

            if (configurationProvider == null) {
                String authManagerStr = filterConfig.getInitParameter(PicketBoxConstants.AUTH_MGR);
                if (authManagerStr != null && !authManagerStr.isEmpty()) {
                    am = getAuthMgr(authManagerStr);
                    contextData.put(PicketBoxConstants.AUTH_MGR, am);
                }
                String authzManagerStr = filterConfig.getInitParameter(PicketBoxConstants.AUTHZ_MGR);
                if (authzManagerStr != null && authzManagerStr.isEmpty()) {
                    authorizationManager = getAuthzMgr(authzManagerStr);
                    authorizationManager.start();
                    contextData.put(PicketBoxConstants.AUTHZ_MGR, authorizationManager);
                }
            }

            authenticationScheme = (HTTPAuthenticationScheme) SecurityActions.instance(getClass(), loader);
        }

        if (configurationProvider != null) {
            configuration = ((ConfigurationBuilderProvider) SecurityActions.instance(getClass(), configurationProvider)).getBuilder(sc);
        } else {
            configuration = new HTTPConfigurationBuilder();

            configuration.authentication().authManager(am);
            configuration.authorization().manager(authorizationManager);
        }

        configuration.sessionManager().userAttributeName(userAttributeName);

        this.securityManager = new PicketBoxHTTPManager((PicketBoxHTTPConfiguration) configuration.build());

        this.securityManager.start();

        authenticationScheme.setPicketBoxManager(this.securityManager);

        sc.setAttribute(PicketBoxConstants.PICKETBOX_MANAGER, this.securityManager);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        logout(httpRequest, httpResponse);

        authenticate(httpRequest, httpResponse);

        authorize(httpRequest, httpResponse);

        if (!response.isCommitted()) {
            chain.doFilter(httpRequest, response);
        }
    }

    private void authorize(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        if (httpResponse.isCommitted()) {
            return;
        }

        boolean authorize = this.securityManager.authorize(getAuthenticatedUser(httpRequest, httpResponse),
                createWebResource(httpRequest, httpResponse));

        if (!authorize) {
            if (!httpResponse.isCommitted()) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }

    private WebResource createWebResource(HttpServletRequest request, HttpServletResponse response) {
        WebResource resource = new WebResource();

        resource.setContext(request.getServletContext());
        resource.setRequest(request);
        resource.setResponse(response);

        return resource;
    }

    public PicketBoxSubject getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
        return this.securityManager.getSubject(request);
    }

    private void authenticate(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
        if (httpResponse.isCommitted()) {
            return;
        }

        try {
            this.authenticationScheme.authenticate(httpRequest, httpResponse);
        } catch (AuthenticationException e) {
            throw new ServletException(e);
        }
    }

    private void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
        if (isLogoutRequest(httpRequest)) {
            this.securityManager.logout(getAuthenticatedUser(httpRequest, httpResponse));
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
        this.filterConfig = null;
        if (this.securityManager != null) {
            this.securityManager.stop();
        }
    }

    private HTTPAuthenticationScheme getAuthenticationScheme(String value, Map<String, Object> contextData)
            throws ServletException {
        if (value.equalsIgnoreCase(PicketBoxConstants.BASIC)) {
            return new HTTPBasicAuthentication();
        }
        if (value.equalsIgnoreCase(PicketBoxConstants.DIGEST)) {
            return new HTTPDigestAuthentication();
        }
        if (value.equalsIgnoreCase(PicketBoxConstants.CLIENT_CERT)) {
            return new HTTPClientCertAuthentication();
        }

        return new HTTPFormAuthentication();
    }

    private AuthenticationManager getAuthMgr(String value) {
        if (value.equalsIgnoreCase("Credential")) {
            return new SimpleCredentialAuthenticationManager();
        } else if (value.equalsIgnoreCase("Properties")) {
            return new PropertiesFileBasedAuthenticationManager();
        } else if (value.equalsIgnoreCase("Database")) {
            return new DatabaseAuthenticationManager();
        } else if (value.equalsIgnoreCase("Ldap")) {
            return new LDAPAuthenticationManager();
        }
        if (value == null || value.isEmpty()) {
            return new PropertiesFileBasedAuthenticationManager();
        }

        return (AuthenticationManager) SecurityActions.instance(getClass(), value);
    }

    private AuthorizationManager getAuthzMgr(String value) {
        if (value.equalsIgnoreCase("Drools")) {
            return (AuthorizationManager) SecurityActions.instance(getClass(),
                    "org.picketbox.drools.authorization.PicketBoxDroolsAuthorizationManager");
        } else if (value.equalsIgnoreCase("Simple")) {
            return new SimpleAuthorizationManager();
        }

        return (AuthorizationManager) SecurityActions.instance(getClass(),
                "org.picketbox.drools.authorization.PicketBoxDroolsAuthorizationManager");
    }
}