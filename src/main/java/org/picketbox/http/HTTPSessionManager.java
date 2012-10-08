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

import javax.servlet.http.HttpSession;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.UserContext;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.session.DefaultSessionManager;
import org.picketbox.core.session.PicketBoxSession;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class HTTPSessionManager extends DefaultSessionManager {

    private PicketBoxHTTPConfiguration configuration;

    public HTTPSessionManager(PicketBoxManager picketBoxManager) {
        super(picketBoxManager);
        this.configuration = (PicketBoxHTTPConfiguration) picketBoxManager.getConfiguration();
    }

    @Override
    protected PicketBoxSession doCreateSession(UserContext authenticatedUserContext) {
        HTTPUserContext httpUserContext = (HTTPUserContext) authenticatedUserContext;

        HttpSession httpSession = httpUserContext.getRequest().getSession();

        httpSession.setAttribute(getUserAttributeName(), httpUserContext);

        return new PicketBoxHTTPSession(httpSession);
    }

    /**
     * <p>Returns the attribute name that should be used to store the {@link UserContext}.</p>
     *
     * @return
     */
    private String getUserAttributeName() {
        String name = this.configuration.getSessionManager().getSessionAttributeName();

        if (name == null) {
            name = PicketBoxConstants.SUBJECT;
        }

        return name;
    }
}
