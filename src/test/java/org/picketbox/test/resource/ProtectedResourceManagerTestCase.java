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

import org.junit.Test;
import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authorization.Resource;
import org.picketbox.core.resource.ProtectedResourceManager;
import org.picketbox.http.authorization.resource.WebResource;

/**
 * <p>
 * Unit Tests for the {@link ProtectedResourceManager} class.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class ProtectedResourceManagerTestCase extends AbstractPicketBoxManagerTestCase {

    private Resource testResource = new WebResource();

    /**
     * <p>
     * Tests a resource constrained with <code>ProtectedResourceConstraint.AUTHENTICATION
     * </p>
     * . No authorization should be performed.
     *
     * @throws Exception
     */
    @Test
    public void testNoAuthorizationResource() throws Exception {
        // PicketBoxConfiguration configuration = createConfiguration();
        //
        // configuration.addProtectedResource(ProtectedResource.ANY_RESOURCE_PATTERN,
        // ProtectedResourceConstraint.AUTHENTICATION);
        //
        // PicketBoxManager manager = configuration.buildAndStart();
        //
        // TestServletRequest req = createRequest("/anyResource");
        // TestServletResponse resp = createResponse();
        //
        // forceSecurityContextCreation(req);
        //
        // manager.authorize(req, resp);
        //
        // Assert.assertFalse(testResource.isAuthorized());
    }

    /**
     * <p>
     * Tests a resource constrained with <code>ProtectedResourceConstraint.AUTHORIZATION</code>. The resource is expected to be
     * authorized.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testAuthorizationResource() throws Exception {
        // PicketBoxConfiguration configuration = createConfiguration();
        //
        // configuration.addProtectedResource(ProtectedResource.ANY_RESOURCE_PATTERN,
        // ProtectedResourceConstraint.AUTHORIZATION);
        //
        // PicketBoxManager manager = configuration.buildAndStart();
        //
        // TestServletRequest req = createRequest("/anyResource");
        // TestServletResponse resp = createResponse();
        //
        // forceSecurityContextCreation(req);
        //
        // manager.authorize(req, resp);
        //
        // Assert.assertTrue(testResource.isAuthorized());
    }

    /**
     * <p>
     * Tests if a resource using a pattern <code>ProtectedResource.ANY_RESOURCE_PATTERN</code> is protected. User should be
     * redirect to the login page.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testResourcesProtectedWithAnyResourcePattern() throws Exception {
        // PicketBoxConfiguration configuration = createConfiguration();
        //
        // configuration.addProtectedResource(ProtectedResource.ANY_RESOURCE_PATTERN, ProtectedResourceConstraint.ALL);
        //
        // PicketBoxManager manager = configuration.buildAndStart();
        //
        // TestServletRequest req = createRequest("/anyResource");
        // TestServletResponse resp = createResponse();
        //
        // // Call the server to get the digest challenge
        // manager.authenticate(req, resp);
        //
        // // We will test that the request dispatcher is set on the form login page
        // TestRequestDispatcher rd = super.servletContext.getLast();
        // assertEquals(rd.getRequest(), req);
        //
        // assertEquals("/login.jsp", rd.getRequestUri());
    }

    /**
     * <p>
     * Tests if a resource constrained with <code>ProtectedResourceConstraint.NOT_PROTECTED</code> allows anonymous access.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testNotProtectedResource() throws Exception {
        // PicketBoxConfiguration configuration = createConfiguration();
        //
        // configuration.addProtectedResource("/notProtectedResource", ProtectedResourceConstraint.NOT_PROTECTED);
        //
        // PicketBoxManager manager = configuration.buildAndStart();
        //
        // TestServletRequest req = createRequest("/notProtectedResource");
        // TestServletResponse resp = createResponse();
        //
        // // Call the server to get the digest challenge
        // manager.authenticate(req, resp);
        //
        // // We will test that the request dispatcher was not used to redirect the user to the login page
        // TestRequestDispatcher rd = servletContext.getLast();
        //
        // // user should not be redirect/forwarded to any page.
        // Assert.assertNull(rd.getRequest());
    }

    /**
     * <p>
     * Tests if a resource constrained with <code>ProtectedResourceConstraint.NOT_PROTECTED</code> and using an specific access
     * pattern allows anonymous access.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testNotProtectedPrefixedResource() throws Exception {
        // PicketBoxConfiguration configuration = createConfiguration();
        //
        // configuration.addProtectedResource("/static/images/*", ProtectedResourceConstraint.NOT_PROTECTED);
        //
        // PicketBoxManager manager = configuration.buildAndStart();
        //
        // TestServletRequest req = createRequest("/static/images/someimage.png");
        // TestServletResponse resp = createResponse();
        //
        // // Call the server to get the digest challenge
        // manager.authenticate(req, resp);
        //
        // // We will test that the request dispatcher was not used to redirect the user to the login page
        // TestRequestDispatcher rd = servletContext.getLast();
        //
        // // user should not be redirect/forwarded to any page.
        // Assert.assertNull(rd.getRequest());
    }

    /**
     * <p>
     * Tests if all resources are protected by default.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testAllResourcesProtectByDefault() throws Exception {
        // PicketBoxManager manager = createConfiguration().buildAndStart();
        //
        // TestServletRequest req = createRequest("/anyResource");
        // TestServletResponse resp = createResponse();
        //
        // // Call the server to get the digest challenge
        // manager.authenticate(req, resp);
        //
        // // We will test that the request dispatcher is set on the form login page
        // TestRequestDispatcher rd = super.servletContext.getLast();
        // assertEquals(rd.getRequest(), req);
        //
        // assertEquals("/login.jsp", rd.getRequestUri());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.test.resource.AbstractPicketBoxManagerTestCase#doAuthorize(org.picketbox.core.authorization.Resource,
     * org.picketbox.core.PicketBoxSubject)
     */
    @Override
    protected boolean doAuthorize(Resource resource, PicketBoxSubject subject) {
        testResource.setAuthorized(true);
        return true;
    }
}
