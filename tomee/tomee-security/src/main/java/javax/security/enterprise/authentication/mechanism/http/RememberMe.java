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
package javax.security.enterprise.authentication.mechanism.http;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@InterceptorBinding
@Retention(RUNTIME)
@Target(TYPE)
public @interface RememberMe {
    @Nonbinding
    int cookieMaxAgeSeconds() default 86400;

    @Nonbinding
    String cookieMaxAgeSecondsExpression() default "";

    @Nonbinding
    boolean cookieSecureOnly() default true;

    @Nonbinding
    String cookieSecureOnlyExpression() default "";

    @Nonbinding
    boolean cookieHttpOnly() default true;

    @Nonbinding
    String cookieHttpOnlyExpression() default "";

    @Nonbinding
    String cookieName() default "JREMEMBERMEID";

    @Nonbinding
    boolean isRememberMe() default true;

    @Nonbinding
    String isRememberMeExpression() default "";

    @SuppressWarnings("all")
    final class Literal extends AnnotationLiteral<RememberMe> implements RememberMe {
        private static final long serialVersionUID = 1L;

        private final int cookieMaxAgeSeconds;
        private final String cookieMaxAgeSecondsExpression;
        private final boolean cookieSecureOnly;
        private final String cookieSecureOnlyExpression;
        private final boolean cookieHttpOnly;
        private final String cookieHttpOnlyExpression;
        private final String cookieName;
        private final boolean isRememberMe;
        private final String isRememberMeExpression;

        public static LiteralBuilder builder() {
            return new LiteralBuilder();
        }

        public static class LiteralBuilder {
            private int cookieMaxAgeSeconds = 86400;
            private String cookieMaxAgeSecondsExpression;
            private boolean cookieSecureOnly = true;
            private String cookieSecureOnlyExpression;
            private boolean cookieHttpOnly = true;
            private String cookieHttpOnlyExpression;
            private String cookieName = "JREMEMBERMEID";
            private boolean isRememberMe = true;
            private String isRememberMeExpression;

            public LiteralBuilder cookieMaxAgeSeconds(int cookieMaxAgeSeconds) {
                this.cookieMaxAgeSeconds = cookieMaxAgeSeconds;
                return this;
            }

            public LiteralBuilder cookieMaxAgeSecondsExpression(String cookieMaxAgeSecondsExpression) {
                this.cookieMaxAgeSecondsExpression = cookieMaxAgeSecondsExpression;
                return this;
            }

            public LiteralBuilder cookieSecureOnly(boolean cookieSecureOnly) {
                this.cookieSecureOnly = cookieSecureOnly;
                return this;

            }

            public LiteralBuilder cookieSecureOnlyExpression(String cookieSecureOnlyExpression) {
                this.cookieSecureOnlyExpression = cookieSecureOnlyExpression;
                return this;
            }

            public LiteralBuilder cookieHttpOnly(boolean cookieHttpOnly) {
                this.cookieHttpOnly = cookieHttpOnly;
                return this;
            }

            public LiteralBuilder cookieHttpOnlyExpression(String cookieHttpOnlyExpression) {
                this.cookieHttpOnlyExpression = cookieHttpOnlyExpression;
                return this;
            }

            public LiteralBuilder cookieName(String cookieName) {
                this.cookieName = cookieName;
                return this;
            }

            public LiteralBuilder isRememberMe(boolean isRememberMe) {
                this.isRememberMe = isRememberMe;
                return this;
            }

            public LiteralBuilder isRememberMeExpression(String isRememberMeExpression) {
                this.isRememberMeExpression = isRememberMeExpression;
                return this;
            }

            public Literal build() {
                return new Literal(
                        cookieMaxAgeSeconds,
                        cookieMaxAgeSecondsExpression,
                        cookieSecureOnly,
                        cookieSecureOnlyExpression,
                        cookieHttpOnly,
                        cookieHttpOnlyExpression,
                        cookieName,
                        isRememberMe,
                        isRememberMeExpression);
            }
        }

        public Literal(int cookieMaxAgeSeconds,
                       String cookieMaxAgeSecondsExpression,
                       boolean cookieSecureOnly,
                       String cookieSecureOnlyExpression,
                       boolean cookieHttpOnly,
                       String cookieHttpOnlyExpression,
                       String cookieName,
                       boolean isRememberMe,
                       String isRememberMeExpression) {

            this.cookieMaxAgeSeconds = cookieMaxAgeSeconds;
            this.cookieMaxAgeSecondsExpression = cookieMaxAgeSecondsExpression;
            this.cookieSecureOnly = cookieSecureOnly;
            this.cookieSecureOnlyExpression = cookieSecureOnlyExpression;
            this.cookieHttpOnly = cookieHttpOnly;
            this.cookieHttpOnlyExpression = cookieHttpOnlyExpression;
            this.cookieName = cookieName;
            this.isRememberMe = isRememberMe;
            this.isRememberMeExpression = isRememberMeExpression;
        }

        @Override
        public boolean cookieHttpOnly() {
            return cookieHttpOnly;
        }

        @Override
        public String cookieHttpOnlyExpression() {
            return cookieHttpOnlyExpression;
        }

        @Override
        public int cookieMaxAgeSeconds() {
            return cookieMaxAgeSeconds;
        }

        @Override
        public String cookieMaxAgeSecondsExpression() {
            return cookieMaxAgeSecondsExpression;
        }

        @Override
        public boolean cookieSecureOnly() {
            return cookieSecureOnly;
        }

        @Override
        public String cookieSecureOnlyExpression() {
            return cookieSecureOnlyExpression;
        }

        @Override
        public String cookieName() {
            return cookieName;
        }

        @Override
        public boolean isRememberMe() {
            return isRememberMe;
        }

        @Override
        public String isRememberMeExpression() {
            return isRememberMeExpression;
        }
    }
}
