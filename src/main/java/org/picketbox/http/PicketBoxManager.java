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

package org.picketbox.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.AbstractPicketBoxLifeCycle;
import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authentication.AuthenticationCallbackHandler;
import org.picketbox.core.authentication.AuthenticationMechanism;
import org.picketbox.core.authentication.AuthenticationProvider;
import org.picketbox.core.authentication.AuthenticationResult;
import org.picketbox.core.authentication.AuthenticationService;
import org.picketbox.core.authentication.AuthenticationStatus;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.authorization.AuthorizationManager;
import org.picketbox.core.authorization.EntitlementsManager;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.core.exceptions.AuthorizationException;
import org.picketbox.core.identity.IdentityManager;
import org.picketbox.core.logout.LogoutManager;
import org.picketbox.core.resource.ProtectedResource;
import org.picketbox.core.resource.ProtectedResourceManager;
import org.picketbox.http.authorization.resource.WebResource;

/**
 * <p>
 * This class acts as a <i>Facade</i> for the PicketBox Security capabilites.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class PicketBoxManager extends AbstractPicketBoxLifeCycle {

    private AuthenticationProvider authenticationProvider;
    private LogoutManager logoutManager;
    private ProtectedResourceManager protectedResourceManager;
    private AuthorizationManager authorizationManager;
    private EntitlementsManager entitlementsManager;
    private IdentityManager identityManager;

    public PicketBoxManager(AuthenticationProvider authenticationProvider, LogoutManager logoutManager,
            ProtectedResourceManager protectedResourceManager) {
        if (authenticationProvider == null) {
            throw PicketBoxHTTPMessages.MESSAGES.invalidNullArgument("Authentication Scheme");
        }

        if (logoutManager == null) {
            throw PicketBoxHTTPMessages.MESSAGES.invalidNullArgument("Logout Manager");
        }

        if (protectedResourceManager == null) {
            throw PicketBoxHTTPMessages.MESSAGES.invalidNullArgument("Protected Resource Manager");
        }

        this.authenticationProvider = authenticationProvider;
        this.logoutManager = logoutManager;
        this.protectedResourceManager = protectedResourceManager;
    }

    /**
     * <p>
     * Checks if the specified {@link HttpServletRequest} instance is from an authenticated user.
     * </p>
     *
     * @param servletReq
     * @return true if the request came from an authenticated user.
     */
    public boolean isAuthenticated(HttpServletRequest servletReq) {
        return getAuthenticatedUser(servletReq) != null;
    }

    /**
     * Get the Authenticated User
     *
     * @param servletReq
     * @return
     */
    public PicketBoxSubject getAuthenticatedUser(HttpServletRequest servletReq) {
        checkIfStarted();

        if (servletReq.getSession(false) == null) {
            return null;
        }

        return (PicketBoxSubject) servletReq.getSession(false).getAttribute(PicketBoxConstants.SUBJECT);
    }

    /**
     * @param authenticationCallbackHandler
     * @throws AuthenticationException
     */
    public PicketBoxSubject authenticate(AuthenticationCallbackHandler authenticationCallbackHandler)
            throws AuthenticationException {
        AuthenticationResult result = null;

        String[] mechanisms = this.authenticationProvider.getSupportedMechanisms();

        for (String mechanismName : mechanisms) {
            AuthenticationMechanism mechanism = this.authenticationProvider.getMechanism(mechanismName);
            AuthenticationService authenticationService = mechanism.getService();

            if (authenticationService.supportsHandler(authenticationCallbackHandler.getClass())) {
                try {
                    result = authenticationService.authenticate(authenticationCallbackHandler);
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                }
            }
        }

        if (result == null) {
            throw new AuthenticationException("Authentication not supported. Using handler: " + authenticationCallbackHandler);
        }

        PicketBoxSubject subject = null;

        if (result.getStatus().equals(AuthenticationStatus.SUCCESS)) {
            subject = this.identityManager.getIdentity(result.getSubject().getUser());
        }

        return subject;
    }

    /**
     * <pAuthorizes a user.</p>
     *
     * @param servletReq
     * @param servletResp
     *
     * @return true is the user is authorized.
     *
     * @throws AuthorizationException if some problem occurs during the authorization process.
     */
    public boolean authorize(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws AuthorizationException {
        try {
            checkIfStarted();

            ProtectedResource protectedResource = this.protectedResourceManager.getProtectedResource(httpRequest);

            if (!isPerformAuthorization(httpRequest, protectedResource)) {
                return true;
            }

            WebResource resource = new WebResource();
            resource.setContext(httpRequest.getServletContext());
            resource.setRequest(httpRequest);
            resource.setResponse(httpResponse);

            boolean isAuthorized = this.authorizationManager.authorize(resource, getAuthenticatedUser(httpRequest));

            return isAuthorized;
        } catch (Exception e) {
            throw PicketBoxHTTPMessages.MESSAGES.authorizationFailed(e);
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        checkIfStarted();
        this.logoutManager.logout(request, response);
    }

    private boolean isPerformAuthorization(HttpServletRequest httpRequest, ProtectedResource protectedResource) {
        return this.authorizationManager != null && this.isAuthenticated(httpRequest)
                && protectedResource.requiresAuthorization();
    }

    /**
     * @return the authorizationManager
     */
    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    /**
     * @param authorizationManager the authorizationManager to set
     */
    public void setAuthorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    /**
     * @return the identityManager
     */
    public IdentityManager getIdentityManager() {
        return identityManager;
    }

    /**
     * @param identityManager the identityManager to set
     */
    public void setIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    /**
     * @return the logoutManager
     */
    public LogoutManager getLogoutManager() {
        return this.logoutManager;
    }

    /**
     * @param logoutManager the logoutManager to set
     */
    public void setLogoutManager(LogoutManager logoutManager) {
        this.logoutManager = logoutManager;
    }

    /**
     * Get the {@link EntitlementsManager}
     *
     * @return
     */
    public EntitlementsManager getEntitlementsManager() {
        return entitlementsManager;
    }

    /**
     * Set the {@link EntitlementsManager}
     *
     * @param entitlementsManager
     */
    public void setEntitlementsManager(EntitlementsManager entitlementsManager) {
        this.entitlementsManager = entitlementsManager;
    }

    /**
     * Get the {@link ProtectedResourceManager}
     *
     * @return the protectedResourceManager
     */
    public ProtectedResourceManager getProtectedResourceManager() {
        return protectedResourceManager;
    }

    /**
     * Set the {@link ProtectedResourceManager}
     *
     * @param protectedResourceManager
     */
    public void setProtectedResourceManager(ProtectedResourceManager protectedResourceManager) {
        this.protectedResourceManager = protectedResourceManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.core.AbstractPicketBoxLifeCycle#doStart()
     */
    @Override
    protected void doStart() {
        PicketBoxHTTPLogger.LOGGER.debug("Using Logout Manager : " + this.logoutManager.getClass().getName());
        PicketBoxHTTPLogger.LOGGER.debug("Using Protected Resource Manager : "
                + this.protectedResourceManager.getClass().getName());

        if (this.authorizationManager != null) {
            PicketBoxHTTPLogger.LOGGER.debug("Using Authorization Manager : " + this.authorizationManager.getClass().getName());
        }

        if (this.identityManager != null) {
            PicketBoxHTTPLogger.LOGGER.debug("Using Identity Manager : " + this.identityManager.getClass().getName());
        }

        PicketBoxHTTPLogger.LOGGER.startingPicketBox();

        if (this.authorizationManager != null) {
            this.authorizationManager.start();
        }

        this.protectedResourceManager.start();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.core.AbstractPicketBoxLifeCycle#doStop()
     */
    @Override
    protected void doStop() {

    }

    public AuthenticationProvider getAuthenticationProvider() {
        return this.authenticationProvider;
    }

}