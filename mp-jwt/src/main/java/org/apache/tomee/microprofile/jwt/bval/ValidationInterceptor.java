/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.bval;

import org.apache.openejb.util.Logger;
import org.apache.tomee.microprofile.jwt.JWTLogCategories;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.Set;
import java.util.function.Supplier;

public class ValidationInterceptor implements ContainerRequestFilter {

    private static final Logger CONSTRAINT = Logger.getInstance(JWTLogCategories.CONSTRAINT, ValidationInterceptor.class);
    private static final Logger VALIDATION = Logger.getInstance(JWTLogCategories.CONSTRAINT, ValidationInterceptor.class);
    public static final String JWT_SUPPLIER = JsonWebToken.class.getName() + ".Supplier";

    private final ResourceInfo resourceInfo;
    private final ValidationConstraints constraints;

    public ValidationInterceptor(final ResourceInfo resourceInfo, final ValidationConstraints constraints) {
        this.resourceInfo = resourceInfo;
        this.constraints = constraints;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        try {
            final Supplier<JsonWebToken> tokenSupplier = (Supplier<JsonWebToken>) requestContext.getProperty(JWT_SUPPLIER);

            if (tokenSupplier == null) {
                VALIDATION.debug("No JsonWebToken found in request attribute '" + JWT_SUPPLIER + "'");
                return;
            }

            final JsonWebToken jsonWebToken = tokenSupplier.get();

            if (jsonWebToken == null) {
                VALIDATION.error("No JsonWebToken returned from supplier");
                return;
            }

            final String id = jsonWebToken.claim("jti").orElse("<jti missing>") + "";

            final Method resourceMethod = resourceInfo.getResourceMethod();

            final Set<ConstraintViolation<Object>> violations;
            try {
                violations = constraints.validate(resourceMethod, jsonWebToken);
            } catch (Throwable e) {
                VALIDATION.error("Constraint Validation Error: " + e.getMessage(), e);
                throw new ValidationConstraintException(jsonWebToken, e);
            }

            if (violations.size() == 0) {
                VALIDATION.debug("Constraint Validation Passed: " + id);
                return;
            }

            /**
             * If we got this far, validation did not pass and we have issues to report
             * and a request to fail
             */
            for (final ConstraintViolation<Object> violation : violations) {
                final String message = violation.getMessage();
                final Class<? extends Annotation> annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType();
                CONSTRAINT.warning("@" + annotationType.getSimpleName() + ": " + message + " : '" + id + "'");
            }

            VALIDATION.warning("JWT '" + id + "' invalid, " + violations.size() + " constraints failed");
            forbidden(requestContext);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void forbidden(final ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(HttpURLConnection.HTTP_FORBIDDEN).build());
    }
}