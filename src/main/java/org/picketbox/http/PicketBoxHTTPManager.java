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

import org.picketbox.core.AbstractPicketBoxManager;
import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.resource.ProtectedResource;
import org.picketbox.http.authorization.resource.WebResource;

/**
 * <p>
 * This class acts as a <i>Facade</i> for the PicketBox Security capabilites.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public final class PicketBoxHTTPManager extends AbstractPicketBoxManager {

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

}