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

package org.picketbox.http.logout;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.picketbox.core.PicketBoxMessages;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.logout.LogoutManager;
import org.picketbox.http.PicketBoxHTTPMessages;

/**
 * <p>
 * This class provides the basic functionalities for the logout process.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class HTTPLogoutManager implements LogoutManager<HttpServletRequest, HttpServletResponse> {

    /**
     * <p>
     * URL used to start the logout process.
     * </p>
     */
    private String logoutUrl;

    /**
     * <p>
     * Page URL to redirect the user after a successful logout.
     * </p>
     */
    private String logoutPage;

    /**
     * <p>
     * Process the logout.
     * </p>
     *
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        if (isLogoutRequest(request)) {
            HttpSession session = request.getSession(false);

            if (session == null) {
                throw PicketBoxMessages.MESSAGES.invalidUserSession();
            }

            session.invalidate();

            try {
                String logoutPage = getLogoutPage();

                if (getLogoutPage() == null) {
                    logoutPage = request.getContextPath();
                }

                response.sendRedirect(logoutPage);
            } catch (IOException e) {
                throw PicketBoxHTTPMessages.MESSAGES.runtimeException(e);
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
        String logoutUrl = getLogoutUrl();

        if (logoutUrl == null) {
            logoutUrl = PicketBoxConstants.LOGOUT_URI;
        }

        return request.getRequestURI().contains(logoutUrl);
    }

    /**
     * @return the logoutUrl
     */
    public String getLogoutUrl() {
        return this.logoutUrl;
    }

    /**
     * @param logoutUrl the logoutUrl to set
     */
    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    /**
     * @return the logoutPage
     */
    public String getLogoutPage() {
        return this.logoutPage;
    }

    /**
     * @param logoutPage the logoutPage to set
     */
    public void setLogoutPage(String logoutPage) {
        this.logoutPage = logoutPage;
    }

}