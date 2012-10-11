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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.PicketBoxPrincipal;
import org.picketbox.core.authentication.AuthenticationInfo;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.core.util.Base64;
import org.picketlink.idm.model.User;

/**
 * Perform HTTP Basic Authentication
 *
 * @author anil saldhana
 * @since July 5, 2012
 */
public class HTTPBasicAuthentication extends AbstractHTTPAuthentication {

    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.AuthenticationMechanism#getAuthenticationInfo()
     */
    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        List<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("HTTP BASIC Authentication Credential", "Authenticates users using the HTTP BASIC Authentication scheme.", HTTPBasicCredential.class));

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
        String authorizationHeader = getAuthorizationHeader(request);

        return authorizationHeader != null && authorizationHeader.isEmpty() == false;
    }

    private String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader(PicketBoxConstants.HTTP_AUTHORIZATION_HEADER);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.core.authentication.http.AbstractHTTPAuthentication#getUserNamePasswordHandler(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected PicketBoxPrincipal doHTTPAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String authorizationHeader = getAuthorizationHeader(request);

        int whitespaceIndex = authorizationHeader.indexOf(' ');

        if (whitespaceIndex > 0) {
            String method = authorizationHeader.substring(0, whitespaceIndex);

            if (PicketBoxConstants.HTTP_BASIC.equalsIgnoreCase(method)) {
                authorizationHeader = authorizationHeader.substring(whitespaceIndex + 1);
                authorizationHeader = new String(Base64.decode(authorizationHeader));
                int indexOfColon = authorizationHeader.indexOf(':');

                if (indexOfColon > 0) {
                    String username = authorizationHeader.substring(0, indexOfColon);
                    String password = authorizationHeader.substring(indexOfColon + 1);

                    User user = getIdentityManager().getUser(username);

                    if (user != null && getIdentityManager().validatePassword(user, password)) {
                        return new PicketBoxPrincipal(username);
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected void challengeClient(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        response.setHeader(PicketBoxConstants.HTTP_WWW_AUTHENTICATE, "basic realm=\"" + this.realmName + '"');

        try {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    protected void sendErrorPage(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        challengeClient(request, response);
    }

}