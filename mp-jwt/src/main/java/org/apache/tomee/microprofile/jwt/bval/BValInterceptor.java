/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tomee.microprofile.jwt.bval;

import org.apache.bval.cdi.BValBinding;
import org.apache.bval.cdi.BValExtension;
import org.apache.bval.jsr.descriptor.DescriptorManager;
import org.apache.bval.jsr.metadata.Signature;
import org.apache.bval.jsr.util.ExecutableTypes;
import org.apache.bval.jsr.util.Methods;
import org.apache.bval.jsr.util.Proxies;
import org.apache.bval.util.ObjectUtils;
import org.apache.bval.util.Validate;
import org.apache.bval.util.reflection.Reflection;
import org.apache.bval.util.reflection.Reflection.Interfaces;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.executable.ValidateOnExecution;
import jakarta.validation.metadata.ConstructorDescriptor;
import jakarta.validation.metadata.MethodDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiPredicate;

/**
 * This interceptor is a specialized version of the org.apache.bval.cdi.BValInterceptor
 *
 * To support the JWT+BeanValidation feature we will split any JWT-validating annotations
 * into a dedicated class with a '$$JwtConstraints' suffix, and the other annotations into a
 * dedicated class with a '$$ReturnConstraints' suffix.
 *
 * For the purposes of parameter and return value validation, we use only the regular class
 * or, if present, the '$$ReturnConstraints' suffixed class.
 *
 * Interceptor class for the {@link BValBinding} {@link InterceptorBinding}.
 */
@SuppressWarnings("serial")
@Interceptor
@BValBinding
@Priority(4800)
// TODO: maybe add it through ASM to be compliant with CDI 1.0 containers using simply this class as a template to
// generate another one for CDI 1.1 impl
public class BValInterceptor implements Serializable {
    private static Collection<ExecutableType> removeFrom(Collection<ExecutableType> coll,
                                                         ExecutableType... executableTypes) {
        Validate.notNull(coll, "collection was null");
        if (!(coll.isEmpty() || ObjectUtils.isEmptyArray(executableTypes))) {
            final List<ExecutableType> toRemove = Arrays.asList(executableTypes);
            if (!Collections.disjoint(coll, toRemove)) {
                final Set<ExecutableType> result = EnumSet.copyOf(coll);
                result.removeAll(toRemove);
                return result;
            }
        }
        return coll;
    }

    private transient volatile Set<ExecutableType> classConfiguration;
    private transient volatile Map<Signature, Boolean> executableValidation;

    @Inject
    private Validator validator;

    @Inject
    private BValExtension globalConfiguration;

    private transient volatile ExecutableValidator executableValidator;
    private transient volatile ConcurrentMap<Class<?>, Class<?>> classMapping;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @AroundConstruct // TODO: see previous one
    public Object construct(final InvocationContext context) throws Exception {

        final Constructor ctor = context.getConstructor();
        if (!isConstructorValidated(ctor)) {
            return context.proceed();
        }
        final Class clazz = ctor.getDeclaringClass();

        final ClassValidationData validationData = new ClassValidationData(clazz);
        if (validationData.getJwtConstraints().size() > 0) {
            // Temporary measure as currently bval will fail if there are JWT constraints
            // anywhere on the class
            return context.proceed();
        }

        final ConstructorDescriptor constraints = validator.getConstraintsForClass(clazz)
                .getConstraintsForConstructor(ctor.getParameterTypes());

        if (!DescriptorManager.isConstrained(constraints)) {
            return context.proceed();
        }
        initExecutableValidator();

        if (constraints.hasConstrainedParameters()) {
            final Set<ConstraintViolation<?>> violations =
                    executableValidator.validateConstructorParameters(ctor, context.getParameters());
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        final Object result = context.proceed();

        if (constraints.hasConstrainedReturnValue()) {
            final Set<ConstraintViolation<?>> violations =
                    executableValidator.validateConstructorReturnValue(ctor, context.getTarget());
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        return result;
    }

    @AroundInvoke
    public Object invoke(final InvocationContext context) throws Exception {
        Method method = context.getMethod();
        Class<?> targetClass = getTargetClass(context);

        final ClassValidationData validationData = new ClassValidationData(targetClass);
        if (validationData.getJwtConstraints().size() > 0) {
            final Class<?> constraintsClazz = new ClassValidationGenerator(validationData)
                    .generate()
                    .stream()
                    .filter(aClass -> aClass.getName().endsWith("ReturnConstraints"))
                    .findFirst()
                    .orElseThrow(MissingConstraintsException::new);

            try {
                final Method constraintsClazzMethod = constraintsClazz.getMethod(method.getName(), method.getParameterTypes());
                targetClass = constraintsClazz;
                method = constraintsClazzMethod;
            } catch (NoSuchMethodException | SecurityException e) {
                // this is ok.  it means there are no return value constraints
                return context.proceed();
            }
        }

        if (!isExecutableValidated(targetClass, method, this::computeIsMethodValidated)) {
            return context.proceed();
        }

        final MethodDescriptor constraintsForMethod = validator.getConstraintsForClass(targetClass)
                .getConstraintsForMethod(method.getName(), method.getParameterTypes());

        if (!DescriptorManager.isConstrained(constraintsForMethod)) {
            return context.proceed();
        }
        initExecutableValidator();

        if (constraintsForMethod.hasConstrainedParameters()) {
            final Set<ConstraintViolation<Object>> violations =
                    executableValidator.validateParameters(context.getTarget(), method, context.getParameters());
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        final Object result = context.proceed();

        if (constraintsForMethod.hasConstrainedReturnValue()) {
            final Set<ConstraintViolation<Object>> violations =
                    executableValidator.validateReturnValue(context.getTarget(), method, result);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        return result;
    }

    private Class<?> getTargetClass(final InvocationContext context) {
        final Class<?> key = context.getTarget().getClass();
        if (classMapping == null) {
            synchronized (this) {
                if (classMapping == null) {
                    classMapping = new ConcurrentHashMap<>();
                }
            }
        }
        Class<?> mapped = classMapping.get(key);
        if (mapped == null) {
            mapped = Proxies.classFor(key);
            classMapping.putIfAbsent(key, mapped);
        }
        return mapped;
    }

    private <T> boolean isConstructorValidated(final Constructor<T> constructor) {
        return isExecutableValidated(constructor.getDeclaringClass(), constructor, this::computeIsConstructorValidated);
    }

    private <T, E extends Executable> boolean isExecutableValidated(final Class<T> targetClass, final E executable,
                                                                    BiPredicate<? super Class<T>, ? super E> compute) {
        initClassConfig(targetClass);

        if (executableValidation == null) {
            synchronized (this) {
                if (executableValidation == null) {
                    executableValidation = new ConcurrentHashMap<>();
                }
            }
        }
        return executableValidation.computeIfAbsent(Signature.of(executable),
                s -> compute.test(targetClass, executable));
    }

    private void initClassConfig(Class<?> targetClass) {
        if (classConfiguration == null) {
            synchronized (this) {
                if (classConfiguration == null) {
                    final AnnotatedType<?> annotatedType = CDI.current().getBeanManager()
                            .createAnnotatedType(targetClass);

                    if (annotatedType.isAnnotationPresent(ValidateOnExecution.class)) {
                        // implicit does not apply at the class level:
                        classConfiguration = ExecutableTypes.interpret(
                                removeFrom(Arrays.asList(annotatedType.getAnnotation(ValidateOnExecution.class).type()),
                                        ExecutableType.IMPLICIT));
                    } else {
                        classConfiguration = globalConfiguration.getGlobalExecutableTypes();
                    }
                }
            }
        }
    }

    private <T> boolean computeIsConstructorValidated(Class<T> targetClass, Constructor<T> ctor) {
        final AnnotatedType<T> annotatedType =
                CDI.current().getBeanManager().createAnnotatedType(ctor.getDeclaringClass());

        final ValidateOnExecution annotation =
                annotatedType.getConstructors().stream().filter(ac -> ctor.equals(ac.getJavaMember())).findFirst()
                        .map(ac -> ac.getAnnotation(ValidateOnExecution.class))
                        .orElseGet(() -> ctor.getAnnotation(ValidateOnExecution.class));

        final Set<ExecutableType> validatedExecutableTypes =
                annotation == null ? classConfiguration : ExecutableTypes.interpret(annotation.type());

        return validatedExecutableTypes.contains(ExecutableType.CONSTRUCTORS);
    }

    private <T> boolean computeIsMethodValidated(Class<T> targetClass, Method method) {
        final Signature signature = Signature.of(method);

        AnnotatedMethod<?> declaringMethod = null;

        for (final Class<?> c : Reflection.hierarchy(targetClass, Interfaces.INCLUDE)) {
            final AnnotatedType<?> annotatedType = CDI.current().getBeanManager().createAnnotatedType(c);

            final AnnotatedMethod<?> annotatedMethod = annotatedType.getMethods().stream()
                    .filter(am -> Signature.of(am.getJavaMember()).equals(signature)).findFirst().orElse(null);

            if (annotatedMethod != null) {
                declaringMethod = annotatedMethod;
            }
        }
        if (declaringMethod == null) {
            return false;
        }
        final Collection<ExecutableType> declaredExecutableTypes;

        if (declaringMethod.isAnnotationPresent(ValidateOnExecution.class)) {
            final List<ExecutableType> validatedTypesOnMethod =
                    Arrays.asList(declaringMethod.getAnnotation(ValidateOnExecution.class).type());

            // implicit directly on method -> early return:
            if (validatedTypesOnMethod.contains(ExecutableType.IMPLICIT)) {
                return true;
            }
            declaredExecutableTypes = validatedTypesOnMethod;
        } else {
            final AnnotatedType<?> declaringType = declaringMethod.getDeclaringType();
            if (declaringType.isAnnotationPresent(ValidateOnExecution.class)) {
                // IMPLICIT is meaningless at class level:
                declaredExecutableTypes =
                        removeFrom(Arrays.asList(declaringType.getAnnotation(ValidateOnExecution.class).type()),
                                ExecutableType.IMPLICIT);
            } else {
                final Package pkg = declaringType.getJavaClass().getPackage();
                if (pkg != null && pkg.isAnnotationPresent(ValidateOnExecution.class)) {
                    // presumably IMPLICIT is likewise meaningless at package level:
                    declaredExecutableTypes = removeFrom(
                            Arrays.asList(pkg.getAnnotation(ValidateOnExecution.class).type()), ExecutableType.IMPLICIT);
                } else {
                    declaredExecutableTypes = null;
                }
            }
        }
        final ExecutableType methodType =
                Methods.isGetter(method) ? ExecutableType.GETTER_METHODS : ExecutableType.NON_GETTER_METHODS;

        return Optional.ofNullable(declaredExecutableTypes).map(ExecutableTypes::interpret)
                .orElse(globalConfiguration.getGlobalExecutableTypes()).contains(methodType);
    }

    private void initExecutableValidator() {
        if (executableValidator == null) {
            synchronized (this) {
                if (executableValidator == null) {
                    executableValidator = validator.forExecutables();
                }
            }
        }
    }
}
