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

package org.picketbox.http.wrappers;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.picketbox.core.UserContext;
import org.picketbox.http.PicketBoxHTTPManager;

/**
 * <p>
 * A {@link HttpServletRequest} wrapper.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private PicketBoxHTTPManager picketBoxManager;

    public RequestWrapper(HttpServletRequest request, PicketBoxHTTPManager picketBoxManager) {
        super(request);
        this.picketBoxManager = picketBoxManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequestWrapper#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        UserContext userContext = getUserContext();

        if (userContext == null) {
            return null;
        }

        return userContext.getPrincipal();
    }

    /**
     * <p>
     * Returns a {@link UserContext} for an authenticated user. If the user is not authenticated is returned null.
     * </p>
     *
     * @return
     */
    public UserContext getUserContext() {
        return this.picketBoxManager.getUserContext(this);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequestWrapper#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole(String role) {
        UserContext userContext = getUserContext();

        return userContext != null && userContext.hasRole(role);
    }
}
