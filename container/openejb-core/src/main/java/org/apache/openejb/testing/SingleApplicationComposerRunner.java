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
package org.apache.openejb.testing;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

// goal is to share the same container for all embedded tests and hold the config there
// only works if all tests use the same config
public class SingleApplicationComposerRunner extends BlockJUnit4ClassRunner {
    private static final SingleApplicationComposerBase BASE = new SingleApplicationComposerBase();

    // use when you use another runner like Parameterized of JUnit
    public static class Rule implements TestRule {
        private final Object test;

        public Rule(final Object test) {
            this.test = test;
        }

        @Override
        public Statement apply(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    BASE.start(test.getClass());
                    BASE.composerInject(test);
                    base.evaluate();
                }
            };
        }
    }

    public static class Start extends RunListener {
        @Override
        public void testStarted(final Description description) throws Exception {
            BASE.start(null);
        }
    }

    public static void setApp(final Object o) {
        BASE.setApp(o);
    }

    public SingleApplicationComposerRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<MethodRule> rules(final Object test) {
        final List<MethodRule> rules = super.rules(test);
        rules.add(new MethodRule() {
            @Override
            public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        BASE.start(getTestClass().getJavaClass());
                        BASE.composerInject(target);
                        base.evaluate();
                    }
                };
            }
        });
        return rules;
    }
}
