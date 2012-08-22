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

package org.picketbox.test.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authentication.manager.SimpleCredentialAuthenticationManager;
import org.picketbox.core.identity.IdentityManager;
import org.picketbox.http.config.ConfigurationBuilderProvider;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.http.resource.ProtectedResourceConstraint;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class ProtectedResourcesConfigurationProvider implements ConfigurationBuilderProvider {

    /* (non-Javadoc)
     * @see org.picketbox.http.config.ConfigurationBuilderProvider#getBuilder(javax.servlet.ServletContext)
     */
    @Override
    public HTTPConfigurationBuilder getBuilder(ServletContext context) {
        HTTPConfigurationBuilder configurationBuilder = new HTTPConfigurationBuilder();

        configurationBuilder.authentication().authManager(new SimpleCredentialAuthenticationManager());

        configurationBuilder.identityManager().manager(new IdentityManager() {

            @Override
            public PicketBoxSubject getIdentity(PicketBoxSubject resultingSubject) {
                List<String> roles = new ArrayList<String>();

                roles.add("manager");

                resultingSubject.setRoleNames(roles);

                return resultingSubject;
            }
        });

        configurationBuilder
            .protectedResource()
                .resource("/notProtected", ProtectedResourceConstraint.NOT_PROTECTED)
                .resource("/onlyManagers", "manager")
                .resource("/confidentialResource", "confidential");

        return configurationBuilder;
    }

}