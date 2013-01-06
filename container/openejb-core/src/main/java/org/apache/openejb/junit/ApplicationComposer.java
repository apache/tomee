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

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.testing.ApplicationComposers;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationComposer extends BlockJUnit4ClassRunner {
    private final ApplicationComposers delegate;

    public ApplicationComposer(Class<?> klass) throws InitializationError {
        super(klass);
        delegate = new ApplicationComposers(klass);
    }

    @Override
    protected List<MethodRule> rules(Object test) {
        final List<MethodRule> rules = super.rules(test);
        rules.add(new MethodRule(){
            public Statement apply(Statement base, FrameworkMethod method, Object target) {
                return new DeployApplication(target, base);
            }
        });
        return rules;
    }

    public class DeployApplication extends Statement {
        // The TestCase instance
        private final Object testInstance;
        private final Statement next;

        public DeployApplication(Object testInstance, Statement next) {
            this.testInstance = testInstance;
            this.next = next;
        }

        @Override
        public void evaluate() throws Throwable {
            delegate.evaluate(testInstance, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        next.evaluate();
                    } catch (Throwable throwable) {
                        if (throwable instanceof Exception) {
                            throw (Exception) throwable;
                        }
                        throw new OpenEJBRuntimeException("can't evaluate statement", throwable);
                    }
                    return null;
                }
            });
        }
    }
}
