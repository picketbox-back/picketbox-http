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

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picketbox.core.UserContext;
import org.picketbox.core.authentication.AuthenticationStatus;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.http.HTTPUserContext;
import org.picketbox.http.authentication.HTTPFormAuthentication;
import org.picketbox.http.authentication.HTTPFormCredential;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.test.http.TestServletContext;
import org.picketbox.test.http.TestServletContext.TestRequestDispatcher;
import org.picketbox.test.http.TestServletRequest;
import org.picketbox.test.http.TestServletResponse;

/**
 * Unit test the {@link HTTPFormAuthentication} class
 * 
 * @author anil saldhana
 * @since July 9, 2012
 */
public class HTTPDefaultFormAuthenticationTestCase extends AbstractAuthenticationTest {

    private static final String CONTEXT_PATH = "/msite";

    private TestServletContext sc = new TestServletContext(new HashMap<String, String>());

    @Before
    public void setup() throws Exception {
        super.initialize();
    }

    /**
     * <p>
     * Tests if the authentication when using the default configuration.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultConfiguration() throws Exception {
        TestServletRequest req = new TestServletRequest(this.sc, new InputStream() {
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

        // Original URI
        String orig = "http://msite/someurl";

        req.setContextPath("/");
        req.setRequestURI(orig);

        // Call the server to get the challenge
        UserContext authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(req, resp,
                new HTTPFormCredential(req, resp)));

        // mechanism is telling us that we need to continue with the authentication.
        assertNotNull(authenticatedUser);
        Assert.assertFalse(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.CONTINUE);

        // We will test that the request dispatcher is set on the form login page
        TestRequestDispatcher rd = sc.getLast();
        assertEquals(rd.getRequest(), req);

        assertEquals("/login.jsp", rd.getRequestUri());

        // Now assume we have the login page. Lets post
        TestServletRequest newReq = new TestServletRequest(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });
        newReq.setRequestURI("http://msite" + PicketBoxConstants.HTTP_FORM_J_SECURITY_CHECK);
        newReq.setContextPath(CONTEXT_PATH);
        newReq.setParameter(PicketBoxConstants.HTTP_FORM_J_USERNAME, "Aladdin");
        newReq.setParameter(PicketBoxConstants.HTTP_FORM_J_PASSWORD, "Open Sesame");

        authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(newReq, resp, new HTTPFormCredential(newReq,
                resp)));

        assertNotNull(authenticatedUser);
        Assert.assertTrue(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.SUCCESS);

        // After authentication, we should be redirected to the default page
        assertEquals(resp.getSendRedirectedURI(), orig);
    }
}