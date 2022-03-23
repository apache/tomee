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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Provider
public class MPJWTSecurityAnnotationsInterceptorsFeature implements DynamicFeature {

    private final ConcurrentMap<Method, Set<String>> rolesAllowed = new ConcurrentHashMap<>();
    private final Set<Method> denyAll = new HashSet<>();
    private final Set<Method> permitAll = new HashSet<>();

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext context) {

        final boolean hasSecurity = processSecurityAnnotations(resourceInfo.getResourceClass(), resourceInfo.getResourceMethod());

        if (hasSecurity) { // no need to add interceptor on the resources that don(t have any security requirements to enforce
            context.register(new MPJWTSecurityAnnotationsInterceptor(resourceInfo, rolesAllowed, denyAll, permitAll));
        }

    }

    private boolean processSecurityAnnotations(final Class clazz, final Method method) {

        final List<Class<? extends Annotation>[]> classSecurityAnnotations = hasClassLevelAnnotations(clazz,
                RolesAllowed.class, PermitAll.class, DenyAll.class);

        final List<Class<? extends Annotation>[]> methodSecurityAnnotations = hasMethodLevelAnnotations(method,
                RolesAllowed.class, PermitAll.class, DenyAll.class);

        if (classSecurityAnnotations.isEmpty() && methodSecurityAnnotations.isEmpty()) {
            return false; // nothing to do
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

        if (methodSecurityAnnotations.isEmpty()) { // no need to deal with class level annotations if the method has some
            final RolesAllowed classRolesAllowed = (RolesAllowed) clazz.getAnnotation(RolesAllowed.class);
            final PermitAll classPermitAll = (PermitAll) clazz.getAnnotation(PermitAll.class);
            final DenyAll classDenyAll = (DenyAll) clazz.getAnnotation(DenyAll.class);

            if (classRolesAllowed != null) {
                Set<String> roles = new HashSet<>();
                final Set<String> previous = rolesAllowed.putIfAbsent(method, roles);
                if (previous != null) {
                    roles = previous;
                }
                roles.addAll(Arrays.asList(classRolesAllowed.value()));
            }

            if (classPermitAll != null) {
                permitAll.add(method);
            }

            if (classDenyAll != null) {
                denyAll.add(method);
            }
        }

        final RolesAllowed mthdRolesAllowed = method.getAnnotation(RolesAllowed.class);
        final PermitAll mthdPermitAll = method.getAnnotation(PermitAll.class);
        final DenyAll mthdDenyAll = method.getAnnotation(DenyAll.class);

        if (mthdRolesAllowed != null) {
            Set<String> roles = new HashSet<>();
            final Set<String> previous = rolesAllowed.putIfAbsent(method, roles);
            if (previous != null) {
                roles = previous;
            }
            roles.addAll(Arrays.asList(mthdRolesAllowed.value()));
        }

        if (mthdPermitAll != null) {
            permitAll.add(method);
        }

        if (mthdDenyAll != null) {
            denyAll.add(method);
        }

        return true;
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