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

import org.apache.openejb.util.Join;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.Test;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;

public class ClassValidationDataTest {

    @Test
    public void test() throws Exception {
        final ClassValidationData data = new ClassValidationData(Green.class);

        assertEquals("" +
                "public java.net.URL Green.emerald()\n" +
                "   interface TokenValidation\n" +
                "public void Green.olive()\n" +
                "   interface TokenValidation", toString(data.getJwtConstraints()));

        assertEquals("" +
                "public java.net.URI Green.sage()\n" +
                "   interface ReturnValidation\n" +
                "public java.net.URL Green.emerald()\n" +
                "   interface ReturnValidation\n" +
                "public org.eclipse.microprofile.jwt.JsonWebToken Green.forrest()\n" +
                "   interface TokenValidation", toString(data.getReturnConstraints()));
    }

    public class Green {

        /**
         * This method will show up in both the list of methods
         * that have return value constraints and jwt constraints
         *
         * Only the applicable constraints will be listed in each
         * collection
         */
        @ReturnValidation("bar")
        @TokenValidation("http://foo.bar.com")
        public URL emerald() {
            try {
                return new URL("foo://bar");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * This method will only show up in the list of
         * methods with return constraints
         */
        @ReturnValidation("bar")
        public URI sage() {
            return null;
        }

        /**
         * This method will only show up in the list of
         * methods with jwt constraints
         */
        @TokenValidation("http://foo.bar.com")
        public void olive() {
        }

        /**
         * This method will only show up in the list of
         * methods with return constraints.
         *
         * When the return value is a JsonWebToken we
         * do not try to validate the caller's token
         */
        @TokenValidation("http://foo.bar.com")
        public JsonWebToken forrest() {
            return null;
        }

        /**
         * This method has no constraints and therefore will
         * not show up in any of the collections
         */
        public java.util.Collection<URI> mint() {
            return null;
        }

    }


    @Documented
    @jakarta.validation.Constraint(validatedBy = {ReturnValidation.UriConstraint.class, ReturnValidation.UrlConstraint.class})
    @Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
    @Retention(RUNTIME)
    public @interface ReturnValidation {

        String value();

        Class<?>[] groups() default {};

        String message() default "The 'aud' claim must contain '{value}'";

        Class<? extends Payload>[] payload() default {};


        class UrlConstraint implements ConstraintValidator<ReturnValidation, URL> {
            @Override
            public boolean isValid(final URL value, final ConstraintValidatorContext context) {
                return true;
            }
        }

        class UriConstraint implements ConstraintValidator<ReturnValidation, URI> {
            @Override
            public boolean isValid(final URI value, final ConstraintValidatorContext context) {
                return true;
            }
        }
    }

    @Documented
    @jakarta.validation.Constraint(validatedBy = {TokenValidation.Constraint.class})
    @Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
    @Retention(RUNTIME)
    public @interface TokenValidation {

        String value();

        Class<?>[] groups() default {};

        String message() default "The 'aud' claim must contain '{value}'";

        Class<? extends Payload>[] payload() default {};


        class Constraint implements ConstraintValidator<TokenValidation, JsonWebToken> {
            private TokenValidation audience;

            @Override
            public void initialize(final TokenValidation constraint) {
                this.audience = constraint;
            }

            @Override
            public boolean isValid(final JsonWebToken value, final ConstraintValidatorContext context) {
                return value != null;
            }
        }
    }


    /*
     * Serialize the data into a string for quick and complete assertion
     */

    private String toString(final List<MethodConstraints> jwtConstraints) {
        final List<String> strings = jwtConstraints.stream()
                .map(this::toString)
                .sorted()
                .map(s -> s.replace(":", "\n   "))
                .collect(Collectors.toList());

        return Join.join("\n", strings)
                .replace(this.getClass().getName() + "$", "");
    }

    private String toString(final MethodConstraints methodConstraints) {
        return methodConstraints.getMethod().toString() + ":" + toString(methodConstraints.getAnnotations());
    }

    private String toString(final Set<?> annotations) {
        final List<String> strings = annotations.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.toList());
        return Join.join(":", strings);
    }
}