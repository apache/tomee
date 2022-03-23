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
package org.apache.openejb.test.singleton;

import jakarta.ejb.EJBException;

public class SingletonLocalIntfcTests extends BasicSingletonLocalTestClient {

    public SingletonLocalIntfcTests() {
        super("LocalIntfc.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Object obj = initialContext.lookup("client/tests/singleton/BasicSingletonPojoHomeLocal");
        ejbLocalHome = (BasicSingletonLocalHome) obj;
        ejbLocalObject = ejbLocalHome.create();
    }

    //=================================
    // Test local interface methods
    //
    public void test01_businessMethod() {
        try {
            final String expected = "Success";
            final String actual = ejbLocalObject.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * Throw an application exception in the bean and make sure the exception
     * reaches the client nicely.
     */
    public void test02_throwApplicationException() {
        try {
            ejbLocalObject.throwApplicationException();
        } catch (final org.apache.openejb.test.ApplicationException e) {
            //Good.  This is the correct behaviour
            return;
        } catch (final Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
        fail("An ApplicationException should have been thrown.");
    }

    /**
     * After an application exception we should still be able to
     * use our bean
     */
    public void test03_invokeAfterApplicationException() {
        try {
            final String expected = "Success";
            final String actual = ejbLocalObject.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (final Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_throwSystemException() {
        try {
            ejbLocalObject.throwSystemException_NullPointer();
        } catch (final EJBException e) {
            //Good, so far.

            assertNotNull("Nested exception should not be is null", e);
            assertTrue("Nested exception should be an instance of NullPointerException, but exception is " + e.getCausedByException().getClass().getName(), (e.getCausedByException() instanceof NullPointerException));
            return;
        } catch (final Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
        fail("An EJBException encapsulating a NullPointerException should have been thrown.");
    }

    /**
     * After a system exception the intance should be garbage collected
     * but this is invisible to the client as it will just use another
     * singleton session object. All the singleton session objects are
     * equal.  Refer 4.5.3 in EJB 3.0 core specification.
     * This one seems to fail. we should double-check the spec on this.
     */
    public void test05_invokeAfterSystemException() {
        try {
            final String expected = "Success";
            final String actual = ejbLocalObject.businessMethod("sseccuS");
            assertEquals(expected, actual);

        } catch (final Exception e) {
            fail("The business method should have been executed.");
        } catch (final Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test remote interface methods
    //=================================

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //TODO Tests for the below conditions need to be added.
        /* Test an Application Exception that is a runtime exception
	     * Throwing an application exception within a transaction
	     * will rollback only if that method is marked for rollback
	     * Also FinderException, CreateException and RemoveException
	     * are application exceptions.
	     */
}