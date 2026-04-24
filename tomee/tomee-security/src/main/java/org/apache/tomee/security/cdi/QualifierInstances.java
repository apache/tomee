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
package org.apache.tomee.security.cdi;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

final class QualifierInstances {
    private QualifierInstances() {
        // no-op
    }

    static Annotation[] literals(final Class<?>[] qualifierClasses) {
        final Annotation[] qualifiers = new Annotation[qualifierClasses.length];
        for (int i = 0; i < qualifierClasses.length; i++) {
            qualifiers[i] = toQualifierLiteral(qualifierClasses[i]);
        }
        return qualifiers;
    }

    static Annotation[] beanQualifiers(final Class<?>[] qualifierClasses) {
        final Map<Class<?>, Annotation> qualifiers = new LinkedHashMap<>();
        qualifiers.put(Any.class, Any.Literal.INSTANCE);

        for (final Class<?> qualifierClass : qualifierClasses) {
            final Annotation qualifier = toQualifierLiteral(qualifierClass);
            qualifiers.put(qualifier.annotationType(), qualifier);
        }

        if (qualifierClasses.length == 0) {
            qualifiers.put(Default.class, Default.Literal.INSTANCE);
        }

        return qualifiers.values().toArray(new Annotation[0]);
    }

    private static Annotation toQualifierLiteral(final Class<?> qualifierClass) {
        if (!Annotation.class.isAssignableFrom(qualifierClass)) {
            throw new IllegalArgumentException("Not an annotation qualifier type: " + qualifierClass);
        }

        if (Any.class == qualifierClass) {
            return Any.Literal.INSTANCE;
        }
        if (Default.class == qualifierClass) {
            return Default.Literal.INSTANCE;
        }

        final String literalClassName = qualifierClass.getName() + "$Literal";
        try {
            final Class<?> literalClass = Class.forName(literalClassName, true, qualifierClass.getClassLoader());
            final Field instance = literalClass.getField("INSTANCE");
            final Object value = instance.get(null);
            if (value instanceof Annotation annotation) {
                return annotation;
            }
        } catch (final ReflectiveOperationException e) {
            return proxyQualifier(qualifierClass.asSubclass(Annotation.class));
        }

        return proxyQualifier(qualifierClass.asSubclass(Annotation.class));
    }

    private static Annotation proxyQualifier(final Class<? extends Annotation> qualifierClass) {
        return (Annotation) Proxy.newProxyInstance(
                qualifierClass.getClassLoader(),
                new Class<?>[] {qualifierClass},
                new QualifierInvocationHandler(qualifierClass));
    }

    private static final class QualifierInvocationHandler implements InvocationHandler {
        private final Class<? extends Annotation> qualifierClass;

        private QualifierInvocationHandler(final Class<? extends Annotation> qualifierClass) {
            this.qualifierClass = qualifierClass;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            final String name = method.getName();
            if ("annotationType".equals(name) && method.getParameterCount() == 0) {
                return qualifierClass;
            }
            if ("equals".equals(name) && method.getParameterCount() == 1) {
                return equals(args[0]);
            }
            if ("hashCode".equals(name) && method.getParameterCount() == 0) {
                return hashCode();
            }
            if ("toString".equals(name) && method.getParameterCount() == 0) {
                return "@" + qualifierClass.getName() + "()";
            }

            return method.getDefaultValue();
        }

        @Override
        public boolean equals(final Object other) {
            if (!qualifierClass.isInstance(other)) {
                return false;
            }

            for (final Method member : qualifierClass.getDeclaredMethods()) {
                final Object defaultValue = member.getDefaultValue();
                final Object otherValue;
                try {
                    otherValue = member.invoke(other);
                } catch (final ReflectiveOperationException e) {
                    return false;
                }
                if (!memberValueEquals(defaultValue, otherValue)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 0;
            for (final Method member : qualifierClass.getDeclaredMethods()) {
                hashCode += (127 * member.getName().hashCode()) ^ memberValueHashCode(member.getDefaultValue());
            }
            return hashCode;
        }

        private static boolean memberValueEquals(final Object left, final Object right) {
            return Arrays.deepEquals(new Object[] {left}, new Object[] {right});
        }

        private static int memberValueHashCode(final Object value) {
            return Arrays.deepHashCode(new Object[] {value}) - 31;
        }
    }
}
