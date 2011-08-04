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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.bval;

import org.apache.openejb.core.ivm.naming.NamingException;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**
 * A simple interceptor to validate parameters and returned value using
 * bean validation spec. It doesn't use group for now.
 *
 * @author Romain Manni-Bucau
 */
public class BeanValidationAppendixInterceptor {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, BeanValidationAppendixInterceptor.class);
    private static final Class<?> APACHE_BVAL_METHOD_CLASS = initApache();
    private static final Class<?> HIBERNATE_METHOD_CLASS = initHibernate();

    private SessionContext sessionContext;

    @AroundInvoke public Object aroundInvoke(final InvocationContext ejbContext) throws Exception {
        Object validatorObject = null;
        Validator validator = null;
        try {
            validator = (Validator) new InitialContext().lookup("java:comp/Validator");
            sessionContext = (SessionContext) new InitialContext().lookup("java:comp/EJBContext"); // injection doesn't work
        } catch (NamingException ne) {
            // no-op
        }

        // get bval annotation informations
        Class<?> bvalClazzToValidate = ejbContext.getTarget().getClass();
        if (sessionContext != null && ejbContext.getTarget().getClass().getInterfaces().length > 0) {
            bvalClazzToValidate = sessionContext.getInvokedBusinessInterface();
        }
        Method method = ejbContext.getMethod();
        if (!bvalClazzToValidate.equals(ejbContext.getTarget().getClass())) {
            method = bvalClazzToValidate.getMethod(method.getName(), method.getParameterTypes());
        }

        Set<?> violations = Collections.emptySet();
        if (APACHE_BVAL_METHOD_CLASS != null && validator != null) {
            validatorObject = validator.unwrap(APACHE_BVAL_METHOD_CLASS);
            violations = call(Set.class, validatorObject, "validateParameters",
                new Object[]{
                    bvalClazzToValidate, method, ejbContext.getParameters(), new Class[0]
                },
                new Class<?>[]{
                    Class.class, Method.class, Object[].class, Class[].class
                });
        } else if (HIBERNATE_METHOD_CLASS != null && validator != null) {
            validatorObject = validator.unwrap(HIBERNATE_METHOD_CLASS);
            violations = call(Set.class, validatorObject, "validateAllParameters",
                new Object[]{
                    ejbContext.getTarget(), ejbContext.getMethod(), ejbContext.getParameters(), new Class[0]
                },
                new Class<?>[]{
                    Object.class, Method.class, Object[].class, Class[].class
                });
        } else { // a warning message to inform Apache Bean Validation is not present
            if (validator == null) {
                logger.error("can't find validator");
            } else {
                logger.warning("Apache Bean Validation is not present, "
                    + BeanValidationAppendixInterceptor.class.getName() + " will not work. "
                    + "Please put it if you want to validate your parameters and returned values "
                    + "with bean validation JSR.");
            }
        }

        if (violations.size() > 0) {
            throw buildValidationException((Set<ConstraintViolation<?>>) violations);
        }

        Object returnedValue = ejbContext.proceed();

        violations = Collections.emptySet();
        if (validatorObject != null && APACHE_BVAL_METHOD_CLASS != null) {
            violations = call(Set.class, validatorObject, "validateReturnedValue",
                new Object[]{
                    bvalClazzToValidate, method, returnedValue, new Class[0]
                },
                new Class<?>[]{
                    Class.class, Method.class, Object.class, Class[].class
                });
        } else if (validatorObject != null && HIBERNATE_METHOD_CLASS != null) {
            violations = call(Set.class, validatorObject, "validateReturnValue",
                new Object[]{
                    ejbContext.getTarget(), ejbContext.getMethod(), returnedValue, new Class[0]
                },
                new Class<?>[]{
                    Object.class, Method.class, Object.class, Class[].class
                });
        }

        if (violations.size() > 0) {
            throw buildValidationException((Set<ConstraintViolation<?>>) violations);
        }

        return returnedValue;
    }

    // just a simple EJBException for now
    private RuntimeException buildValidationException(Set<ConstraintViolation<?>> violations) {
        return new ConstraintViolationException(violations);
    }

    private static <T> T call(Class<T> returnedType, Object o, String methodName, Object[] params, Class<?>[] types) {
        Method method = null;
        boolean accessible = true;
        try {
            method = o.getClass().getMethod(methodName, types);
            accessible = method.isAccessible();
            if (!accessible) {
                accessible = false;
                method.setAccessible(true);
            }
            return returnedType.cast(method.invoke(o, params));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException("can't call method " + methodName + " on " + o, e);
        } finally {
            if (method != null) {
                method.setAccessible(accessible);
            }
        }
    }

    private static ClassLoader getClassLaoder() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = BeanValidationAppendixInterceptor.class.getClassLoader();
        }
        return classLoader;
    }

    private static Class<?> initApache() {
        try {
            return getClassLaoder().loadClass("org.apache.bval.jsr303.extensions.MethodValidator");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Class<?> initHibernate() {
        try {
            return getClassLaoder().loadClass("org.hibernate.validator.method.MethodValidator");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}

