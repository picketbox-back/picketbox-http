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
package org.picketbox.test.authentication.http;

import org.picketbox.core.PicketBoxManager;
import org.picketbox.http.PicketBoxHTTPManager;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;
import org.picketbox.test.config.IdentityManagerInitializer;

/**
 * Base class
 * 
 * @author anil saldhana
 * @since Aug 1, 2012
 */
public class AbstractAuthenticationTest {
    protected HTTPConfigurationBuilder configuration;
    protected PicketBoxHTTPManager picketBoxManager;

    public void initialize() throws Exception {
        configuration = new HTTPConfigurationBuilder();
        configuration.identityManager().fileStore().preserveState();
        IdentityManagerInitializer.initializeIdentityStore();

        doConfigureManager(configuration);

        this.picketBoxManager = new PicketBoxHTTPManager((PicketBoxHTTPConfiguration) configuration.build());

        this.picketBoxManager.start();
    }

    /**
     * <p>
     * Tests cases can override this method to provide additional configuration before creating and starting the
     * {@link PicketBoxManager} instance.
     * </p>
     * 
     * @param configuration
     */
    protected void doConfigureManager(HTTPConfigurationBuilder configuration) {

    }

}