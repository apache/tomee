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
package org.apache.openejb.junit;

import org.apache.openejb.testing.ApplicationComposers;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Objects;

public class ApplicationRule implements TestRule {
    private final Object instance;

    public ApplicationRule(final Object instance) {
        this.instance = Objects.requireNonNull(instance);
    }

    public <T> T getInstance(final Class<T> as) {
        return as.cast(instance);
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Thread thread = Thread.currentThread();
                final ClassLoader old = thread.getContextClassLoader();
                final ApplicationComposers composers = new ApplicationOnlyApplicationComposers(instance);
                composers.deployApp(instance);
                try {
                    statement.evaluate();
                } finally {
                    composers.stopApplication();
                    thread.setContextClassLoader(old);
                }
            }
        };
    }

    public static class ApplicationOnlyApplicationComposers extends ApplicationComposers {
        public ApplicationOnlyApplicationComposers(final Object modules) {
            super(modules);
        }

        @Override
        protected boolean isContainer() {
            return false;
        }

        @Override
        protected boolean mockCdiContexts() {
            return false;
        }
    }
}
