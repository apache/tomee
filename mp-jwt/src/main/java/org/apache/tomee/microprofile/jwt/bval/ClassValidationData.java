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

import org.apache.bval.jsr.descriptor.ConstraintD;
import org.apache.bval.jsr.job.ConstraintValidators;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassValidationData {

    private final List<MethodConstraints> jwtConstraints = new ArrayList<>();

    private final List<MethodConstraints> returnConstraints = new ArrayList<>();

    public ClassValidationData(final Class<?> clazz) {
        final ConstraintValidators validators = new ConstraintValidators();

        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        final BeanDescriptor descriptor = validator.getConstraintsForClass(clazz);

        for (final Method method : clazz.getMethods()) {
            final MethodDescriptor methodDescriptor = descriptor.getConstraintsForMethod(method.getName(), method.getParameterTypes());

            if (methodDescriptor == null) continue;

            final MethodConstraints jwtAnnotations = new MethodConstraints(method);
            final MethodConstraints returnAnnotations = new MethodConstraints(method);

            final Set<ConstraintD<?>> constraints = cast(methodDescriptor.getReturnValueDescriptor());

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

    public List<MethodConstraints> getJwtConstraints() {
        return jwtConstraints;
    }

    public List<MethodConstraints> getReturnConstraints() {
        return returnConstraints;
    }

    /**
     * Cast to set of ConstraintD
     */
    private static Set<ConstraintD<?>> cast(final ReturnValueDescriptor returnValueDescriptor) {
        final Set<ConstraintDescriptor<?>> descriptors = returnValueDescriptor.getConstraintDescriptors();
        final Set<ConstraintD<?>> constraintDs = new HashSet<ConstraintD<?>>();
        for (final ConstraintDescriptor<?> descriptor : descriptors) {
            constraintDs.add(ConstraintD.class.cast(descriptor));
        }
        return constraintDs;
    }
}
