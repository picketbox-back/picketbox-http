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
import org.picketbox.core.authentication.DigestHolder;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.exceptions.FormatException;
import org.picketbox.core.util.Base64;
import org.picketbox.core.util.HTTPDigestUtil;
import org.picketbox.http.HTTPUserContext;
import org.picketbox.http.authentication.HTTPDigestAuthentication;
import org.picketbox.http.authentication.HTTPDigestCredential;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.test.http.TestServletRequest;
import org.picketbox.test.http.TestServletResponse;

/**
 * Unit test the {@link HTTPDigestAuthentication} class
 *
 * @author anil saldhana
 * @since July 6, 2012
 */
public class HTTPDigestAuthenticationTestCase extends AbstractAuthenticationTest {

    @Before
    public void setup() throws Exception {
        super.initialize();
    }
    
    @Override
    protected void doConfigureManager(HTTPConfigurationBuilder configuration) {
        configuration.authentication().digest().realm("testrealm@host.com");
        configuration.authentication().digest().opaque("5ccc069c403ebaf9f0171e9517f40e41");
    }

    @Test
    public void testHttpDigest() throws Exception {
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
        req.setContextPath("/test");
        req.setRequestURI(req.getContextPath() + "/index.html");

        // Call the server to get the digest challenge
        UserContext authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(req, resp,
                new HTTPDigestCredential(req, resp)));
        
        // mechanism is telling us that we need to continue with the authentication.
        assertNotNull(authenticatedUser);
        Assert.assertFalse(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.CONTINUE);

        String authorizationHeader = resp.getHeader(PicketBoxConstants.HTTP_WWW_AUTHENTICATE);
        authorizationHeader = authorizationHeader.substring(7);
        String[] tokens = HTTPDigestUtil.quoteTokenize(authorizationHeader);

        // Let us get the digest info
        DigestHolder digest = HTTPDigestUtil.digest(tokens);

        // Get Positive Authentication
        req.addHeader(PicketBoxConstants.HTTP_AUTHORIZATION_HEADER, "Digest " + getPositive(digest));

        authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(req, resp,
                new HTTPDigestCredential(req, resp)));

        assertNotNull(authenticatedUser);
        Assert.assertTrue(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.SUCCESS);

        req.clearHeaders();
        req.getSession().setAttribute(PicketBoxConstants.SUBJECT, null);
        // Get Negative Authentication
        req.addHeader(PicketBoxConstants.HTTP_AUTHORIZATION_HEADER, "Digest " + getNegative());
        
        authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(req, resp,
                new HTTPDigestCredential(req, resp)));

        assertNotNull(authenticatedUser);
        Assert.assertFalse(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.INVALID_CREDENTIALS);
        String digestHeader = resp.getHeader(PicketBoxConstants.HTTP_WWW_AUTHENTICATE);
        assertTrue(digestHeader.startsWith("Digest realm="));
    }

    private String getPositive(DigestHolder digest) {
        String cnonce = "0a4f113b";
        String clientResponse = null;
        try {
            digest.setUsername("Aladdin");
            digest.setRequestMethod("GET");
            digest.setUri("/dir/index.html");
            digest.setCnonce(cnonce);
            digest.setNc("00000001");
            digest.setQop("auth");

            clientResponse = HTTPDigestUtil.clientResponseValue(digest, "Open Sesame".toCharArray());
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }

        StringBuilder str = new StringBuilder(" username=\"Aladdin\",");

        str.append("realm=\"" + digest.getRealm() + "\",");
        str.append("nonce=\"").append(digest.getNonce()).append("\",");
        str.append("uri=\"/dir/index.html\",");
        str.append("qop=auth,").append("nc=00000001,").append("cnonce=\"" + cnonce + "\",");
        str.append("response=\"" + clientResponse + "\",");
        str.append("opaque=\"").append(digest.getOpaque()).append("\"");
        return str.toString();
    }

    private String getNegative() {
        String str = "Aladdin:Bad sesame";
        String encoded = Base64.encodeBytes(str.getBytes());
        return encoded;
    }
}