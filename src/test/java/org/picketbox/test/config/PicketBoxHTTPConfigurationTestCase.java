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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.picketbox.core.UserContext;
import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
import org.picketbox.core.identity.UserContextPopulator;
import org.picketbox.http.PicketBoxHTTPManager;
import org.picketbox.http.HTTPUserContext;
import org.picketbox.http.authorization.resource.WebResource;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;
import org.picketbox.http.resource.ProtectedResourceConstraint;
import org.picketbox.test.http.TestServletRequest;
import org.picketbox.test.http.TestServletResponse;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class PicketBoxHTTPConfigurationTestCase {

    private PicketBoxHTTPManager picketBoxManager;

    @Before
    public void onSetup() {
        HTTPConfigurationBuilder builder = new HTTPConfigurationBuilder();

        builder.identityManager().userPopulator(new UserContextPopulator() {
            @Override
            public UserContext getIdentity(UserContext resultingUserContext) {
                List<Role> roles = new ArrayList<Role>();

                roles.add(new SimpleRole("Manager"));
                roles.add(new SimpleRole("Financial"));

                resultingUserContext.setRoles(roles);

                return resultingUserContext;
            }
        });

        builder.protectedResource().resource("/secure/*", ProtectedResourceConstraint.ALL)
                .resource("/notSecured/index.html", ProtectedResourceConstraint.NOT_PROTECTED)
                .resource("/onlyRoleManager/index.html", new String[] { "Manager" })
                .resource("/onlyRoleFinancial/index.html", new String[] { "Financial" });

        PicketBoxHTTPConfiguration build = (PicketBoxHTTPConfiguration) builder.build();

        this.picketBoxManager = new PicketBoxHTTPManager(build);

        this.picketBoxManager.start();
    }

    @Test
    public void testFluentConfiguration() throws Exception {
        TestServletRequest req = new TestServletRequest(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });

        TestServletResponse resp = new TestServletResponse(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                System.out.println(b);
            }
        });

        req.setMethod("GET");
        req.setContextPath("/test-app");
        req.setRequestURI(req.getContextPath() + "/secure/index.html");

        HTTPUserContext authenticationUserContext = new HTTPUserContext(req, resp, new UsernamePasswordCredential(
                "admin", "admin"));

        UserContext subject = picketBoxManager.authenticate(authenticationUserContext);

        Assert.assertNotNull(subject);
    }

    @Test
    public void testNotProtectedResource() throws Exception {
        TestServletRequest req = new TestServletRequest(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });

        TestServletResponse resp = new TestServletResponse(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                System.out.println(b);
            }
        });

        req.setMethod("GET");
        req.setContextPath("/test-app");
        req.setRequestURI(req.getContextPath() + "/notSecured/index.html");

        HTTPUserContext authenticationUserContext = new HTTPUserContext(req, resp, new UsernamePasswordCredential(
                "admin", "admin"));

        UserContext subject = picketBoxManager.authenticate(authenticationUserContext);

        Assert.assertNotNull(subject);
        Assert.assertFalse(subject.isAuthenticated());

    }

    @Test
    public void testRoleProtectedResource() throws Exception {
        TestServletRequest req = new TestServletRequest(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });

        TestServletResponse resp = new TestServletResponse(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                System.out.println(b);
            }
        });

        req.setMethod("GET");
        req.setContextPath("/test-app");
        req.setRequestURI(req.getContextPath() + "/onlyRoleManager/index.html");

        HTTPUserContext authenticationUserContext = new HTTPUserContext(req, resp, new UsernamePasswordCredential(
                "admin", "admin"));

        UserContext subject = picketBoxManager.authenticate(authenticationUserContext);

        Assert.assertNotNull(subject);

        WebResource resource = new WebResource();

        resource.setRequest(req);
        resource.setResponse(resp);

        boolean isAuthorized = this.picketBoxManager.authorize(subject, resource);

        Assert.assertTrue(isAuthorized);
    }

}
