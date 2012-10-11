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
package org.picketbox.http.authentication;

import static org.picketbox.core.PicketBoxMessages.MESSAGES;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.Credential;
import org.picketbox.core.PicketBoxMessages;
import org.picketbox.core.UserContext;
import org.picketbox.core.authentication.AuthenticationResult;
import org.picketbox.core.authentication.AuthenticationStatus;
import org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.http.PicketBoxHTTPManager;
import org.picketbox.http.config.HTTPAuthenticationConfiguration;

/**
 * Base class for all the HTTP authentication schemes
 *
 * @author anil saldhana
 * @since Jul 6, 2012
 */
public abstract class AbstractHTTPAuthentication extends AbstractAuthenticationMechanism {

    private RequestCache requestCache = new RequestCache();

    /**
     * Injectable realm name
     */
    protected String realmName = "PicketBox Realm";

    private static final String DEFAULT_PAGE_URL = "/";

    /**
     * The page used to redirect the user after a succesful authentication.
     */
    protected String defaultPage = DEFAULT_PAGE_URL;

    /**
     * The FORM login page. It should always start with a '/'
     */
    protected String formAuthPage = "/login.jsp";

    /**
     * The FORM error page. It should always start with a '/'
     */
    protected String formErrorPage = "/error.jsp";

    /**
     * The FORM login page. It should always start with a '/'
     */
    public void setFormAuthPage(String formAuthPage) {
        this.formAuthPage = formAuthPage;
    }

    /**
     * The FORM error page. It should always start with a '/'
     */
    public void setFormErrorPage(String formErrorPage) {
        this.formErrorPage = formErrorPage;
    }

    /**
     * The default page. It should always start with a '/'
     */
    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }

    public String getRealmName() {
        return this.realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism#doAuthenticate(org.picketbox.core.Credential, org.picketbox.core.authentication.AuthenticationResult)
     */
    @Override
    protected Principal doAuthenticate(Credential credential, AuthenticationResult result) throws AuthenticationException {
        if (!(credential instanceof HttpServletCredential)) {
            throw PicketBoxMessages.MESSAGES.unexpectedCredentialType(credential, HttpServletCredential.class);
        }

        HttpServletCredential httpCredential = (HttpServletCredential) credential;

        HttpServletRequest request = httpCredential.getRequest();
        HttpServletResponse response = httpCredential.getResponse();

        UserContext subject = getPicketBoxManager().getUserContext(request);

        if (subject != null && subject.isAuthenticated()) {
            return subject.getPrincipal();
        }

        boolean jSecurityCheck = isAuthenticationRequest(request);

        if (!jSecurityCheck) {
            if (getPicketBoxManager().requiresAuthentication(request, response)) {
                this.requestCache.saveRequest(request);
                result.setStatus(AuthenticationStatus.CONTINUE);
                challengeClient(request, response);
            }

            return null;
        }

        Principal authenticatedPrincipal = performAuthentication(request, response);

        if (authenticatedPrincipal == null) {
            result.setStatus(AuthenticationStatus.INVALID_CREDENTIALS);
        }

        return authenticatedPrincipal;
    }

    protected abstract boolean isAuthenticationRequest(HttpServletRequest request);

    protected Principal performAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        Principal principal = doHTTPAuthentication(request, response);

        if (principal == null) {
            sendErrorPage(request, response);
            return null;
        }

        if (principal != null) {
            // remove from the cache the saved request and store it in the session for further use.
            SavedRequest savedRequest = this.requestCache.removeAndStoreSavedRequestInSession(request);
            String requestedURI = null;

            if (savedRequest != null) {
                requestedURI = savedRequest.getRequestURI();
            }

            // if the user has explicit defined a default page url, use it to redirect the user after a successful
            // authentication.
            if (!getDefaultPage().equals(DEFAULT_PAGE_URL) || requestedURI == null) {
                requestedURI = request.getContextPath() + getDefaultPage();
            }

            sendRedirect(response, requestedURI);
        }

        return principal;
    }

    protected abstract Principal doHTTPAuthentication(HttpServletRequest request, HttpServletResponse response);

    protected abstract void challengeClient(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException;

    protected void sendErrorPage(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        sendRedirect(response, request.getContextPath() + getFormErrorPage());
    }

    protected void sendRedirect(HttpServletResponse response, String redirectUrl) throws AuthenticationException {
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw MESSAGES.failRedirectToDefaultPage(redirectUrl, e);
        }
    }

    protected void forwardLoginPage(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        RequestDispatcher rd = request.getServletContext().getRequestDispatcher(getFormAuthPage());
        if (rd == null)
            throw MESSAGES.unableToFindRequestDispatcher();

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.impl.AbstractAuthenticationMechanism#getPicketBoxManager()
     */
    @Override
    protected PicketBoxHTTPManager getPicketBoxManager() {
        return (PicketBoxHTTPManager) super.getPicketBoxManager();
    }

    protected HTTPAuthenticationConfiguration getAuthenticationConfig() {
        return (HTTPAuthenticationConfiguration) getPicketBoxManager().getConfiguration().getAuthentication();
    }

    public String getDefaultPage() {
        return this.defaultPage;
    }

    public String getFormAuthPage() {
        return this.formAuthPage;
    }

    public String getFormErrorPage() {
        return this.formErrorPage;
    }
}