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
package org.picketbox.test.authentication.http.jetty;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.http.authentication.HTTPBasicAuthentication;
import org.picketbox.http.filters.DelegatingSecurityFilter;
import org.picketbox.test.http.jetty.EmbeddedWebServerBase;

/**
 * Unit test the {@link DelegatingSecurityFilter} for {@link HTTPBasicAuthentication}
 *
 * @author anil saldhana
 * @since Jul 10, 2012
 */
public class DelegatingSecurityFilterHTTPBasicUnitTestCase extends EmbeddedWebServerBase {

    String urlStr = "http://localhost:11080/auth/";

    @Override
    protected void establishUserApps() {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl == null) {
            tcl = getClass().getClassLoader();
        }

        final String WEBAPPDIR = "auth/webapp";

        final String CONTEXTPATH = "/auth";

        // for localhost:port/admin/index.html and whatever else is in the webapp directory
        final URL warUrl = tcl.getResource(WEBAPPDIR);
        final String warUrlString = warUrl.toExternalForm();

        /*
         * Context context = new WebAppContext(warUrlString, CONTEXTPATH); server.setHandler(context);
         *
         * Thread.currentThread().setContextClassLoader(context.getClassLoader());
         */
        WebAppContext webapp = createWebApp(CONTEXTPATH, warUrlString);
        server.setHandler(webapp);

        System.setProperty(PicketBoxConstants.USERNAME, "Aladdin");
        System.setProperty(PicketBoxConstants.CREDENTIAL, "Open Sesame");

        FilterHolder filterHolder = new FilterHolder(DelegatingSecurityFilter.class);
        
        webapp.setInitParameter(PicketBoxConstants.AUTHENTICATION_KEY, PicketBoxConstants.BASIC);
        webapp.setInitParameter(PicketBoxConstants.HTTP_CONFIGURATION_PROVIDER, HTTPDigestConfigurationProvider.class.getName());

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addFilter(filterHolder, createFilterMapping("/", filterHolder));

        webapp.setServletHandler(servletHandler);
    }

    @Test
    public void testBasicAuth() throws Exception {
        URL url = new URL(urlStr);

        DefaultHttpClient httpclient = null;
        try {
            String user = "Aladdin";
            String pass = "Open Sesame";

            httpclient = new DefaultHttpClient();
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),
                    new UsernamePasswordCredentials(user, pass));

            HttpGet httpget = new HttpGet(url.toExternalForm());

            System.out.println("executing request" + httpget.getRequestLine());
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            StatusLine statusLine = response.getStatusLine();
            System.out.println(statusLine);
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
            }
            assertEquals(200, statusLine.getStatusCode());
            EntityUtils.consume(entity);
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }
}