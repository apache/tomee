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
            throw new IllegalStateException("Unable to resolve qualifier literal for " + qualifierClass.getName(), e);
        }

        throw new IllegalStateException("Qualifier literal for " + qualifierClass.getName() + " is invalid");
    }
}
