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
package org.picketbox.http.authentication;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.Credential;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.authentication.credential.CertificateCredential;
import org.picketbox.core.authentication.credential.TrustedUsernameCredential;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.http.PicketBoxHTTPManager;

/**
 * Perform HTTP Client Certificate Authentication
 *
 * @author anil saldhana
 * @since July 9, 2012
 */
public class HTTPClientCertAuthentication extends AbstractHTTPAuthentication {

    /**
     * Use Certificate validation directly rather than username/cred model
     */
    protected boolean useCertificateValidation = false;
    private boolean useCNAsPrincipal;

    public HTTPClientCertAuthentication(PicketBoxHTTPManager securityManager) {
        super(securityManager);
    }

    /**
     * Use Certificate validation directly rather than username/cred model. Default is false.
     *
     * @param useCertificateValidation
     */
    public void setUseCertificateValidation(boolean useCertificateValidation) {
        this.useCertificateValidation = useCertificateValidation;
    }

    @Override
    protected boolean isAuthenticationRequest(HttpServletRequest request) {
        return request.getAttribute(PicketBoxConstants.HTTP_CERTIFICATE) != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.http.authentication.AbstractHTTPAuthentication#getAuthenticationCallbackHandler(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected Credential getAuthenticationCallbackHandler(HttpServletRequest request, HttpServletResponse response) {

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(PicketBoxConstants.HTTP_CERTIFICATE);

        if (certs != null) {
            if (useCertificateValidation) {
                return new CertificateCredential(certs);
            }

            for (X509Certificate cert : certs) {
                // Get the username
                Principal certprincipal = cert.getSubjectDN();
                if (certprincipal == null) {
                    certprincipal = cert.getIssuerDN();
                }

                if (certprincipal == null)
                    return null;

                String username = certprincipal.getName();

                if (this.useCNAsPrincipal) {
                    Properties prop = new Properties();
                    try {
                        prop.load(new StringReader(username.replaceAll(",", "\n")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    username = prop.getProperty("CN");
                }

                return new TrustedUsernameCredential(username);
            }
        }
        return null;
    }

    @Override
    protected void challengeClient(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    }

    /**
     * <p>
     * Indicates that the CN from the certificate's subjectDN should be used as the username. The authentication will assume
     * that the certificate was already validated and the username is trusted.
     * </p>
     *
     * @param useCNAsPrincipal
     */
    public void setUseCNAsPrincipal(boolean useCNAsPrincipal) {
        this.useCNAsPrincipal = useCNAsPrincipal;
    }
}