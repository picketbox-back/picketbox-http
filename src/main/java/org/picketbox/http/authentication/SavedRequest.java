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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * This class is a representation of the state of a previous {@link HttpServletRequest} instance.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class SavedRequest {

    private List<Cookie> cookies = new ArrayList<Cookie>();
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String[]> parameters = new HashMap<String, String[]>();
    private String method;
    private String queryString;
    private String requestURI;
    private String scheme;
    private String contextPath;

    /**
     * <p>
     * Create a new instance copying the state from the request passed as argument.
     * </p>
     */
    public SavedRequest(HttpServletRequest request) {
        copyCookies(request);
        copyHeaders(request);
        copyParameters(request);

        // copy general properties from the original request
        this.method = request.getMethod();
        this.queryString = request.getQueryString();
        this.requestURI = request.getRequestURI();
        this.scheme = request.getScheme();
        this.contextPath = request.getContextPath();
    }

    /**
     * <p>
     * Returns the parameters copied from the original request.
     * </p>
     */
    public Map<String, String[]> getParameters() {
        return this.parameters;
    }

    /**
     * <p>
     * Returns the headers copied from the original request.
     * </p>
     */
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * <p>
     * Returns the cookies copied from the original request.
     * </p>
     */
    public List<Cookie> getCookies() {
        return this.cookies;
    }

    /**
     * <p>
     * Returns the original HTTP method used by the original request.
     * </p>
     */
    public String getMethod() {
        return method;
    }

    /**
     * <p>
     * Returns the querystring used by the original request.
     * </p>
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * <p>
     * Returns the requestURI used by the original request.
     * </p>
     */
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * <p>
     * Returns the original scheme used by the original request.
     * </p>
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * <p>
     * Returns the original context path used by the original request.
     * </p>
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * <p>
     * Copy the parameters from the original {@link HttpServletRequest}.
     * </p>
     */
    private void copyParameters(HttpServletRequest request) {
        Set<Entry<String, String[]>> parametersEntries = request.getParameterMap().entrySet();

        for (Entry<String, String[]> parameter : parametersEntries) {
            this.getParameters().put(parameter.getKey(), (String[]) parameter.getValue());
        }
    }

    /**
     * <p>
     * Copy the headers from the original {@link HttpServletRequest}.
     * </p>
     */
    private void copyHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            this.getHeaders().put(headerName, headerValue);
        }
    }

    /**
     * <p>
     * Copy the cookies from the original {@link HttpServletRequest}.
     * </p>
     */
    private void copyCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            this.getCookies().add(cookie);
        }
    }
}
