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

import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        final Class constraintsClazz = loadOrCreate(componentClass);

        if (constraintsClazz == null) return null;

        final Set<Method> original = OldValidationGenerator.getConstrainedMethods(componentClass);
        final Set<Method> generated = OldValidationGenerator.getConstrainedMethods(constraintsClazz);

        if (original.size() != generated.size()) {
            throw new GeneratedConstraintsMissingException(original, generated);
        }

        final Object instance;
        try {
            instance = constraintsClazz.newInstance();
        } catch (Exception e) {
            throw new ConstraintsClassInstantiationException(constraintsClazz, e);
        }

        final Map<String, Method> names = new HashMap<>();
        for (final Method method : constraintsClazz.getMethods()) {
            final Generated name = method.getAnnotation(Generated.class);
            if (name == null) continue;
            names.put(name.value(), method);
        }

        final Map<Method, Method> validationMethods = new HashMap<>();
        for (final Method method : OldValidationGenerator.getConstrainedMethods(componentClass)) {
            final Method validationMethod = names.get(method.toString());
            validationMethods.put(method, validationMethod);
        }

        final ExecutableValidator executableValidator = Validation.buildDefaultValidatorFactory()
                .getValidator()
                .forExecutables();

        return new ValidationConstraints(instance, validationMethods, executableValidator);
    }

    public static Class loadOrCreate(final Class<?> componentClass) {
        final String constraintsClassName = OldValidationGenerator.getName(componentClass);
        final ClassLoader classLoader = componentClass.getClassLoader();

        try {
            return classLoader.loadClass(constraintsClassName);
        } catch (ClassNotFoundException e) {
            // ok, let's proceed to making it
        }

        final byte[] bytes;
        try {
            bytes = OldValidationGenerator.generateFor(componentClass);
        } catch (ProxyGenerationException e) {
            throw new ValidationGenerationException(componentClass, e);
        }

        if (bytes == null) return null;

        try {
            return LocalBeanProxyFactory.Unsafe.defineClass(classLoader, componentClass, constraintsClassName, bytes);
        } catch (IllegalAccessException e) {
            throw new ValidationGenerationException(componentClass, e);
        } catch (InvocationTargetException e) {
            throw new ValidationGenerationException(componentClass, e.getCause());
        }
    }
}
