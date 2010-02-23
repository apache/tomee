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
/**
 * TODO: Decide what to do when the user already specifies a login module/configuration to use. We need to either provide a way to authenticate against this, or disable the securityRole option. Discuss with dblevins
 */
package org.apache.openejb.junit;

import junit.framework.TestCase;
import org.apache.openejb.junit.context.OpenEjbTestContext;
import org.apache.openejb.junit.context.TestContext;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *
 * @author quintin
 */
public class OpenEjbRunner extends Runner {
    private Runner delegate;

    /**
     * Test class
     */
    private Class<?> testClazz;

    /**
     * Stores the TestContext where only the class configuration is used.
     */
    private TestContext classTestContext;

    /**
     * Creates a new runner (called by JUnit)
     *
     * @param testClazz
     * @throws InitializationError
     */
    public OpenEjbRunner(Class<?> testClazz) throws InitializationError {
        this.testClazz = testClazz;
        try {
            delegate = getDelegateRunner(testClazz);
        }
        catch (Throwable e) {
            throw new InitializationError(Arrays.asList(e));
        }
    }

    /**
     * Determines, based on the test class which runner we should delegate the
     * actual JUnit logic to.
     *
     * @param testClazz
     * @return the delegate runner
     * @throws Throwable NFC! Not like you need to declare you're throwing an Error
     */
    protected Runner getDelegateRunner(Class<?> testClazz) throws Throwable {
        if (TestCase.class.isAssignableFrom(testClazz)) {
            throw new UnsupportedOperationException("JUnit 3 tests not supported yet.");
        } else {
            return new JUnit4Runner(this, testClazz);
        }
    }

    /**
     * @param method
     * @param method
     * @return a new method level context
     * @see #newTestContext(java.lang.reflect.Method, java.lang.String)
     */
    public TestContext newTestContext(Method method) {
        return newTestContext(method, null);
    }

    /**
     * This accepts a null method. Might make it easier to initialize new contexts
     * from the delegate runner (which calls back here). The cohesion isn't that great
     * but is the best I could come up with for delegating to both JUnit 3 and 4.
     * <p/>
     * The alternative was a context factory - though that introduced a new class and
     * made extending it more complex as 3 classes are needed - a runner to identify the
     * factory, a factory and a context. Either that or you need to specify the
     * factory class in every test.
     *
     * @param method
     * @param roleName   Role to execute the context in.
     * @return a new method level context
     */
    public TestContext newTestContext(Method method, String roleName) {
        if (method == null) {
            if (classTestContext == null) {
                classTestContext = new OpenEjbTestContext(testClazz);
            }
            return classTestContext;
        } else {
            return new OpenEjbTestContext(method, roleName);
        }
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier runNotifier) {
        delegate.run(runNotifier);
    }

    @Override
    public int testCount() {
        return delegate.testCount();
    }

    /**
     * @return the test class being run by this runner
     */
    protected Class<?> getTestClass() {
        return testClazz;
    }
}
