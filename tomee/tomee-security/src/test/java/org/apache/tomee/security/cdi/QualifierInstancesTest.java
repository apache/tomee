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
import jakarta.inject.Qualifier;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QualifierInstancesTest {

    @Test
    public void literalsSupportsCustomQualifierWithoutNestedLiteral() {
        final Annotation[] qualifiers = QualifierInstances.literals(new Class<?>[] {CustomQualifier.class});

        assertEquals(1, qualifiers.length);
        assertEquals(CustomQualifier.class, qualifiers[0].annotationType());
        assertTrue(qualifiers[0].equals(new CustomQualifierLiteral()));
        assertEquals(new CustomQualifierLiteral().hashCode(), qualifiers[0].hashCode());
    }

    @Test
    public void beanQualifiersIncludesAnyAndCustomQualifierWithoutNestedLiteral() {
        final Annotation[] qualifiers = QualifierInstances.beanQualifiers(new Class<?>[] {CustomQualifier.class});

        final Set<Class<? extends Annotation>> qualifierTypes = Arrays.stream(qualifiers)
                .map(Annotation::annotationType)
                .collect(Collectors.toSet());
        assertEquals(Set.of(Any.class, CustomQualifier.class), qualifierTypes);
    }

    @Test
    public void beanQualifiersUsesDefaultWhenNoCustomQualifiersAreConfigured() {
        final Annotation[] qualifiers = QualifierInstances.beanQualifiers(new Class<?>[0]);

        assertArrayEquals(new Annotation[] {Any.Literal.INSTANCE, Default.Literal.INSTANCE}, qualifiers);
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({TYPE, METHOD, FIELD, PARAMETER})
    private @interface CustomQualifier {
        String value() default "default";
    }

    private static final class CustomQualifierLiteral implements CustomQualifier {
        @Override
        public Class<? extends Annotation> annotationType() {
            return CustomQualifier.class;
        }

        @Override
        public String value() {
            return "default";
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof CustomQualifier qualifier && "default".equals(qualifier.value());
        }

        @Override
        public int hashCode() {
            return (127 * "value".hashCode()) ^ "default".hashCode();
        }
    }
}
