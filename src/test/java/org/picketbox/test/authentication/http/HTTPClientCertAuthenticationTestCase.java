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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picketbox.core.UserContext;
import org.picketbox.core.authentication.AuthenticationStatus;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.http.HTTPUserContext;
import org.picketbox.http.authentication.HTTPClientCertAuthentication;
import org.picketbox.http.authentication.HTTPClientCertCredential;
import org.picketbox.http.config.HTTPConfigurationBuilder;
import org.picketbox.test.http.TestServletRequest;
import org.picketbox.test.http.TestServletResponse;

/**
 * Unit test the {@link HTTPClientCertAuthentication} class
 * 
 * @author anil saldhana
 * @since July 9, 2012
 */
public class HTTPClientCertAuthenticationTestCase extends AbstractAuthenticationTest {

    @Before
    public void onSetup() throws Exception {
        super.initialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.picketbox.test.authentication.http.AbstractAuthenticationTest#doConfigureManager(org.picketbox.http.config.
     * HTTPConfigurationBuilder)
     */
    @Override
    protected void doConfigureManager(HTTPConfigurationBuilder configuration) {
        configuration.authentication().clientCert().useCNAsPrincipal();
    }

    /**
     * <p>
     * Tests if the authentication is successful when validating the Subject CN from the provided certificate. By default, the
     * {@link HTTPClientCertAuthentication} is configured with useCNAsPrincipal == true.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationUsingCNAsUserName() throws Exception {
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

        UserContext authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(req, resp,
                new HTTPClientCertCredential(req, resp)));

        // mechanism is telling us that we need to continue with the authentication.
        assertNotNull(authenticatedUser);
        Assert.assertFalse(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.CONTINUE);

        // Now set the certificate
        req.setAttribute(PicketBoxConstants.HTTP_CERTIFICATE, new X509Certificate[] { cert });

        authenticatedUser = this.picketBoxManager.authenticate(new HTTPUserContext(req, resp, new HTTPClientCertCredential(req,
                resp)));

        assertNotNull(authenticatedUser);
        Assert.assertTrue(authenticatedUser.isAuthenticated());
        Assert.assertNotNull(authenticatedUser.getAuthenticationResult().getStatus());
        Assert.assertEquals(authenticatedUser.getAuthenticationResult().getStatus(), AuthenticationStatus.SUCCESS);
    }

}