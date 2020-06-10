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
package org.apache.tomee.security;

import javax.el.ELProcessor;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TomEEELInvocationHandler implements InvocationHandler {

    private final Annotation annotation;
    private final ELProcessor processor;

    public TomEEELInvocationHandler(final Annotation annotation, final ELProcessor processor) {
        this.annotation = annotation;
        this.processor = processor;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        // todo optimize and cache methods

        if (method.getName().endsWith("Expression") && method.getReturnType().equals(String.class)) {
            return method.invoke(annotation, args);
        }

        try {
            final Method expressionMethod =
                annotation.getClass().getDeclaredMethod(method.getName() + "Expression", method.getParameterTypes());
            final String expression = (String) expressionMethod.invoke(proxy, args);
            if (expression.isEmpty()) {
                return method.invoke(annotation, args);

            } else {
                return eval(expression, method.getReturnType());
            }

        } catch (final NoSuchMethodException e) {
            return method.invoke(annotation, args);

        } catch (final InvocationTargetException e) {
            throw e.getTargetException();
        }

    }

    private Object eval(final String expression, final Class<?> expectedType) {
        // expression maybe #{expression} instead of ${expression}
        // the ELProcessor anyways wraps it with ${}
        final String sanitizedExpression = expression.replaceAll("^[#$]\\{(.+)}$", "$1");
        return processor.getValue(sanitizedExpression, expectedType);
    }

    public static <T extends Annotation> T of(final Class<T> annotationClass, final T annotation, final BeanManager beanManager) {
        final ELProcessor elProcessor = new ELProcessor();
        elProcessor.getELManager().addELResolver(beanManager.getELResolver());
        return (T) Proxy.newProxyInstance(annotation.getClass().getClassLoader(),
                                          new Class[]{annotationClass},
                                          new TomEEELInvocationHandler(annotation, elProcessor));
    }

    public static <T extends Annotation> T of(final Class<T> annotationClass, final T annotation, final ELProcessor elProcessor) {
        return (T) Proxy.newProxyInstance(annotation.getClass().getClassLoader(),
                                          new Class[]{annotationClass},
                                          new TomEEELInvocationHandler(annotation, elProcessor));
    }

}
