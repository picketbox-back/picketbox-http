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
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.picketbox.core.PicketBoxPrincipal;
import org.picketbox.core.authentication.AuthenticationInfo;
import org.picketbox.core.authentication.DigestHolder;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.exceptions.AuthenticationException;
import org.picketbox.core.exceptions.FormatException;
import org.picketbox.core.nonce.NonceGenerator;
import org.picketbox.core.nonce.UUIDNonceGenerator;
import org.picketbox.core.util.HTTPDigestUtil;
import org.picketbox.http.config.HTTPAuthenticationConfiguration;
import org.picketbox.http.config.HTTPDigestConfiguration;
import org.picketlink.idm.model.User;
import org.picketlink.idm.password.PasswordValidator;

/**
 * Class that handles HTTP/Digest Authentication
 *
 * @author anil saldhana
 * @since Jul 6, 2012
 */
public class HTTPDigestAuthentication extends AbstractHTTPAuthentication {

    protected String opaque = UUID.randomUUID().toString();

    protected String qop = PicketBoxConstants.HTTP_DIGEST_QOP_AUTH;

    // How long is the nonce valid? By default, it is set at 3 minutes
    protected long nonceMaxValid = 3 * 60 * 1000;

    protected NonceGenerator nonceGenerator = new UUIDNonceGenerator();

    /**
     * A simple lookup map of session id versus the nonces issued
     */
    protected ConcurrentMap<String, List<String>> idVersusNonce = new ConcurrentHashMap<String, List<String>>();

    /* (non-Javadoc)
     * @see org.picketbox.core.authentication.AuthenticationMechanism#getAuthenticationInfo()
     */
    @Override
    public List<AuthenticationInfo> getAuthenticationInfo() {
        List<AuthenticationInfo> info = new ArrayList<AuthenticationInfo>();

        info.add(new AuthenticationInfo("HTTP DIGEST Authentication Credential", "Authenticates users using the HTTP DIGEST Authentication scheme.", HTTPDigestCredential.class));

        return info;
    }

    public NonceGenerator getNonceGenerator() {
        return this.nonceGenerator;
    }

    public void setNonceGenerator(NonceGenerator nonceGenerator) {
        this.nonceGenerator = nonceGenerator;
    }

    public void setNonceMaxValid(String nonceMaxValidStr) {
        this.nonceMaxValid = Long.parseLong(nonceMaxValidStr);
    }

    public String getOpaque() {
        HTTPAuthenticationConfiguration authenticationConfig = getAuthenticationConfig();

        if (authenticationConfig != null) {
            HTTPDigestConfiguration digestConfiguration = authenticationConfig.getDigestConfiguration();

            if (digestConfiguration != null && digestConfiguration.getOpaque() != null) {
                this.opaque = digestConfiguration.getOpaque();
            }
        }

        return this.opaque;
    }

    public void setOpaque(String opaque) {
        this.opaque = opaque;
    }

    private static enum NONCE_VALIDATION_RESULT {
        INVALID, STALE, VALID
    }

    private NONCE_VALIDATION_RESULT validateNonce(DigestHolder digest, String sessionId) {
        String nonce = digest.getNonce();

        List<String> storedNonces = this.idVersusNonce.get(sessionId);
        if (storedNonces == null) {
            return NONCE_VALIDATION_RESULT.INVALID;
        }
        if (storedNonces.contains(nonce) == false) {
            return NONCE_VALIDATION_RESULT.INVALID;
        }

        boolean hasExpired = this.nonceGenerator.hasExpired(nonce, this.nonceMaxValid);
        if (hasExpired)
            return NONCE_VALIDATION_RESULT.STALE;

        return NONCE_VALIDATION_RESULT.VALID;
    }

    @Override
    protected boolean isAuthenticationRequest(HttpServletRequest request) {
        return request.getHeader(PicketBoxConstants.HTTP_AUTHORIZATION_HEADER) != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketbox.http.authentication.AbstractHTTPAuthentication#getAuthenticationCallbackHandler(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected Principal doHTTPAuthentication(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();

        // Get the Authorization Header
        String authorizationHeader = request.getHeader(PicketBoxConstants.HTTP_AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.isEmpty() == false) {

            if (authorizationHeader.startsWith(PicketBoxConstants.HTTP_DIGEST)) {
                authorizationHeader = authorizationHeader.substring(7).trim();
            }
            String[] tokens = HTTPDigestUtil.quoteTokenize(authorizationHeader);

            int len = tokens.length;
            if (len == 0) {
                return null;
            }

            final DigestHolder digest = HTTPDigestUtil.digest(tokens);

            // Pre-verify the client response
            if (digest.getUsername() == null || digest.getRealm() == null || digest.getNonce() == null
                    || digest.getUri() == null || digest.getClientResponse() == null) {
                return null;
            }

            // Validate Opaque
            if (digest.getOpaque() != null && digest.getOpaque().equals(getOpaque()) == false) {
                return null;
            }

            // Validate realm
            if (digest.getRealm().equals(getRealmName()) == false) {
                return null;
            }

            // Validate qop
            if (digest.getQop().equals(this.qop) == false) {
                return null;
            }

            digest.setRequestMethod(request.getMethod());

            // Validate the nonce
            NONCE_VALIDATION_RESULT nonceResult = validateNonce(digest, sessionId);

            if (nonceResult == NONCE_VALIDATION_RESULT.VALID) {
                User user = getIdentityManager().getUser(digest.getUsername());

                if (user != null) {
                    if (getIdentityManager().validatePassword(user, new PasswordValidator() {

                        @Override
                        public boolean validate(String userPassword) {
                            try {
                                return HTTPDigestUtil.matchCredential(digest, userPassword.toCharArray());
                            } catch (FormatException e) {
                                throw new RuntimeException("Error validating digest credential.", e);
                            }
                        }

                    })) {
                        return new PicketBoxPrincipal(digest.getUsername());
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected void challengeClient(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();

        String domain = request.getContextPath();
        if (domain == null)
            domain = "/";

        String newNonce = this.nonceGenerator.get();

        List<String> storedNonces = this.idVersusNonce.get(sessionId);
        if (storedNonces == null) {
            storedNonces = new ArrayList<String>();
            this.idVersusNonce.put(sessionId, storedNonces);
        }
        storedNonces.add(newNonce);

        StringBuilder str = new StringBuilder("Digest realm=\"");
        str.append(getRealmName()).append("\",");
        str.append("domain=\"").append(domain).append("\",");
        str.append("nonce=\"").append(newNonce).append("\",");
        str.append("algorithm=MD5,");
        str.append("qop=").append(this.qop).append(",");
        str.append("opaque=\"").append(getOpaque()).append("\",");
        str.append("stale=\"").append(false).append("\"");

        response.setHeader(PicketBoxConstants.HTTP_WWW_AUTHENTICATE, str.toString());

        try {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.picketbox.http.authentication.AbstractHTTPAuthentication#getRealmName()
     */
    @Override
    public String getRealmName() {
        HTTPAuthenticationConfiguration authenticationConfig = getAuthenticationConfig();

        if (authenticationConfig != null) {
            HTTPDigestConfiguration digestConfiguration = authenticationConfig.getDigestConfiguration();

            if (digestConfiguration != null && digestConfiguration.getRealm() != null) {
                this.realmName = digestConfiguration.getRealm();
            }
        }

        return this.realmName;
    }
}