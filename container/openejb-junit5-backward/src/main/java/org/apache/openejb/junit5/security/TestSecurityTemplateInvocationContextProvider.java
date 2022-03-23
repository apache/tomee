/**
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
package org.apache.openejb.junit5.security;

import org.apache.openejb.junit.TestSecurity;
import org.apache.openejb.junit5.OpenEjbExtension;
import org.junit.jupiter.api.extension.*;

import jakarta.ejb.EJBAccessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestSecurityTemplateInvocationContextProvider implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {

        TestSecurity testSecurity = null;
        if (extensionContext.getTestMethod().isPresent() && extensionContext.getTestMethod().get().isAnnotationPresent(TestSecurity.class)) {
            testSecurity = extensionContext.getTestMethod().get().getAnnotation(TestSecurity.class);
        } else if (extensionContext.getTestClass().isPresent() && extensionContext.getTestClass().get().isAnnotationPresent(TestSecurity.class)) {
            testSecurity = extensionContext.getTestClass().get().getAnnotation(TestSecurity.class);
        }

        if (testSecurity != null) {
            String[] authorized = testSecurity.authorized();
            String[] unauthorized = testSecurity.unauthorized();

            List<TestTemplateInvocationContext> contexts = new ArrayList<>();

            for (String role : authorized) {
                contexts.add(invocationContext(role, true));
            }
            for (String role : unauthorized) {
                contexts.add(invocationContext(role, false));
            }

            return contexts.stream();
        } else {
            //no security annotations present, go with the default invocation context


           return Stream.of(new TestTemplateInvocationContext() {
               @Override
               public List<Extension> getAdditionalExtensions() {
                   return Collections.singletonList(new OpenEjbExtension());
               }
           });
        }

    }


    private TestTemplateInvocationContext invocationContext(String role, boolean authorized) {
        return new TestTemplateInvocationContext() {

            private boolean ejbAccessThrown = false;

            @Override
            public String getDisplayName(int invocationIndex) {
                if(role.equals(TestSecurity.UNAUTHENTICATED)) {
                    return "unauthenticated";
                }
                return role;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                List<Extension> extensions = new ArrayList<>();

                extensions.add(new TestExecutionExceptionHandler() {
                    @Override
                    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
                        if (!authorized) {
                            if (throwable instanceof EJBAccessException) {
                                //ok - this would be expected here, do not fail the test!
                                ejbAccessThrown = true;
                                return;
                            } else {
                                throw throwable;
                            }
                        }
                        throw throwable;
                    }
                });

                extensions.add(new AfterEachCallback() {
                    @Override
                    public void afterEach(ExtensionContext extensionContext) throws Exception {
                        if (!authorized) {
                            if (!ejbAccessThrown) {
                                throw new RuntimeException("Expected 'EJBAccessException' but caught none.");
                            }
                        }
                    }
                });

                extensions.add(new OpenEjbExtension(role));

                return extensions;



            }
        };
    }
}