/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.jaxrs.springmvc;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.logging.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;

public final class SpringWebUtils {
    private static final Logger LOG = LogUtils.getL7dLogger(SpringViewResolverProvider.class);
    private static final boolean SPRING_WEB_DETECTED;
    
    static {
        if (!Boolean.valueOf(System.getProperty("org.apache.cxf.jaxrs.springmvc.SpringWebUtils.enabled", "true"))) {
            SPRING_WEB_DETECTED = false;
        } else {
            boolean springWebDetected = false;

            try {
                ClassLoaderUtils.loadClass("org.springframework.web.filter.ServerHttpObservationFilter",
                        SpringWebUtils.class);
                springWebDetected = true;
            } catch (final ClassNotFoundException ex) {
                /* Nothing to do, Spring Web is not present */
            }

            SPRING_WEB_DETECTED = springWebDetected;
        }
    }
    
    private SpringWebUtils() {
    }

    public static void setHttpRequestURI(HttpServletRequest request, String uriTemplate) {
        request.setAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern", uriTemplate);

        // Populate the HTTP URI to be available to Spring Boot actuator metrics
        if (SPRING_WEB_DETECTED) {
            // rewritten to use reflection to ensure that if Spring Web is in the application's classloader, it is still called.

            try {
                final Class<?> observationFilterClass = ClassLoaderUtils.loadClass("org.springframework.web.filter.ServerHttpObservationFilter",
                        SpringWebUtils.class);

                final Method findObservationContextMethod = observationFilterClass.getDeclaredMethod("findObservationContext", HttpServletRequest.class);

                final Object result = findObservationContextMethod.invoke(null, request);

                if (result instanceof Optional) {
                    Optional<?> optional = (Optional<?>) result;
                    if (optional.isPresent()) {
                        final Object o = optional.get();

                        final Class<?> observationContextClass = ClassLoaderUtils.loadClass("org.springframework.http.server.observation.ServerRequestObservationContext",
                                SpringWebUtils.class);

                        if (observationContextClass.isInstance(o)) {
                            final Method setPathPatternMethod = observationContextClass.getDeclaredMethod("setPathPattern", String.class);
                            setPathPatternMethod.invoke(o, uriTemplate);
                        }
                    }
                }

            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException e) {
                LOG.warning("Unable to set path pattern on ServerRequestObservationContext: " + e.getMessage());
            }
        }
    }
}
