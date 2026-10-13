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
package org.apache.tomee.microprofile.jwt.jaxrs;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Provider
public class MPJWTSecurityAnnotationsInterceptorsFeature implements DynamicFeature {

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext context) {

        // the constraint is resolved per resource class: an inherited resource method is the same
        // java.lang.reflect.Method for every subclass, so it cannot be used as a shared key
        final MPJWTSecurityAnnotationsInterceptor interceptor =
                processSecurityAnnotations(resourceInfo.getResourceClass(), resourceInfo.getResourceMethod());

        if (interceptor != null) { // no need to add interceptor on the resources that don(t have any security requirements to enforce
            context.register(interceptor);
        }

    }

    private MPJWTSecurityAnnotationsInterceptor processSecurityAnnotations(final Class clazz, final Method method) {

        final List<Class<? extends Annotation>[]> classSecurityAnnotations = hasClassLevelAnnotations(clazz,
                RolesAllowed.class, PermitAll.class, DenyAll.class);

        final List<Class<? extends Annotation>[]> methodSecurityAnnotations = hasMethodLevelAnnotations(method,
                RolesAllowed.class, PermitAll.class, DenyAll.class);

        if (classSecurityAnnotations.isEmpty() && methodSecurityAnnotations.isEmpty()) {
            return null; // nothing to do
        }

        /*
         * Process annotations at the class level
         */
        if (classSecurityAnnotations.size() > 1) {
            throw new IllegalStateException(clazz.getName() + " has more than one security annotation (RolesAllowed, PermitAll, DenyAll).");
        }

        if (methodSecurityAnnotations.size() > 1) {
            throw new IllegalStateException(method.toString() + " has more than one security annotation (RolesAllowed, PermitAll, DenyAll).");
        }

        if (!methodSecurityAnnotations.isEmpty()) { // method level annotations override class level ones
            return toInterceptor(method.getAnnotation(RolesAllowed.class),
                    method.isAnnotationPresent(DenyAll.class),
                    method.isAnnotationPresent(PermitAll.class));
        }

        return toInterceptor((RolesAllowed) clazz.getAnnotation(RolesAllowed.class),
                clazz.isAnnotationPresent(DenyAll.class),
                clazz.isAnnotationPresent(PermitAll.class));
    }

    private MPJWTSecurityAnnotationsInterceptor toInterceptor(final RolesAllowed rolesAllowed,
                                                              final boolean denyAll,
                                                              final boolean permitAll) {
        final Set<String> roles = rolesAllowed == null ? null : new HashSet<>(Arrays.asList(rolesAllowed.value()));
        return new MPJWTSecurityAnnotationsInterceptor(roles, denyAll, permitAll);
    }

    private List<Class<? extends Annotation>[]> hasClassLevelAnnotations(final Class clazz, final Class<? extends Annotation>... annotationsToCheck) {
        final List<Class<? extends Annotation>[]> list = new ArrayList<>();
        for (Class<? extends Annotation> annotationToCheck : annotationsToCheck) {
            if (clazz.isAnnotationPresent(annotationToCheck)) {
                list.add(annotationsToCheck);
            }
        }
        return list;
    }

    private List<Class<? extends Annotation>[]> hasMethodLevelAnnotations(final Method method, final Class<? extends Annotation>... annotationsToCheck) {
        final List<Class<? extends Annotation>[]> list = new ArrayList<>();
        for (Class<? extends Annotation> annotationToCheck : annotationsToCheck) {
            if (method.isAnnotationPresent(annotationToCheck)) {
                list.add(annotationsToCheck);
            }
        }
        return list;
    }

}