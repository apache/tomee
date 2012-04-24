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
package org.apache.openejb.junit.context;

import org.apache.openejb.OpenEJBRuntimeException;
import org.junit.Test;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ContextWrapperStatementTest {
    private static boolean configuredInvoked = false;

    private static boolean statementInvoked = false;

    private static final Object fakeTestObject = new Object();

    private static class FakeStatement extends Statement {
        private boolean fail;

        public FakeStatement(boolean fail) {
            this.fail = fail;
        }

        @Override
        public void evaluate() throws Throwable {
            if (fail) {
                throw new Exception("Fail");
            }
            statementInvoked = true;
        }
    }

    private static class FakeTestContext implements TestContext {
        private boolean fail;

        public FakeTestContext(boolean fail) {
            this.fail = fail;
        }

        public void configureTest(Object testObj) {
            if (fail) {
                throw new OpenEJBRuntimeException("Fail");
            }

            assertEquals(fakeTestObject, testObj);

            configuredInvoked = true;
        }

        @Override
        public void close() {
            // no-op
        }
    }

    public ContextWrapperStatementTest() {
    }

    @Test
    public void testEvaluate() throws Throwable {
        configuredInvoked = false;
        statementInvoked = false;

        ContextWrapperStatement wrapper = new ContextWrapperStatement(new FakeTestContext(false), new FakeStatement(false), fakeTestObject);
        wrapper.evaluate();

        assertTrue(configuredInvoked);
        assertTrue(statementInvoked);
    }

    /**
     * Testing the failures, we can assert that the order of invocation is true, based
     * on the values of the booleans AFTER the failure
     *
     * @throws Throwable
     */
    @Test
    public void testEvaluateFailStatement() throws Throwable {
        configuredInvoked = false;
        statementInvoked = false;

        ContextWrapperStatement wrapper = new ContextWrapperStatement(new FakeTestContext(false), new FakeStatement(true), fakeTestObject);
        try {
            wrapper.evaluate();
            fail("Wrapper call succeeded.");
        }
        catch (Throwable e) {
        }

        assertTrue(configuredInvoked);
        assertFalse(statementInvoked);
    }

    /**
     * Testing the failures, we can assert that the order of invocation is true, based
     * on the values of the booleans AFTER the failure
     *
     * @throws Throwable
     */
    @Test
    public void testEvaluateFailConfigure() throws Throwable {
        configuredInvoked = false;
        statementInvoked = false;

        ContextWrapperStatement wrapper = new ContextWrapperStatement(new FakeTestContext(true), new FakeStatement(false), fakeTestObject);
        try {
            wrapper.evaluate();
            fail("Wrapper call succeeded.");
        }
        catch (Throwable e) {
        }

        assertFalse(configuredInvoked);
        assertFalse(statementInvoked);
    }
}
