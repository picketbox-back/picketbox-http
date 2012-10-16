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

package org.picketbox.http.config;

import org.picketbox.core.config.AuthenticationConfiguration;
import org.picketbox.core.config.AuthenticationConfigurationBuilder;
import org.picketbox.core.config.ConfigurationBuilder;
import org.picketbox.http.authentication.HTTPBasicAuthentication;
import org.picketbox.http.authentication.HTTPClientCertAuthentication;
import org.picketbox.http.authentication.HTTPDigestAuthentication;
import org.picketbox.http.authentication.HTTPFormAuthentication;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class HTTPAuthenticationConfigurationBuilder extends AuthenticationConfigurationBuilder {

    private final HTTPClientCertConfigurationBuilder clientCertAuthentication;
    private final HTTPDigestConfigurationBuilder digestAuthentication;
    private HTTPFormConfigurationBuilder formAuthentication;

    public HTTPAuthenticationConfigurationBuilder(ConfigurationBuilder builder) {
        super(builder);
        this.clientCertAuthentication = new HTTPClientCertConfigurationBuilder(builder);
        this.digestAuthentication = new HTTPDigestConfigurationBuilder(builder);
        this.formAuthentication = new HTTPFormConfigurationBuilder(builder);
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.config.AuthenticationConfigurationBuilder#setDefaults()
     */
    @Override
    protected void setDefaults() {
        super.setDefaults();

        // add the defaults http authentication mechanisms
        this.mechanisms.add(new HTTPFormAuthentication());
        this.mechanisms.add(new HTTPDigestAuthentication());
        this.mechanisms.add(new HTTPBasicAuthentication());
        this.mechanisms.add(new HTTPClientCertAuthentication());
    }

    public HTTPClientCertConfigurationBuilder clientCert() {
        return this.clientCertAuthentication;
    }

    public HTTPDigestConfigurationBuilder digest() {
        return this.digestAuthentication;
    }

    public HTTPFormConfigurationBuilder form() {
        return this.formAuthentication;
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.config.AuthenticationConfigurationBuilder#doBuild()
     */
    @Override
    public AuthenticationConfiguration doBuild() {
        return new HTTPAuthenticationConfiguration(this.mechanisms, this.builder.eventManager().build(),
                this.clientCertAuthentication.build(), this.digestAuthentication.build(), this.formAuthentication.build());
    }

}
