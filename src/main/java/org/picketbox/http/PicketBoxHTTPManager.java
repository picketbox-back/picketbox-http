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

package org.picketbox.http;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.picketbox.core.AbstractPicketBoxManager;
import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.PicketBoxSecurityContext;
import org.picketbox.core.PicketBoxSubject;
import org.picketbox.core.authentication.AuthenticationCallbackHandler;
import org.picketbox.core.authentication.PicketBoxConstants;
import org.picketbox.core.authorization.Resource;
import org.picketbox.http.authorization.resource.WebResource;
import org.picketbox.http.config.PicketBoxHTTPConfiguration;
import org.picketbox.http.resource.ProtectedResource;
import org.picketbox.http.resource.ProtectedResourceManager;

/**
 * <p>
 * {@link PicketBoxManager} implementation to be used by web applications.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public final class PicketBoxHTTPManager extends AbstractPicketBoxManager {

    private ProtectedResourceManager protectedResourceManager;
    private PicketBoxHTTPConfiguration configuration;

    public PicketBoxHTTPManager() {

    }

    public PicketBoxHTTPManager(PicketBoxHTTPConfiguration configuration) {
        super(configuration);
        this.configuration = configuration;
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.AbstractPicketBoxManager#doCreateSession(org.picketbox.core.PicketBoxSecurityContext, org.picketbox.core.PicketBoxSubject)
     */
    @Override
    protected PicketBoxHTTPSession doCreateSession(PicketBoxSecurityContext securityContext, PicketBoxSubject resultingSubject) {
        if (!(securityContext instanceof PicketBoxHTTPSecurityContext)) {
            throw new IllegalArgumentException("Wrong security context type. Expected an instance of " + PicketBoxHTTPSecurityContext.class);
        }

        PicketBoxHTTPSecurityContext httpSecurityContext = (PicketBoxHTTPSecurityContext) securityContext;
        HttpSession httpSession = httpSecurityContext.getRequest().getSession(false);

        return new PicketBoxHTTPSession(resultingSubject, httpSession);
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.PicketBoxManager#createSubject(org.picketbox.core.PicketBoxSecurityContext)
     */
    @Override
    public PicketBoxSubject createSubject(PicketBoxSecurityContext securityContext) {
        if (!(securityContext instanceof PicketBoxHTTPSecurityContext)) {
            throw new IllegalArgumentException("Wrong security context type. Expected an instance of " + PicketBoxHTTPSecurityContext.class);
        }

        PicketBoxHTTPSecurityContext httpSecurityContext = (PicketBoxHTTPSecurityContext) securityContext;

        HttpSession session = httpSecurityContext.getRequest().getSession(true);

        PicketBoxSubject subject = null;

        if (session != null) {
            subject = (PicketBoxSubject) session.getAttribute(PicketBoxConstants.SUBJECT);
        }

        if (subject == null) {
            subject = new PicketBoxHTTPSubject();
            session.setAttribute(PicketBoxConstants.SUBJECT, subject);
        }

        return subject;
    }

    /* (non-Javadoc)
     * @see org.picketbox.core.AbstractPicketBoxManager#doPreAuthentication(org.picketbox.core.PicketBoxSecurityContext, org.picketbox.core.authentication.AuthenticationCallbackHandler)
     */
    @Override
    protected boolean doPreAuthentication(PicketBoxSecurityContext securityContext,
            AuthenticationCallbackHandler authenticationCallbackHandler) {
        if (this.protectedResourceManager == null) {
            return true;
        }

        PicketBoxHTTPSecurityContext httpContext = (PicketBoxHTTPSecurityContext) securityContext;

        ProtectedResource protectedResource = this.protectedResourceManager.getProtectedResource(createWebResource(httpContext.getRequest(), httpContext.getResponse()));

        return protectedResource.requiresAuthentication();
    }

    @Override
    public boolean authorize(PicketBoxSubject subject, Resource resource) {
        if (this.protectedResourceManager != null) {
            ProtectedResource protectedResource = this.protectedResourceManager.getProtectedResource(resource);

            if (protectedResource.requiresAuthorization() && subject.isAuthenticated()) {
                if (protectedResource.isAllowed(subject.getRoleNames().toArray(new String[subject.getRoleNames().size()]))) {
                    return true;
                }
            }
        }

        return super.authorize(subject, resource);
    }

    private WebResource createWebResource(HttpServletRequest request, HttpServletResponse response) {
        WebResource resource = new WebResource();

        resource.setContext(request.getServletContext());
        resource.setRequest(request);
        resource.setResponse(response);

        return resource;
    }


    /* (non-Javadoc)
     * @see org.picketbox.core.AbstractPicketBoxManager#doConfigure()
     */
    @Override
    protected void doConfigure() {
        this.protectedResourceManager = this.configuration.getProtectedResource().getManager();
        List<ProtectedResource> resources = this.configuration.getProtectedResource().getResources();

        for (ProtectedResource protectedResource : resources) {
            this.protectedResourceManager.addProtectedResource(protectedResource);
        }

        this.protectedResourceManager.start();
    }
}