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

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.picketbox.core.authentication.PicketBoxConstants;

/**
 * <p>
 * This class maintains a cache of {@link SavedRequest} instances created from {@link HttpServletRequest} instances. This class
 * is to be used during authentication to help to retrieve previous informations from the request made for the first time before
 * the authentication process begins. It also stores the cached request in the user session for later use, if necessary.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class RequestCache {

    private ConcurrentHashMap<String, SavedRequest> requestCache = new ConcurrentHashMap<String, SavedRequest>();

    /**
     * <p>
     * Saves a {@link HttpServletRequest} as a {@link SavedRequest} instance. All the state from the original request will be
     * copied.
     * </p>
     *
     * @param request
     */
    public void saveRequest(HttpServletRequest request) {
        requestCache.put(getCurrentSession(request).getId(), new SavedRequest(request));
    }

    /**
     * <p>
     * Returns the user session. If no session was created a exception is raised. A valid session must exist before invoking
     * this method.
     * </p>
     */
    private HttpSession getCurrentSession(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        if (session == null) {
            throw new IllegalStateException("Unable to cache the request. User session was not created.");
        }
        return session;
    }

    /**
     * <p>
     * Removes a cached request and stores it in the session.
     * </p>
     */
    public SavedRequest removeAndStoreSavedRequestInSession(HttpServletRequest request) {
        HttpSession session = getCurrentSession(request);
        SavedRequest savedRequest = this.requestCache.remove(session.getId());

        session.setAttribute(PicketBoxConstants.SAVED_REQUEST, savedRequest);

        return savedRequest;
    }
}
