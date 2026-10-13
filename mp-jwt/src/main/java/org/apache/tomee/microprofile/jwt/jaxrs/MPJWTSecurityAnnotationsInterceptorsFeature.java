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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Provider
public class MPJWTSecurityAnnotationsInterceptorsFeature implements DynamicFeature {

    private static final List<Class<? extends Annotation>> SECURITY_ANNOTATIONS =
            Arrays.asList(RolesAllowed.class, PermitAll.class, DenyAll.class);

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

    private MPJWTSecurityAnnotationsInterceptor processSecurityAnnotations(final Class<?> resourceClass, final Method method) {

        if (countSecurityAnnotations(resourceClass) > 1) {
            throw new IllegalStateException(resourceClass.getName() + " has more than one security annotation (RolesAllowed, PermitAll, DenyAll).");
        }

        final int methodSecurityAnnotations = countSecurityAnnotations(method);
        if (methodSecurityAnnotations > 1) {
            throw new IllegalStateException(method.toString() + " has more than one security annotation (RolesAllowed, PermitAll, DenyAll).");
        }

        if (methodSecurityAnnotations == 1) { // method level annotations override class level ones
            return toInterceptor(method);
        }

        final Class<?> constrainingClass = findConstrainingClass(resourceClass, method.getDeclaringClass());
        if (constrainingClass == null) {
            return null; // nothing to do
        }

        if (countSecurityAnnotations(constrainingClass) > 1) {
            throw new IllegalStateException(constrainingClass.getName() + " has more than one security annotation (RolesAllowed, PermitAll, DenyAll).");
        }

        return toInterceptor(constrainingClass);
    }

    /*
     * Security annotations are not @Inherited, so a class level annotation applies to the methods declared by
     * that class. If the declaring class has none, the closest annotated class between the resource class and
     * the declaring class applies, so a subclass can still secure the methods it inherits.
     */
    private Class<?> findConstrainingClass(final Class<?> resourceClass, final Class<?> declaringClass) {
        if (countSecurityAnnotations(declaringClass) > 0) {
            return declaringClass;
        }

        for (Class<?> current = resourceClass;
             current != null && current != declaringClass && current != Object.class;
             current = current.getSuperclass()) {

            if (countSecurityAnnotations(current) > 0) {
                return current;
            }
        }

        return null;
    }

    private MPJWTSecurityAnnotationsInterceptor toInterceptor(final AnnotatedElement element) {
        final RolesAllowed rolesAllowed = element.getAnnotation(RolesAllowed.class);
        final Set<String> roles = rolesAllowed == null ? null : new HashSet<>(Arrays.asList(rolesAllowed.value()));
        return new MPJWTSecurityAnnotationsInterceptor(roles,
                element.isAnnotationPresent(DenyAll.class),
                element.isAnnotationPresent(PermitAll.class));
    }

    private int countSecurityAnnotations(final AnnotatedElement element) {
        int count = 0;
        for (final Class<? extends Annotation> annotation : SECURITY_ANNOTATIONS) {
            if (element.isAnnotationPresent(annotation)) {
                count++;
            }
        }
        return count;
    }

}