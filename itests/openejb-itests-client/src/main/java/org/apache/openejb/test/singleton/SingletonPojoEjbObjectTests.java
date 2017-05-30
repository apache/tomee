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

import javax.ejb.EJBHome;


/**
 * This class tests that all javax.ejb.EJBObject methods work as expected on the EJB 2.1 compatible remote interface
 * of an ejb3 singleton bean.
 * <br>
 * [4] Should be run as the fourth test suite of the BasicSingletonTestClients
 *
 * @version $Rev: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
 */
public class SingletonPojoEjbObjectTests extends BasicSingletonTestClient {

    public SingletonPojoEjbObjectTests() {
        super("PojoEJBObject.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Object obj = initialContext.lookup("client/tests/singleton/BasicSingletonPojoHome");
        ejbHome = (BasicSingletonHome) obj;
        ejbObject = ejbHome.createObject();
    }

    // ===============================
    // Start EJBObject methods test
    //

    /**
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
            final EJBHome home = ejbObject.getEJBHome();
            assertNotNull("The EJBHome is null", home);
        } catch (final Exception e) {
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
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * A method to test the implementation of Singleton Session Bean identity check.
     * See EJB3.0 "Core Contracts and Requirements" specification, section 3.4.5.2
     */
    public void test03_isIdentical() {
        BasicSingletonObject otherEJBObject = null;
        try {
            /**
             * This EJBObject reference is created to validate the identity if different EJBObject references
             * of the same interface type of the same session bean.
             */
            otherEJBObject = ejbHome.createObject();
            assertTrue("The EJBObjects are not identical", ejbObject.isIdentical(ejbObject));
            assertTrue("The EJBObject and the OtherEJBObject are not identical", ejbObject.isIdentical(otherEJBObject));
        } catch (final Exception e) {
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
            // you can't really remove a singleton handle
            ejbObject.businessMethod("Should not throw an exception");
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * See EJB3.0 "Core Contracts and Requirements" specification, section : 3.6.8.3 .
     */
    public void test05_getPrimaryKey() {
        try {
            final Object key = ejbObject.getPrimaryKey();
        } catch (final java.rmi.RemoteException e) {
            assertTrue(true);
            return;
        } catch (final Exception e) {
            fail("A RuntimeException should have been thrown.  Received Exception "
                + e.getClass() + " : " + e.getMessage());
        }
        fail("A RuntimeException should have been thrown.");
    }

    //
    // End EJBObject methods test
    // ===============================
}
