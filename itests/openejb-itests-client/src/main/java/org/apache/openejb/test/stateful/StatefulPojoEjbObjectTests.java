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
package org.apache.openejb.test.stateful;

import javax.ejb.EJBHome;


/**
 * [4] Should be run as the fourth test suite of the BasicStatefulTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatefulPojoEjbObjectTests extends BasicStatefulTestClient {

    public StatefulPojoEjbObjectTests() {
        super("PojoEJBObject.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateful/BasicStatefulPojoHome");
        ejbHome = (BasicStatefulHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicStatefulHome.class);
        ejbObject = ejbHome.createObject("First Bean");
    }

    protected void tearDown() throws Exception {
        //ejbObject.remove();
        super.tearDown();
    }

    //  ===============================
    // Start EJBObject methods test
    //

    /*
         * According to the EJB3.0 "Core Contracts and Requirements" specs, section
         * 3.6.4, a session EJBObject supports:
         * 1. Get the session object's remote home interface.
         * 2. Get the session object's handle.
         * 3. Test if the session object is identical with another session object.
         * 4. Remove the session object.
         */

    /**
     * A method to test retrieving the EJBHome interface of a session bean using its EJBObject reference.
     */
    public void test01_getEjbHome() {
        try {
            EJBHome home = ejbObject.getEJBHome();
            assertNotNull("The EJBHome is null", home);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * A method to test retrieving a Handle of a session bean using its EJBObject reference.
     */
    public void test02_getHandle() {
        try {
            ejbHandle = ejbObject.getHandle();
            assertNotNull("The Handle is null", ejbHandle);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * A method to test the implementation of Stateful Session Bean identity check.
     * See EJB3.0 "Core Contracts and Requirements" specification, section 3.4.5.1
     */
    public void test03_isIdentical() {
        BasicStatefulObject otherEJBObject = null;
        BasicStatefulObject JustAnotherEJBObject = null;

        try {
            /*
                      * This EJBObject reference is created to validate the identity if different EJBObject refernces
                      * of the same interface type of the same session bean.
                      */
            otherEJBObject = ejbHome.createObject("Second bean");
            JustAnotherEJBObject = ejbHome.createObject("First bean");
            assertTrue("The EJBObjects are not equal", ejbObject.isIdentical(ejbObject));
            assertFalse("The EJBObjects are not equal", ejbObject.isIdentical(otherEJBObject));
            assertFalse("The EJBObjects are not equal", ejbObject.isIdentical(JustAnotherEJBObject));
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * This test is 2 in 1, it tests calling remove() on an EJBObject reference and then calling a business method on the
     * same reference after the remove() is successfuly called.
     */
    public void test04_remove() {
        try {
            ejbObject.remove();
            try {
                ejbObject.businessMethod("Should throw an exception");
                assertTrue("Calling business method after removing the EJBObject does not throw an exception", false);
            } catch (Exception e) {
                assertTrue(true);
                return;
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * See EJB3.0 "Core Contracts and Requirements" specification, section : 3.6.8.3 .
     */
    public void test05_getPrimaryKey() {
        try {
            Object key = ejbObject.getPrimaryKey();
        } catch (java.rmi.RemoteException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            fail("A RuntimeException should have been thrown.  Received Exception " + e.getClass() + " : " + e.getMessage());
        }
        fail("A RuntimeException should have been thrown.");
    }

    //
    // Test ejb object methods
    //===============================

}
