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

package org.picketbox.http.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketbox.http.resource.ProtectedResource;
import org.picketbox.http.resource.ProtectedResourceManager;

/**
 * Configuration for a Protected Resource
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class ProtectedResourceConfig {

    /**
     * The related {@link ProtectedResourceManager}
     */
    @SuppressWarnings("rawtypes")
    private ProtectedResourceManager manager;
    /**
     * A list of {@link ProtectedResource}
     */
    private List<ProtectedResource> resources = new ArrayList<ProtectedResource>();

    /**
     * Construct a resource config using a manager and a list of resources
     *
     * @param manager
     * @param resources
     */
    @SuppressWarnings("rawtypes")
    public ProtectedResourceConfig(ProtectedResourceManager manager, List<ProtectedResource> resources) {
        this.manager = manager;
        this.resources.addAll(resources);
    }

    /**
     * Get the manager
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public ProtectedResourceManager getManager() {
        return manager;
    }

    /**
     * Get a read only list of resources
     *
     * @return
     */
    public List<ProtectedResource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    /**
     * Add a resource
     *
     * @param resource
     */
    public void addResource(ProtectedResource resource) {
        this.resources.add(resource);
    }

    /**
     * Remove a resource
     *
     * @param resource
     */
    public void removeResource(ProtectedResource resource) {
        this.resources.remove(resource);
    }

    /**
     * Remove all the resources
     */
    public void removeAllResource() {
        resources.clear();
    }
}