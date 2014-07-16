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
package org.apache.openejb.junit.jee;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.junit.jee.statement.InjectStatement;
import org.apache.openejb.junit.jee.statement.ShutingDownStatement;
import org.apache.openejb.junit.jee.statement.StartingStatement;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.naming.NamingException;

// @Rule works but mainly designed for @ClassRule for perf reasons
public class EJBContainerRule implements TestRule {
    private final Object test;

    private StartingStatement startingStatement;

    // @ClassRule, you'll need @Rule InjectRule to get injections
    public EJBContainerRule() {
        this(null);
    }

    // @Rule, injections are automatic on test
    public EJBContainerRule(final Object test) {
        this.test = test;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        if (test == null) {
            startingStatement = new StartingStatement(base, description.getTestClass());
        } else {
            startingStatement = new StartingStatement(new Statement() {
                @Override
                // this class avoids a dependency loop issue, we have it actually but that's just to make a nicer API
                public void evaluate() throws Throwable {
                    // don't use testClass since it can be another instance that the test one
                    new InjectStatement(base, test.getClass(), test, startingStatement).evaluate();
                }
            }, description.getTestClass());
        }
        return new ShutingDownStatement(startingStatement, startingStatement);
    }

    // inject in test class or a class of org.apache.openejb.OpenEjbContainer.Provider.OPENEJB_ADDITIONNAL_CALLERS_KEY list
    public void inject(final Object target) {
        try { // reuse this logic to get @TestResource for free
            new InjectStatement(null, target.getClass(), target, startingStatement).evaluate();
        } catch (final Throwable throwable) {
            throw new OpenEJBRuntimeException(throwable.getMessage(), throwable);
        }
    }

    // helper method to get a resource
    public <T> T resource(final Class<T> type, final String name) {
        try {
            return type.cast(
                SystemInstance.get().getComponent(ContainerSystem.class)
                    .getJNDIContext().lookup("java:" + Assembler.OPENEJB_RESOURCE_JNDI_PREFIX + name)
            );
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    // internal API to make it easily integrated
    StartingStatement getStartingStatement() {
        return startingStatement;
    }
}
