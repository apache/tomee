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

import org.apache.openejb.junit.context.ContextWrapperStatement;
import org.apache.openejb.junit.context.TestContext;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import jakarta.ejb.EJBAccessException;
import java.util.ArrayList;
import java.util.List;

public class JUnit4Runner extends BlockJUnit4ClassRunner {
    private final OpenEjbRunner runner;

    public JUnit4Runner(final OpenEjbRunner runner, final Class<?> testClazz) throws InitializationError {
        super(testClazz);
        this.runner = runner;
    }

    /**
     * @return the main runner
     */
    protected OpenEjbRunner getOpenEjbRunner() {
        return runner;
    }

    /**
     * Prepares a method statement, configuring it for the test context
     */
    @Override
    protected Statement methodBlock(final FrameworkMethod method) {
        // check if either the method/class is annotated with the TestSecurity annotation
        TestSecurity testSecurity = null;
        if (method.getMethod().isAnnotationPresent(TestSecurity.class)) {
            testSecurity = method.getMethod().getAnnotation(TestSecurity.class);
        } else if (getTestClass().getJavaClass().isAnnotationPresent(TestSecurity.class)) {
            testSecurity = getTestClass().getJavaClass().getAnnotation(TestSecurity.class);
        }

        // no security to run as, just create a normal statement
        if (testSecurity == null ||
            (testSecurity.authorized().length == 0 && testSecurity.unauthorized().length == 0)) {
            return createUnsecuredStatement(method);
        }
        // security roles specified, create separate statements for them all
        else {
            return createSecuredStatementExecutor(method, testSecurity);
        }
    }

    /**
     * Builds a method statement that executes in an unauthenticated context
     */
    protected Statement createUnsecuredStatement(final FrameworkMethod method) {
        final Object test = newTestInstance();

        Statement statement = methodInvoker(method, test);
        statement = possiblyExpectingExceptions(method, test, statement);
        statement = withPotentialTimeout(method, test, statement);
        statement = withBefores(method, test, statement);
        statement = withAfters(method, test, statement);

        final TestContext context = runner.newTestContext(method.getMethod());
        return new ContextWrapperStatement(context, statement, test);
    }

    /**
     * Create a method statement that executes the test for each of the specified
     * roles, both doing authorized and unauthorized tests.
     *
     * Unauthorized roles are configured to fail with EJBAccessExceptions.
     *
     * @param method
     * @param testSecurity
     * @return created statement
     */
    private Statement createSecuredStatementExecutor(final FrameworkMethod method, final TestSecurity testSecurity) {
        final MultiStatementExecutor statementExecutor = new MultiStatementExecutor();

        for (final String role : testSecurity.authorized()) {
            final Statement statement = createSecuredStatement(method, role, false);
            statementExecutor.addStatement(statement);
        }

        for (final String role : testSecurity.unauthorized()) {
            final Statement statement = createSecuredStatement(method, role, true);
            statementExecutor.addStatement(statement);
        }

        return statementExecutor;
    }

    /**
     * Create a new statement to run with a given role and whether it should fail or not.
     *
     * @param method
     * @param role
     * @param failWithAccessException
     * @return statement
     */
    private Statement createSecuredStatement(final FrameworkMethod method, final String role, final boolean failWithAccessException) {
        final Object test = newTestInstance();

        Statement statement = methodInvoker(method, test);
        statement = possiblyExpectingAccessException(statement, failWithAccessException);
        statement = possiblyExpectingExceptions(method, test, statement); // specified in @Test
        statement = withPotentialTimeout(method, test, statement);
        statement = withBefores(method, test, statement);
        statement = withAfters(method, test, statement);

        final TestContext context = runner.newTestContext(method.getMethod(), role);
        return new ContextWrapperStatement(context, statement, test);
    }

    protected Statement possiblyExpectingAccessException(final Statement next, final boolean failWithAccessException) {
        if (failWithAccessException) {
            return new ExpectException(next, EJBAccessException.class);
        } else {
            return next;
        }
    }

    /**
     * Creates a new test instance
     *
     * @return new instance
     */
    private Object newTestInstance() {
        try {
            return new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (final Throwable e) {
            return new Fail(e);
        }
    }

    public static class MultiStatementExecutor extends Statement {
        private final List<Statement> statements = new ArrayList<Statement>();

        @Override
        public void evaluate() throws Throwable {
            for (final Statement statement : statements) {
                statement.evaluate();
            }
        }

        public void addStatement(final Statement statement) {
            statements.add(statement);
        }
    }
}
