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

package org.picketbox.http.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.picketbox.core.PicketBoxLogger;
import org.picketbox.core.PicketBoxMessages;
import org.picketbox.http.authorization.resource.WebResource;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class HTTPProtectedResourceManager extends AbstractProtectedResourceManager implements
        ProtectedResourceManager<WebResource> {

    private List<ProtectedResource> resources = new ArrayList<ProtectedResource>();

    /**
     * @return the resources
     */
    public List<ProtectedResource> getResources() {
        return Collections.unmodifiableList(this.resources);
    }

    /**
     * @param resources the resources to set
     */
    public void setResources(List<ProtectedResource> resources) {
        this.resources = resources;
    }

    /**
     * <p>
     * Returns a {@link ProtectedResource} instance that matches the specified {@link HttpServletRequest} instance. If no match
     * is found, it will be returned a default resource. See <code>ProtectedResource.DEFAULT_RESOURCE</code>.
     * </p>
     *
     * @param servletReq
     * @return
     */
    public ProtectedResource getProtectedResource(WebResource servletReq) {
        checkIfStarted();

        String requestURI = servletReq.getRequest().getRequestURI()
                .substring(servletReq.getRequest().getContextPath().length());

        for (ProtectedResource resource : this.resources) {
            if (resource.matches(requestURI)) {
                return resource;
            }
        }

        return ProtectedResource.DEFAULT_RESOURCE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.core.AbstractPicketBoxLifeCycle#doStart()
     */
    @Override
    protected void doStart() {
        if (this.resources.isEmpty()) {
            PicketBoxLogger.LOGGER.allResourcesWillBeProteced();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.core.AbstractPicketBoxLifeCycle#doStop()
     */
    @Override
    protected void doStop() {

    }

    /**
     * <p>
     * Add a new {@link ProtectedResource} to the list of protected resources.
     * </p>
     *
     * @param pattern
     * @param constraint
     */
    public void addProtectedResource(String pattern, ProtectedResourceConstraint constraint) {
        if (started()) {
            throw PicketBoxMessages.MESSAGES.instanceAlreadyStarted();
        }

        this.resources.add(new ProtectedResource(pattern, constraint));
    }

    @Override
    public void addProtectedResource(ProtectedResource protectedResource) {
        this.resources.add(protectedResource);
    }

}
