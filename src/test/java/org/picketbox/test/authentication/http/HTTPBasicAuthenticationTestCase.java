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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picketbox.core.UserContext;
import org.picketbox.core.authentication.AuthenticationStatus;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.util.Base64;
import org.picketbox.http.HTTPUserContext;
import org.picketbox.http.authentication.HTTPBasicAuthentication;
import org.picketbox.http.authentication.HTTPBasicCredential;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.test.http.TestServletRequest;
import org.picketbox.test.http.TestServletResponse;

/**
 * Unit test the {@link HTTPBasicAuthentication} class
 *
 * @author anil saldhana
 * @since July 5, 2012
 */
public class HTTPBasicAuthenticationTestCase extends AbstractAuthenticationTest {

    @Before
    public void setup() throws Exception {
        super.initialize();
    }
    
    @Override
    protected void doConfigureManager(HTTPConfigurationBuilder configuration) {
        configuration.authentication().digest().realm("Custom Realm");
    }

    @Test
    public void testHttpBasic() throws Exception {
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

        // Get Positive Authentication
        req.addHeader(PicketBoxConstants.HTTP_AUTHORIZATION_HEADER, "Basic " + getPositive());
        req.setContextPath("/test");
        req.setRequestURI(req.getContextPath() + "/index.html");

        UserContext authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(req, resp, new HTTPBasicCredential(req, resp)));
        
        assertNotNull(authenticatedUser);
        Assert.assertTrue(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.SUCCESS);

        req.clearHeaders();
        req.getSession().setAttribute(PicketBoxConstants.SUBJECT, null);
        // Get Negative Authentication
        req.addHeader(PicketBoxConstants.HTTP_AUTHORIZATION_HEADER, "Basic " + getNegative());

        authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(req, resp, new HTTPBasicCredential(req, resp)));
        
        assertNotNull(authenticatedUser);
        Assert.assertFalse(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.INVALID_CREDENTIALS);

        String basicHeader = resp.getHeader(PicketBoxConstants.HTTP_WWW_AUTHENTICATE);
        assertTrue(basicHeader.startsWith("basic realm="));
    }

    private String getPositive() {
        String str = "Aladdin:Open Sesame";
        String encoded = Base64.encodeBytes(str.getBytes());
        return encoded;
    }

    private String getNegative() {
        String str = "Aladdin:Bad sesame";
        String encoded = Base64.encodeBytes(str.getBytes());
        return encoded;
    }
}