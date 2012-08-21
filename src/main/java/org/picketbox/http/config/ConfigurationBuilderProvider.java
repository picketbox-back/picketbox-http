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

import javax.servlet.ServletContext;

import org.picketbox.http.PicketBoxHTTPManager;

/**
 * <p>
 * This interfaces can be implemented to provide a {@link HTTPConfigurationBuilder} that will be used to configure and create
 * the {@link PicketBoxHTTPManager}.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public interface ConfigurationBuilderProvider {

    /**
     * <p>
     * Returns a {@link HTTPConfigurationBuilder} instance to be used to build the configuration. You can use the
     * {@link ServletContext} param to get informations about your application context.
     * </p>
     *
     * @param sc
     *
     * @return
     */
    HTTPConfigurationBuilder getBuilder(ServletContext servletcontext);

}
