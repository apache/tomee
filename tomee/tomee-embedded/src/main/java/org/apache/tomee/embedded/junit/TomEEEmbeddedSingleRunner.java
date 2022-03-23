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
package org.apache.tomee.embedded.junit;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import jakarta.enterprise.inject.Vetoed;
import java.util.List;

/**
 * see org.apache.tomee.embedded.SingleInstanceRunnerTest for a sample.
 * idea is to reuse some part of ApplicationComposer API to get a single container for all tests in embedded mode.
 * <p>
 * Base is to declare an @Application class which holds the model and some injections.
 * Note: this can be replaced setting tomee.application-composer.application property to the fully qualified name of the app.
 * Note: @Application classes are only searched in the same jar as the test.
 * <p>
 * Model:
 * - @Configuration: programmatic properties - note injections don't work there.
 * - @Classes: only context value is used.
 * - @ContainerProperties: to configure the container
 * - @WebResource: first value can be used to set the docBase (other values are ignored)
 * - @TomEEEmbeddedSingleRunner.LifecycleTasks: allow to add some lifecycle tasks (like starting a ftp/sft/elasticsearch... server)
 * <p>
 * Injections:
 * - CDI
 * - @RandomPort: with the value http or https. Supported types are URL (context base) and int (the port).
 */
@Vetoed
public class TomEEEmbeddedSingleRunner extends BlockJUnit4ClassRunner {
    private static final TomEEEmbeddedBase BASE = new TomEEEmbeddedBase();

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
                    BASE.start(test);
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


    public static void close() {
        BASE.close();
    }

    public TomEEEmbeddedSingleRunner(final Class<?> klass) throws InitializationError {
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
                        BASE.start(test);
                        BASE.composerInject(target);
                        base.evaluate();
                    }
                };
            }
        });
        return rules;
    }
}
