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
import static junit.framework.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;
import org.picketbox.core.authentication.AbstractAuthenticationManager;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.http.PicketBoxHTTPManager;
import org.picketbox.http.authentication.HTTPClientCertAuthentication;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;
import org.picketbox.test.http.TestServletRequest;
import org.picketbox.test.http.TestServletResponse;

/**
 * Unit test the {@link HTTPClientCertAuthentication} class
 *
 * @author anil saldhana
 * @since July 9, 2012
 */
public class HTTPClientCertAuthenticationTestCase extends AbstractAuthenticationTest {

    private HTTPClientCertAuthentication httpClientCert = null;

    private class HTTPClientCertAuthenticationTestCaseAM extends AbstractAuthenticationManager {
        @Override
        public Principal authenticate(final String username, Object credential) throws AuthenticationException {
            if ("CN=jbid test, OU=JBoss, O=JBoss, C=US".equalsIgnoreCase(username) && ((String) credential).startsWith("W2G")) {
                return new Principal() {
                    @Override
                    public String getName() {
                        return username;
                    }
                };
            }
            return null;
        }

        @Override
        public boolean started() {
            return false;
        }

        @Override
        public void start() {
        }

        @Override
        public boolean stopped() {
            return false;
        }

        @Override
        public void stop() {
        }
    }

    @Before
    public void setup() throws Exception {
        super.initialize();
        
        httpClientCert = new HTTPClientCertAuthentication();

        configuration.authentication().authManager(new HTTPClientCertAuthenticationTestCaseAM());
        PicketBoxHTTPManager picketBoxManager = new PicketBoxHTTPManager((PicketBoxHTTPConfiguration) configuration.build());
        
        picketBoxManager.start();
        
        httpClientCert.setPicketBoxManager(picketBoxManager);
    }

    @Test
    public void testHttpForm() throws Exception {
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
        
        req.setContextPath("/test");
        req.setRequestURI(req.getContextPath() + "/index.html");
        
        InputStream bis = getClass().getClassLoader().getResourceAsStream("cert/servercert.txt");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(bis);
        bis.close();

        assertNotNull(cert);

        // Call the server to get the digest challenge
        Principal result = httpClientCert.authenticate(req, resp);

        assertNull(result);

        // Now set the certificate
        req.setAttribute(PicketBoxConstants.HTTP_CERTIFICATE, new X509Certificate[] { cert });

        result = httpClientCert.authenticate(req, resp);
        assertNotNull(result);
    }
}