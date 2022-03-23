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

import jakarta.el.ELProcessor;
import jakarta.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomEEELInvocationHandler implements InvocationHandler {

    private static final Pattern EL_EXPRESSION_PATTERN = Pattern.compile("^[#$]\\{(.+)}$");

    private final Annotation annotation;
    private final ELProcessor processor;

    public TomEEELInvocationHandler(final Annotation annotation, final ELProcessor processor) {
        this.annotation = annotation;
        this.processor = processor;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        // todo optimize and cache methods

        // avoid stack overflow because of infinite loop (See bellow)
        // if method is already the expression one with a String return type, then invoke it and return the result
        // so we can evaluate the EL and return the evaluated value
        if (method.getName().endsWith("Expression") && method.getReturnType().equals(String.class)) {
            return method.invoke(annotation, args);
        }

        // If return value is not a String or an array of string, there is another method with "Expression" at the end and a return type String
        if (!method.getReturnType().equals(String.class)) {
            try {
                // try to find the equivalent expression method
                final Method expressionMethod =
                    annotation.getClass()
                              .getDeclaredMethod(method.getName() + "Expression", method.getParameterTypes());

                // great, let's get the EL value
                // we could check the return value to make sure it's a string,
                // but let's assume people writing annotation apis aren't stupid
                final String expression = (String) expressionMethod.invoke(proxy, args);

                // if there is an expression, it takes precedence over the static one
                if (!expression.trim().isEmpty()) {
                    // make sure to pass in the return type of the initial method, otherwith it would be String all the time
                    return eval(processor, sanitizeExpression(expression), method.getReturnType());

                } else {
                    return method.invoke(annotation, args);
                }

            } catch (final NoSuchMethodException e) {
                // from spec it is required for a new String to have an equivalent
                // if not, keep going with the initial method invocation
                return method.invoke(annotation, args);

            } catch (final InvocationTargetException e) { // unwrap the invocation target exception so we get the actual error
                throw e.getTargetException();
            }
        }

        // if the return type is a String, we may always get an expression to evaluate.
        // check if it's something we can evaluate
        final String value = (String) method.invoke(annotation, args);
        if (value != null && value.length() > 3) {
            final String sanitizedExpression = sanitizeExpression(value);
            if (!value.equals(sanitizedExpression)) {
                return eval(processor, sanitizedExpression, method.getReturnType());
            }
        }

        return value;
    }

    // following should be abstracted into a wrapper of the ELProcessor utility class

    public static boolean isExpression(final String rawExpression) {
        final Matcher matcher = EL_EXPRESSION_PATTERN.matcher(rawExpression);
        return matcher.matches();
    }

    public static String sanitizeExpression(final String rawExpression) {
        final Matcher matcher = EL_EXPRESSION_PATTERN.matcher(rawExpression);

        if (!matcher.matches()) {
            return rawExpression;
        }

        return matcher.replaceAll("$1");
    }

    public static Object eval(final ELProcessor processor, final String sanitizedExpression, final Class<?> expectedType) {
        // ELProcessor does not do a good job with enums, so try to be a bit better (not sure)
        // otherwise, let the EL processor do its best to convert into the expected value
        if (!isEnumOrArrayOfEnums(expectedType)) {
            return processor.getValue(sanitizedExpression, expectedType);
        }

        final Object value = processor.getValue(sanitizedExpression, Object.class);

        // Convert single enum name to single enum
        if (expectedType.isEnum()  && value instanceof String) {
            // yeah could use Enum.valueOf ....
            return of(expectedType, value);
        }

        // Convert single enum name to enum array (multiple enum values not supported)
        if (expectedType.isArray()  && value instanceof String) {
            final Class<?> enumType = expectedType.getComponentType();

            if (enumType.isEnum()) { // just in case
                final Enum<?> enumConstant = (Enum<?>) of(enumType, value);
                final Enum<?>[] outcomeArray = (Enum<?>[]) Array.newInstance(enumType, 1);
                outcomeArray[0] = enumConstant;

                return outcomeArray;
            }

            // else not sure what else we can do but let the Object go
        }

        return value;
    }

    private static boolean isEnumOrArrayOfEnums(final Class type) {
        if (type.isEnum()) {
            return true;
        }

        if (type.isArray()) {
            final Class componentType = type.getComponentType();
            return componentType.isEnum();
        }

        return false;
    }

    private static <T /*extends Enum<T>*/> T of(final Class<T> type, final Object name) {
        try {
            return (T) type.getDeclaredMethod("valueOf", String.class).invoke(null, name);

        } catch (final Exception e) {
            // this will most likely result in a conversion error, but at least we know
            // it won't be swallowed
            return (T) name;
        }
    }

    public static <T extends Annotation> T of(final Class<T> annotationClass, final T annotation, final BeanManager beanManager) {
        final ELProcessor elProcessor = new ELProcessor();
        elProcessor.getELManager().addELResolver(beanManager.getELResolver());
        return of(annotationClass, annotation, elProcessor);
    }

    public static <T extends Annotation> T of(final Class<T> annotationClass, final T annotation, final ELProcessor elProcessor) {
        return (T) Proxy.newProxyInstance(annotation.getClass().getClassLoader(),
                                          new Class[]{annotationClass},
                                          new TomEEELInvocationHandler(annotation, elProcessor));
    }

}
