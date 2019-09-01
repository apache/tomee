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
package org.superbiz.val;

import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@javax.validation.Constraint(validatedBy = {Allowed.Constraint.class})
@Target({METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface Allowed {

    String value();

    Class<?>[] groups() default {};

    String message() default "The 'group' claim must contain '{value}'";

    Class<? extends Payload>[] payload() default {};


    class Constraint implements ConstraintValidator<Allowed, JsonWebToken> {
        private Allowed allowed;

        @Override
        public void initialize(final Allowed constraint) {
            this.allowed = constraint;
        }

        @Override
        public boolean isValid(final JsonWebToken value, final ConstraintValidatorContext context) {
            final Set<String> groups = value.getGroups();
            return groups != null && groups.contains(this.allowed.value());
        }
    }
}
