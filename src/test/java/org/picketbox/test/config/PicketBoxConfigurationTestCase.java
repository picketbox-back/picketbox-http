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

import junit.framework.Assert;

import org.junit.Test;
import org.picketbox.http.PicketBoxManager;
import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authentication.AuthenticationEvent;
import org.picketbox.core.authentication.AuthenticationEventHandler;
import org.picketbox.core.authentication.event.DefaultAuthenticationEventManager;
import org.picketbox.core.authentication.event.UserAuthenticatedEvent;
import org.picketbox.core.authentication.event.UserAuthenticationEventHandler;
import org.picketbox.core.authentication.handlers.UsernamePasswordAuthHandler;
import org.picketbox.core.authentication.impl.CertificateMechanism;
import org.picketbox.core.authentication.impl.DigestMechanism;
import org.picketbox.core.authentication.impl.UserNamePasswordMechanism;
import org.picketbox.core.authentication.manager.PropertiesFileBasedAuthenticationManager;
import org.picketbox.core.config.PicketBoxAuthenticationConfig;
import org.picketbox.http.config.PicketBoxConfiguration;
import org.picketbox.http.resource.HTTPProtectedResourceManager;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class PicketBoxConfigurationTestCase {

    @Test
    public void testConfiguration() throws Exception {
        PicketBoxConfiguration configuration = new PicketBoxConfiguration();

        PicketBoxAuthenticationConfig authentication = configuration.authentication();

        authentication.addMechanism(new UserNamePasswordMechanism()).addMechanism(new DigestMechanism())
                .addMechanism(new CertificateMechanism());

        authentication.addAuthManager(new PropertiesFileBasedAuthenticationManager());
        authentication.addEventManager(new DefaultAuthenticationEventManager());
        authentication.addObserver(new UserAuthenticationEventHandler() {

            @Override
            public Class<? extends AuthenticationEvent<? extends AuthenticationEventHandler>> getEventType() {
                return UserAuthenticatedEvent.class;
            }

            @Override
            public void onSucessfullAuthentication(UserAuthenticatedEvent userAuthenticatedEvent) {
                System.out.println("Authenticated.");
            }
        });

        configuration.setProtectedResourceManager(new HTTPProtectedResourceManager());

        PicketBoxManager buildAndStart = configuration.buildAndStart();

        PicketBoxSubject authenticate = buildAndStart.authenticate(new UsernamePasswordAuthHandler("admin", "admin"));

        Assert.assertNotNull(authenticate);
    }

}
