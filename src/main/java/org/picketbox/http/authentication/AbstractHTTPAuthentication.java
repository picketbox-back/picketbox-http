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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;

import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authentication.AuthenticationCallbackHandler;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.http.PicketBoxHTTPManager;

/**
 * Base class for all the HTTP authentication schemes
 *
 * @author anil saldhana
 * @since Jul 6, 2012
 */
public abstract class AbstractHTTPAuthentication implements HTTPAuthenticationScheme {

    private RequestCache requestCache = new RequestCache();

    private PicketBoxHTTPManager picketBoxManager;

    /**
     * Injectable realm name
     */
    protected String realmName = HTTPAuthenticationScheme.REALM;

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

    public void setPicketBoxManager(PicketBoxHTTPManager picketBoxManager) {
        this.picketBoxManager = picketBoxManager;
    }

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
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
    }

    @Override
    public Principal authenticate(ServletRequest servletReq, ServletResponse servletResp) throws AuthenticationException {
        HttpServletRequest request = (HttpServletRequest) servletReq;
        HttpServletResponse response = (HttpServletResponse) servletResp;

        if (this.picketBoxManager.isAuthenticated(request)) {
            PicketBoxSubject subject = this.picketBoxManager.getAuthenticatedUser(request);

            return subject.getUser();
        }

        boolean jSecurityCheck = isAuthenticationRequest(request);

        if (jSecurityCheck == false) {
            this.requestCache.saveRequest(request);
            challengeClient(request, response);
            return null;
        }

        PicketBoxSubject subject = performAuthentication(request, response);

        if (subject == null) {
            return null;
        }

        return subject.getUser();
    }

    protected abstract boolean isAuthenticationRequest(HttpServletRequest request);

    protected PicketBoxSubject performAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        AuthenticationCallbackHandler authenticationCallbackHandler = getAuthenticationCallbackHandler(request, response);

        if (authenticationCallbackHandler == null) {
            challengeClient(request, response);
            return null;
        }

        PicketBoxSubject subject = this.picketBoxManager.authenticate(authenticationCallbackHandler);

        if (subject != null) {
            // remove from the cache the saved request and store it in the session for further use.
            SavedRequest savedRequest = this.requestCache.removeAndStoreSavedRequestInSession(request);
            String requestedURI = null;

            if (savedRequest != null) {
                requestedURI = savedRequest.getRequestURI();
            }

            request.getSession(true).setAttribute(PicketBoxConstants.SUBJECT, subject);

            // if the user has explicit defined a default page url, use it to redirect the user after a successful
            // authentication.
            if (!this.defaultPage.equals(DEFAULT_PAGE_URL) || requestedURI == null) {
                requestedURI = request.getContextPath() + this.defaultPage;
            }

            sendRedirect(response, requestedURI);
        } else {
            sendErrorPage(request, response);
        }

        return subject;
    }

    protected abstract AuthenticationCallbackHandler getAuthenticationCallbackHandler(HttpServletRequest request,
            HttpServletResponse response);

    protected abstract void challengeClient(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException;

    protected void sendErrorPage(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        sendRedirect(response, request.getContextPath() + this.formErrorPage);
    }

    protected void sendRedirect(HttpServletResponse response, String redirectUrl) throws AuthenticationException {
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw MESSAGES.failRedirectToDefaultPage(redirectUrl, e);
        }
    }

    protected void forwardLoginPage(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        RequestDispatcher rd = request.getServletContext().getRequestDispatcher(this.formAuthPage);
        if (rd == null)
            throw MESSAGES.unableToFindRequestDispatcher();

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

}