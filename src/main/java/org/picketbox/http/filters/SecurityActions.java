/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketbox.http.filters;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Privileged Blocks
 *
 * @author Anil Saldhana
 * @since Dec 9, 2008
 */
class SecurityActions {

    static ClassLoader getClassLoader(final Class<?> theClass) {
        if (System.getSecurityManager() == null) {
            return theClass.getClassLoader();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return theClass.getClassLoader();
            }
        });
    }

    static Object instance(final Class<?> theClass, final String fqn) {
        try {
            Class<?> clazz = loadClass(theClass, fqn);
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static Class<?> loadClass(final Class<?> theClass, final String fqn) {
        if (System.getSecurityManager() == null) {
            ClassLoader classLoader = theClass.getClassLoader();

            Class<?> clazz = loadClass(classLoader, fqn);
            if (clazz == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
                clazz = loadClass(classLoader, fqn);
            }
            return clazz;
        }

        return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
            public Class<?> run() {
                ClassLoader classLoader = theClass.getClassLoader();

                Class<?> clazz = loadClass(classLoader, fqn);
                if (clazz == null) {
                    classLoader = Thread.currentThread().getContextClassLoader();
                    clazz = loadClass(classLoader, fqn);
                }
                return clazz;
            }
        });
    }

    static Class<?> loadClass(final ClassLoader cl, final String fqn) {
        if (System.getSecurityManager() == null) {
            try {
                return cl.loadClass(fqn);
            } catch (ClassNotFoundException e) {
            }
            return null;
        }
        return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
            public Class<?> run() {
                try {
                    return cl.loadClass(fqn);
                } catch (ClassNotFoundException e) {
                }
                return null;
            }
        });
    }

    /**
     * Get the system property
     *
     * @param key
     * @param defaultValue
     * @return
     */
    static String getSystemProperty(final String key, final String defaultValue) {
        if (System.getSecurityManager() == null) {
            return System.getProperty(key, defaultValue);
        }
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key, defaultValue);
            }
        });
    }
}
