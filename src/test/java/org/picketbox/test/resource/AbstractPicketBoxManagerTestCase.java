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

package org.picketbox.test.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.junit.Before;
import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.http.authentication.HTTPFormAuthentication;
import org.picketbox.core.authentication.impl.CertificateMechanism;
import org.picketbox.core.authentication.impl.DigestMechanism;
import org.picketbox.core.authentication.impl.UserNamePasswordMechanism;
import org.picketbox.core.authentication.manager.PropertiesFileBasedAuthenticationManager;
import org.picketbox.core.authorization.AuthorizationManager;
import org.picketbox.core.authorization.Resource;
import org.picketbox.core.config.PicketBoxManagerConfiguration;
import org.picketbox.core.exceptions.AuthorizationException;
import org.picketbox.test.http.TestServletContext;
import org.picketbox.test.http.TestServletRequest;
import org.picketbox.test.http.TestServletResponse;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractPicketBoxManagerTestCase {

    protected TestServletContext servletContext;

    @Before
    public void onSetup() {
        this.servletContext = new TestServletContext(new HashMap<String, String>());
    }

    protected PicketBoxManagerConfiguration createConfiguration() {
        PicketBoxManagerConfiguration configuration = new PicketBoxManagerConfiguration();

        configuration.authentication().addMechanism(new UserNamePasswordMechanism()).addMechanism(new DigestMechanism())
                .addMechanism(new CertificateMechanism());

        configuration.authentication().addAuthManager(new PropertiesFileBasedAuthenticationManager());

        return configuration;
    }

    protected TestServletResponse createResponse() {
        return new TestServletResponse(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                System.out.println(b);
            }
        });
    }

    protected TestServletRequest createRequest(String uri) {
        TestServletRequest request = new TestServletRequest(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });

        request.setMethod("GET");
        request.setContextPath("/unittestapp");
        request.setRequestURI(request.getContextPath() + uri);

        return request;
    }

    protected HTTPFormAuthentication createAuthenticationScheme() {
        HTTPFormAuthentication authenticator = new HTTPFormAuthentication();

        return authenticator;
    }

    protected AuthorizationManager createAuthorizationManager() {
        return new TestAuthorizationManager() {

            @Override
            public boolean authorize(Resource resource, PicketBoxSubject subject) throws AuthorizationException {
                return doAuthorize(resource, subject);
            }
        };
    }

    protected boolean doAuthorize(Resource resource, PicketBoxSubject subject) {
        return true;
    }

    /**
     * <p>
     * Forces the creation of a security context. This is the same as authenticating the user.
     * </p>
     *
     * @param req
     */
    protected void forceSecurityContextCreation(TestServletRequest req) {
        req.getSession().setAttribute(PicketBoxConstants.SUBJECT, new PicketBoxSubject());
    }
}
