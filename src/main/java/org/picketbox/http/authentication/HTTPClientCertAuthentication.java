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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketbox.core.PicketBoxPrincipal;
import org.picketbox.core.authentication.AuthenticationInfo;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.http.config.HTTPAuthenticationConfiguration;
import org.picketbox.http.config.HTTPClientCertConfiguration;
import org.picketlink.idm.model.User;

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
    private boolean useCNAsPrincipal = true;

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.core.authentication.AuthenticationMechanism#getAuthenticationInfo()
     */
    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        List<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("HTTP CLIENT-CERT Authentication Credential",
                "Authenticates users using the HTTP CLIENT-CERT Authentication scheme.", HTTPClientCertCredential.class));

        return info;
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
    protected Principal doHTTPAuthentication(HttpServletRequest request, HttpServletResponse response) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(PicketBoxConstants.HTTP_CERTIFICATE);

        if (certs != null) {
            X509Certificate clientCertificate = certs[0];

            String username = getCertificatePrincipal(clientCertificate).getName();

            if (isUseCNAsPrincipal()) {
                Properties prop = new Properties();
                try {
                    prop.load(new StringReader(username.replaceAll(",", "\n")));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                username = prop.getProperty("CN");
            }

            User user = getIdentityManager().getUser(getCertificatePrincipal(clientCertificate).getName());

            if (user != null) {
                if (isUseCertificateValidation()) {
                    if (getIdentityManager().validateCertificate(user, clientCertificate)) {
                        return new PicketBoxPrincipal(user.getKey());
                    }
                }

                if (isUseCNAsPrincipal()) {
                    return new PicketBoxPrincipal(username);
                }
            }
        }

        return null;
    }

    private Principal getCertificatePrincipal(X509Certificate cert) {
        Principal certprincipal = cert.getSubjectDN();

        if (certprincipal == null) {
            certprincipal = cert.getIssuerDN();
        }
        return certprincipal;
    }

    @Override
    protected void challengeClient(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    }

    public boolean isUseCertificateValidation() {
        HTTPClientCertConfiguration clientCertConfig = getClientCertAuthenticationConfig();

        if (clientCertConfig != null) {
            this.useCertificateValidation = clientCertConfig.isUseCertificateValidation();
        }

        return this.useCertificateValidation;
    }

    private HTTPClientCertConfiguration getClientCertAuthenticationConfig() {
        HTTPAuthenticationConfiguration authenticationConfig = getAuthenticationConfig();

        if (authenticationConfig != null) {
            return authenticationConfig.getClientCertConfiguration();
        }

        return null;
    }

    public boolean isUseCNAsPrincipal() {
        HTTPClientCertConfiguration clientCertConfig = getClientCertAuthenticationConfig();

        if (clientCertConfig != null) {
            this.useCNAsPrincipal = clientCertConfig.isUseCNAsPrincipal();
        }

        return this.useCNAsPrincipal;
    }

}