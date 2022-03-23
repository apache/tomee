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
package org.apache.tomee.microprofile.jwt.bval.red;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URL;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@jakarta.validation.Constraint(validatedBy = {TwoReturnValidation.UriConstraint.class, TwoReturnValidation.UrlConstraint.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface TwoReturnValidation {

    String value();

    Class<?>[] groups() default {};

    String message() default "The 'aud' claim must contain '{value}'";

    Class<? extends Payload>[] payload() default {};


    class UrlConstraint implements ConstraintValidator<TwoReturnValidation, URL> {
        @Override
        public boolean isValid(final URL value, final ConstraintValidatorContext context) {
            return true;
        }
    }

    class UriConstraint implements ConstraintValidator<TwoReturnValidation, URI> {
        @Override
        public boolean isValid(final URI value, final ConstraintValidatorContext context) {
            return true;
        }
    }
}
