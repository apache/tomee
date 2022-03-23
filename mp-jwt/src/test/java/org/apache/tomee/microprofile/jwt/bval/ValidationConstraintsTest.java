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

import org.apache.tomee.microprofile.jwt.JsonWebTokenValidator;
import org.apache.tomee.microprofile.jwt.Tokens;
import org.apache.tomee.microprofile.jwt.bval.ann.Audience;
import org.apache.tomee.microprofile.jwt.bval.ann.Issuer;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.Assert;
import org.junit.Test;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class ValidationConstraintsTest {

    @Test
    public void invalidAudAndIss() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Circle.class);

        final Method red = Circle.class.getMethod("red");


        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"http://something.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        assertViolations(constraints.validate(red, jwt),
                "The 'aud' claim is required",
                "The 'aud' claim must contain 'bar'",
                "The 'iss' claim must be 'http://foo.bar.com'"
        );
    }

    @Test
    public void invalidIss() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Circle.class);

        final Method red = Circle.class.getMethod("red");


        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"aud\":[\"bar\",\"user\"]," +
                "  \"iss\":\"http://something.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        assertViolations(constraints.validate(red, jwt),
                "The 'iss' claim must be 'http://foo.bar.com'"
        );
    }

    @Test
    public void invalidAud() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Circle.class);

        final Method red = Circle.class.getMethod("red");


        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"aud\":[\"foo\",\"user\"]," +
                "  \"iss\":\"http://foo.bar.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        assertViolations(constraints.validate(red, jwt),
                "The 'aud' claim must contain 'bar'"
        );
    }

    @Test
    public void missingAud() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Circle.class);

        final Method red = Circle.class.getMethod("red");


        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"http://foo.bar.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        assertViolations(constraints.validate(red, jwt),
                "The 'aud' claim is required",
                "The 'aud' claim must contain 'bar'"
        );
    }

    @Test
    public void valid() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Circle.class);

        final Method red = Circle.class.getMethod("red");


        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"http://foo.bar.com\"," +
                "  \"aud\":[\"bar\",\"user\"]," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        assertViolations(constraints.validate(red, jwt));
    }

    private static void assertViolations(final Set<ConstraintViolation<Object>> constraintViolations, final String... violations) {
        final List<String> actual = constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.toList());

        final List<String> expected = Stream.of(violations)
                .sorted()
                .collect(Collectors.toList());

        Assert.assertEquals(expected, actual);
    }

    public static class Circle {
        @Audience("bar")
        @Issuer("http://foo.bar.com")
        @Crimson()
        @RolesAllowed("") // to ensur non bean-validation annotations do not cause errors
        public Red red() {
            return new Red();
        }

        /**
         * To ensure non bean-validation methods do not cause errors
         */
        @RolesAllowed("")
        public Object green() {
            return new Red();
        }

        /**
         * To ensure non-public methods do not cause errors
         */
        private Object blue() {
            return new Red();
        }
    }

    public static class Red {
    }

    @Documented
    @jakarta.validation.Constraint(validatedBy = {Crimson.Constraint.class})
    @Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
    @Retention(RUNTIME)
    public @interface Crimson {

        Class<?>[] groups() default {};

        String message() default "This will never pass";

        Class<? extends Payload>[] payload() default {};


        class Constraint implements ConstraintValidator<Crimson, Red> {
            @Override
            public boolean isValid(final Red value, final ConstraintValidatorContext context) {
                return false;
            }
        }
    }
}