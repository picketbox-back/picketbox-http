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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.PicketBoxPrincipal;
import org.picketbox.core.authentication.AuthenticationInfo;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.http.config.HTTPAuthenticationConfiguration;
import org.picketbox.http.config.HTTPFormConfiguration;
import org.picketlink.idm.model.User;

/**
 * Perform HTTP Form Authentication
 *
 * @author anil saldhana
 * @since July 9, 2012
 */
public class HTTPFormAuthentication extends AbstractHTTPAuthentication {

    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.AuthenticationMechanism#getAuthenticationInfo()
     */
    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        List<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("HTTP FORM Authentication Credential", "Authenticates users using the HTTP FORM Authentication scheme.", HTTPFormCredential.class));

        return info;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.core.authentication.http.AbstractHTTPAuthentication#isAuthenticationRequest(javax.servlet.http.
     * HttpServletRequest)
     */
    @Override
    protected boolean isAuthenticationRequest(HttpServletRequest request) {
        return request.getRequestURI().contains(PicketBoxConstants.HTTP_FORM_J_SECURITY_CHECK);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.http.authentication.AbstractHTTPAuthentication#getAuthenticationCallbackHandler(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected Principal doHTTPAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String userName = request.getParameter(PicketBoxConstants.HTTP_FORM_J_USERNAME);
        String password = request.getParameter(PicketBoxConstants.HTTP_FORM_J_PASSWORD);

        User user = getIdentityManager().getUser(userName);

        if (user != null && getIdentityManager().validatePassword(user, password)) {
            return new PicketBoxPrincipal(user.getKey());
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.picketbox.core.authentication.http.AbstractHTTPAuthentication#challengeClient(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void challengeClient(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        forwardLoginPage(request, response);
    }

    /* (non-Javadoc)
     * @see org.picketbox.http.authentication.AbstractHTTPAuthentication#getFormAuthPage()
     */
    @Override
    public String getFormAuthPage() {
        HTTPAuthenticationConfiguration authenticationConfig = getAuthenticationConfig();

        if (authenticationConfig != null) {
            HTTPFormConfiguration formConfiguration = authenticationConfig.getFormConfiguration();

            if (formConfiguration != null && formConfiguration.getFormAuthPage() != null) {
                super.formAuthPage = formConfiguration.getFormAuthPage();
            }
        }

        return super.formAuthPage;
    }

    /* (non-Javadoc)
     * @see org.picketbox.http.authentication.AbstractHTTPAuthentication#getDefaultPage()
     */
    @Override
    public String getDefaultPage() {
        HTTPAuthenticationConfiguration authenticationConfig = getAuthenticationConfig();

        if (authenticationConfig != null) {
            HTTPFormConfiguration formConfiguration = authenticationConfig.getFormConfiguration();

            if (formConfiguration != null && formConfiguration.getDefaultPage() != null) {
                super.defaultPage = formConfiguration.getDefaultPage();
            }
        }

        return super.defaultPage;
    }

    /* (non-Javadoc)
     * @see org.picketbox.http.authentication.AbstractHTTPAuthentication#getFormErrorPage()
     */
    @Override
    public String getFormErrorPage() {
        HTTPAuthenticationConfiguration authenticationConfig = getAuthenticationConfig();

        if (authenticationConfig != null) {
            HTTPFormConfiguration formConfiguration = authenticationConfig.getFormConfiguration();

            if (formConfiguration != null && formConfiguration.getErrorPage() != null) {
                super.formErrorPage = formConfiguration.getErrorPage();
            }
        }

        return super.formErrorPage;
    }
}