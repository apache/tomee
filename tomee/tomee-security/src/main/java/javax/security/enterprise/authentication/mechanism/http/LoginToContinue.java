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
public @interface LoginToContinue {
    @Nonbinding
    String loginPage() default "/login";

    @Nonbinding
    boolean useForwardToLogin() default true;

    @Nonbinding
    String useForwardToLoginExpression() default "";

    @Nonbinding
    String errorPage() default "/login-error";

    @SuppressWarnings("all") final class Literal extends AnnotationLiteral<LoginToContinue> implements LoginToContinue {
        private static final long serialVersionUID = 1L;

        private final String loginPage;
        private final boolean useForwardToLogin;
        private final String useForwardToLoginExpression;
        private final String errorPage;

        public static LiteralBuilder builder() {
            return new LiteralBuilder();
        }

        public static class LiteralBuilder {
            private String loginPage = "/login";
            private boolean useForwardToLogin = true;
            private String useForwardToLoginExpression;
            private String errorPage = "/login-error";

            public LiteralBuilder loginPage(String loginPage) {
                this.loginPage = loginPage;
                return this;
            }

            public LiteralBuilder useForwardToLogin(boolean useForwardToLogin) {
                this.useForwardToLogin = useForwardToLogin;
                return this;
            }

            public LiteralBuilder useForwardToLoginExpression(String useForwardToLoginExpression) {
                this.useForwardToLoginExpression = useForwardToLoginExpression;
                return this;
            }

            public LiteralBuilder errorPage(String errorPage) {
                this.errorPage = errorPage;
                return this;
            }

            public Literal build() {
                return new Literal(
                        loginPage,
                        useForwardToLogin,
                        useForwardToLoginExpression,
                        errorPage);
            }
        }

        public Literal(String loginPage,
                       boolean useForwardToLogin,
                       String useForwardToLoginExpression,
                       String errorPage) {
            this.loginPage = loginPage;
            this.useForwardToLogin = useForwardToLogin;
            this.useForwardToLoginExpression = useForwardToLoginExpression;
            this.errorPage = errorPage;
        }

        @Override
        public String loginPage() {
            return loginPage;
        }

        @Override
        public boolean useForwardToLogin() {
            return useForwardToLogin;
        }

        @Override
        public String useForwardToLoginExpression() {
            return useForwardToLoginExpression;
        }

        @Override
        public String errorPage() {
            return errorPage;
        }

    }
}
