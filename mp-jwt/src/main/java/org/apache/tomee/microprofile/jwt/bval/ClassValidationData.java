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

import org.apache.bval.jsr.ApacheValidatorFactory;
import org.apache.bval.jsr.descriptor.ConstraintD;
import org.apache.bval.jsr.job.ConstraintValidators;
import org.apache.bval.jsr.metadata.Meta;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.Validation;
import jakarta.validation.metadata.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClassValidationData {

    private final Class<?> clazz;

    private final List<MethodConstraints> jwtConstraints = new ArrayList<>();

    private final List<MethodConstraints> returnConstraints = new ArrayList<>();

    public ClassValidationData(final Class<?> clazz) {
        this.clazz = clazz;
        final ConstraintValidators validators = new ConstraintValidators();
        final ApacheValidatorFactory factory = ApacheValidatorFactory.class.cast(Validation.buildDefaultValidatorFactory());

        for (final Method method : clazz.getMethods()) {

            final List<ConstraintD<?>> constraints = getConstraints(factory, method);

            if (constraints.size() == 0) continue;

            final MethodConstraints jwtAnnotations = new MethodConstraints(method);
            final MethodConstraints returnAnnotations = new MethodConstraints(method);

            if (method.getReturnType().isAssignableFrom(JsonWebToken.class)) {
                for (final ConstraintD<?> constraint : constraints) {
                    returnAnnotations.add(constraint);
                }
            } else {
                for (final ConstraintD<?> constraint : constraints) {
                    if (validators.canValidate(constraint, JsonWebToken.class)) {
                        jwtAnnotations.add(constraint);
                    } else {
                        returnAnnotations.add(constraint);
                    }
                }
            }

            if (jwtAnnotations.getAnnotations().size() > 0) jwtConstraints.add(jwtAnnotations);
            if (returnAnnotations.getAnnotations().size() > 0) returnConstraints.add(returnAnnotations);
        }
    }

    private static List<ConstraintD<?>> getConstraints(final ApacheValidatorFactory factory, final Method method) {
        final List<ConstraintD<?>> constraints = new ArrayList<>();

        final Meta.ForMethod meta = new Meta.ForMethod(method);
        for (final Annotation annotation : method.getAnnotations()) {
            try {
                final ConstraintD constraint = new ConstraintD(annotation, Scope.LOCAL_ELEMENT, meta, factory);
                constraints.add(constraint);
            } catch (ConstraintDefinitionException e) {
                // ignore
            }
        }
        return constraints;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<MethodConstraints> getJwtConstraints() {
        return jwtConstraints;
    }

    public List<MethodConstraints> getReturnConstraints() {
        return returnConstraints;
    }
}
