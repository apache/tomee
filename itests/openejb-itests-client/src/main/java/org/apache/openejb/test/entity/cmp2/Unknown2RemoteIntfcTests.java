/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.entity.cmp2;

import org.apache.openejb.test.entity.cmp.UnknownCmpHome;

/**
 * [5] Should be run as the fifth test suite of the BasicCmpTestClients
 */
public class Unknown2RemoteIntfcTests extends UnknownCmp2TestClient {

    public Unknown2RemoteIntfcTests() {
        super("RemoteIntfc.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp2/UnknownCmpHome");
        ejbHome = (UnknownCmpHome) javax.rmi.PortableRemoteObject.narrow(obj, UnknownCmpHome.class);
        ejbObject = ejbHome.createObject("Forth Bean");
    }

    //=================================
    // Test remote interface methods
    //
    public void test01_businessMethod() {
        try {
            String expected = "Success";
            String actual = ejbObject.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * Throw an application exception and make sure the exception
     * reaches the bean nicely.
     */
    public void test02_throwApplicationException() {
        try {
            ejbObject.throwApplicationException();
        } catch (org.apache.openejb.test.ApplicationException e) {
            //Good.  This is the correct behaviour
            return;
        } catch (Throwable e) {
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
            String expected = "Success";
            String actual = ejbObject.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_throwSystemException() {
        try {
            ejbObject.throwSystemException_NullPointer();
        } catch (java.rmi.RemoteException e) {
            //Good, so far.
            Throwable n = e.detail;
            assertNotNull("Nested exception should not be is null", n);
            assertTrue("Nested exception should be an instance of NullPointerException, but exception is " + n.getClass().getName(), (n instanceof NullPointerException));
            return;
        } catch (Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
        fail("A NullPointerException should have been thrown.");
    }

    /**
     * After a system exception the intance should be garbage collected
     * and the remote reference should be invalidated.
     * <p/>
     * The Remote Server fails this one, that should be fixed.
     */
    public void BUG_test05_invokeAfterSystemException() {
        try {
            ejbObject.businessMethod("This refernce is invalid");
            fail("A java.rmi.NoSuchObjectException should have been thrown.");
        } catch (java.rmi.NoSuchObjectException e) {
            // Good.
        } catch (Throwable e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test remote interface methods
    //=================================

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
