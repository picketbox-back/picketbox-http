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
package org.picketbox.http.authorization.resource;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.authorization.Resource;
import org.picketbox.core.authorization.resource.AbstractPicketBoxResource;

/**
 * An instance of {@link Resource} that represents a web resource
 *
 * @author anil saldhana
 * @since Jul 12, 2012
 */
public class WebResource extends AbstractPicketBoxResource {
    private static final long serialVersionUID = 1L;

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ServletContext context;

    /**
     * Get the servlet request
     *
     * @return
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Set the servlet request
     *
     * @param request
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Get the servlet response
     *
     * @return
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Set the servlet response
     *
     * @param response
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Get the servlet context
     *
     * @return
     */
    public ServletContext getContext() {
        return context;
    }

    /**
     * Set the servlet context
     *
     * @param context
     */
    public void setContext(ServletContext context) {
        this.context = context;
    }
}