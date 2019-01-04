/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.principal;

import org.apache.tomee.microprofile.jwt.ParseException;
import org.apache.tomee.microprofile.jwt.config.JWTAuthContextInfo;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * The factory class that provides the token string to JWTCallerPrincipal parsing for a given implementation.
 */
public abstract class JWTCallerPrincipalFactory {

    private static JWTCallerPrincipalFactory instance;
    private static final Logger logger = Logger.getLogger(JWTCallerPrincipalFactory.class.getName());
    /**
     * Obtain the JWTCallerPrincipalFactory that has been set or by using the ServiceLoader pattern.
     *
     * @return the factory instance
     * @see #setInstance(JWTCallerPrincipalFactory)
     */
    public static JWTCallerPrincipalFactory instance() {
        if (instance == null) {
            synchronized (JWTCallerPrincipalFactory.class) {
                if (instance != null) {
                    return instance;
                }

                ClassLoader cl = AccessController.doPrivileged(
                        (PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader()
                );
                if (cl == null) {
                    cl = JWTCallerPrincipalFactory.class.getClassLoader();
                }

                JWTCallerPrincipalFactory newInstance = loadSpi(cl);

                if (newInstance == null && cl != JWTCallerPrincipalFactory.class.getClassLoader()) {
                    cl = JWTCallerPrincipalFactory.class.getClassLoader();
                    newInstance = loadSpi(cl);
                }
                if (newInstance == null) {
                    throw new IllegalStateException("No JWTCallerPrincipalFactory implementation found!");
                }

                instance = newInstance;
            }
        }

        return instance;
    }

    /**
     * Look for a JWTCallerPrincipalFactory service implementation using the ServiceLoader.
     *
     * @param cl - the ClassLoader to pass into the {@link ServiceLoader#load(Class, ClassLoader)} method.
     * @return the JWTCallerPrincipalFactory if found, null otherwise
     */
    private static JWTCallerPrincipalFactory loadSpi(ClassLoader cl) {
        if (cl == null) {
            return null;
        }

        // start from the root CL and go back down to the TCCL
        JWTCallerPrincipalFactory instance = loadSpi(cl.getParent());

        if (instance == null) {
            ServiceLoader<JWTCallerPrincipalFactory> sl = ServiceLoader.load(JWTCallerPrincipalFactory.class, cl);
            URL u = cl.getResource("/META-INF/services/org.apache.tomee.microprofile.jwt.JWTCallerPrincipalFactory");
            logger.info(String.format("JWTCallerPrincipalFactory, cl=%s, u=%s, sl=%s", cl, u, sl));

            try {
                for (JWTCallerPrincipalFactory spi : sl) {
                    if (instance != null) {
                        throw new IllegalStateException(
                                "Multiple JWTCallerPrincipalFactory implementations found: "
                                        + spi.getClass().getName() + " and "
                                        + instance.getClass().getName());
                    } else {
                        logger.info(String.format("sl=%s, loaded=%s", sl, spi));
                        instance = spi;
                    }
                }

            } catch (final Throwable e) {
                logger.warning(String.format("Warning: %s", e.getMessage()));
            }
        }
        return instance;
    }

    /**
     * Set the instance. It is used by OSGi environment where service loader pattern is not supported.
     *
     * @param resolver the instance to use.
     */
    public static void setInstance(final JWTCallerPrincipalFactory resolver) {
        instance = resolver;
    }

    /**
     * Parse the given bearer token string into a JWTCallerPrincipal instance.
     *
     * @param token - the bearer token provided for authorization
     * @return A JWTCallerPrincipal representation for the token.
     * @throws ParseException on parse or verification failure.
     */
    public abstract JWTCallerPrincipal parse(final String token, final JWTAuthContextInfo authContextInfo) throws ParseException;
}