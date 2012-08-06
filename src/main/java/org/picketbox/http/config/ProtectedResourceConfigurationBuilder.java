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
import java.util.List;

import org.picketbox.http.resource.HTTPProtectedResourceManager;
import org.picketbox.http.resource.ProtectedResource;
import org.picketbox.http.resource.ProtectedResourceConstraint;
import org.picketbox.http.resource.ProtectedResourceManager;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class ProtectedResourceConfigurationBuilder extends AbstractPicketBoxHTTPConfigBuilder<ProtectedResourceConfig> {

    private ProtectedResourceManager manager;
    private List<ProtectedResource> resources = new ArrayList<ProtectedResource>();

    public ProtectedResourceConfigurationBuilder(HTTPConfigurationBuilder builder) {
        super(builder);
    }

    @Override
    protected void setDefaults() {
        if (this.manager == null) {
            this.manager = new HTTPProtectedResourceManager();
        }
    }

    public ProtectedResourceConfigurationBuilder manager(ProtectedResourceManager manager) {
        this.manager = manager;
        return this;
    }

    @Override
    protected ProtectedResourceConfig doBuild() {
        return new ProtectedResourceConfig(this.manager, this.resources);
    }

    public ProtectedResourceConfigurationBuilder resource(String pattern, ProtectedResourceConstraint constraint) {
        this.resources.add(new ProtectedResource(pattern, constraint));
        return this;
    }

    public ProtectedResourceConfigurationBuilder resource(String pattern, String... roles) {
        this.resources.add(new ProtectedResource(pattern, ProtectedResourceConstraint.AUTHORIZATION, roles));
        return this;
    }

}
