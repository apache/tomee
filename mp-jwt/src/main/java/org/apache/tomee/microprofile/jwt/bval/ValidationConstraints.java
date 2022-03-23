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

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ValidationConstraints {

    private final Map<Method, Method> validationMethods;
    private final ExecutableValidator validator;
    private final Object instance;

    public ValidationConstraints(final Object instance, final Map<Method, Method> validationMethods, final ExecutableValidator validator) {
        this.validationMethods = validationMethods;
        this.validator = validator;
        this.instance = instance;
    }

    public Set<ConstraintViolation<Object>> validate(final Method method, final JsonWebToken jsonWebToken) {
        final Method constraints = validationMethods.get(method);
        if (constraints == null) return Collections.EMPTY_SET;
        return this.validator.validateReturnValue(instance, constraints, jsonWebToken);
    }

    public static ValidationConstraints of(final Class<?> componentClass) {
        final ClassValidationData data = new ClassValidationData(componentClass);

        if (data.getJwtConstraints().size() == 0) return null;

        final Class<?> constraintsClazz = new ClassValidationGenerator(data)
                .generate()
                .stream()
                .filter(aClass -> aClass.getName().endsWith("JwtConstraints"))
                .findFirst()
                .orElseThrow(MissingConstraintsException::new);


        final Map<Method, Method> mapping = new HashMap<>();

        Stream.of(constraintsClazz.getMethods())
                .filter(method -> method.isAnnotationPresent(Generated.class))
                .forEach(method -> mapping.put(resolve(componentClass, method), method)
                );

        final Object instance;
        try {
            instance = constraintsClazz.newInstance();
        } catch (Exception e) {
            throw new ConstraintsClassInstantiationException(constraintsClazz, e);
        }

        final ExecutableValidator executableValidator = Validation.buildDefaultValidatorFactory()
                .getValidator()
                .forExecutables();

        return new ValidationConstraints(instance, mapping, executableValidator);
    }

    private static Method resolve(final Class<?> componentClass, final Method method) {
        try {
            return componentClass.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new MissingConstraintsMethodException(componentClass, method);
        }
    }
}
