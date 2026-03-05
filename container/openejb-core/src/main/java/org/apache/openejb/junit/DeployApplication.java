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

import java.util.concurrent.Callable;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.testing.ApplicationComposers;
import org.junit.runners.model.Statement;

public class DeployApplication extends Statement {
    // The TestCase instance
    private final Object testInstance;
    private final Statement next;
    private final ApplicationComposers delegate;

    public DeployApplication(final Object testInstance, final Statement next, final ApplicationComposers delegate) {
        this.testInstance = testInstance;
        this.next = next;
        this.delegate = delegate;
    }

    @Override
    public void evaluate() throws Throwable {
        delegate.evaluate(testInstance, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    next.evaluate();
                } catch (final Error e) {
                    throw e;
                } catch (final Throwable throwable) {
                    if (throwable instanceof Exception) {
                        throw (Exception) throwable;
                    }
                    throw new OpenEJBRuntimeException("Failed test evaluation", throwable);
                }
                return null;
            }
        });
    }
}
