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

import org.picketbox.core.Credential;
import org.picketbox.core.UserContext;

/**
 * <p>
 * {@link UserContext} implementation for web applications.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class HTTPUserContext extends UserContext {

    private static final String RESPONSE_CONTEXT_DATA = "RESPONSE";
    private static final String REQUEST_CONTEXT_DATA = "REQUEST";

    private static final long serialVersionUID = 8826031649152896118L;

    public HTTPUserContext(HttpServletRequest request, HttpServletResponse response, Credential credential) {
        this.contextData.put(REQUEST_CONTEXT_DATA, request);
        this.contextData.put(RESPONSE_CONTEXT_DATA, response);
        setCredential(credential);
    }

    public HttpServletRequest getRequest() {
        return (HttpServletRequest) this.getContextData().get(REQUEST_CONTEXT_DATA);
    }

    public HttpServletResponse getResponse() {
        return (HttpServletResponse) this.getContextData().get(RESPONSE_CONTEXT_DATA);
    }

}
