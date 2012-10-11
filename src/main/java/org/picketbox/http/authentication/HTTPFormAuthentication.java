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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.Credential;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.http.PicketBoxHTTPManager;

/**
 * Perform HTTP Form Authentication
 *
 * @author anil saldhana
 * @since July 9, 2012
 */
public class HTTPFormAuthentication extends AbstractHTTPAuthentication {

    public HTTPFormAuthentication(PicketBoxHTTPManager securityManager) {
        super(securityManager);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.core.authentication.http.AbstractHTTPAuthentication#isAuthenticationRequest(javax.servlet.http.
     * HttpServletRequest)
     */
    protected boolean isAuthenticationRequest(HttpServletRequest request) {
        return request.getRequestURI().contains(PicketBoxConstants.HTTP_FORM_J_SECURITY_CHECK);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.http.authentication.AbstractHTTPAuthentication#getAuthenticationCallbackHandler(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected Credential getAuthenticationCallbackHandler(HttpServletRequest request, HttpServletResponse response) {
        String userName = request.getParameter(PicketBoxConstants.HTTP_FORM_J_USERNAME);
        String password = request.getParameter(PicketBoxConstants.HTTP_FORM_J_PASSWORD);

        return new UsernamePasswordCredential(userName, password);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.picketbox.core.authentication.http.AbstractHTTPAuthentication#challengeClient(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void challengeClient(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        forwardLoginPage(request, response);
    }

}