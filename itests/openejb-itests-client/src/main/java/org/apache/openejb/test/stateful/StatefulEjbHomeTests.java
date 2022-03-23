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

import jakarta.ejb.EJBMetaData;
import jakarta.ejb.RemoveException;

/**
 * [3] Should be run as the third test suite of the BasicStatefulTestClients
 */
public class StatefulEjbHomeTests extends BasicStatefulTestClient {

    public StatefulEjbHomeTests() {
        super("EJBHome.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Object obj = initialContext.lookup("client/tests/stateful/BasicStatefulHome");
        ejbHome = (BasicStatefulHome) obj;
    }

    //===============================
    // Test ejb home methods
    //
    public void test01_getEJBMetaData() {
        try {
            final EJBMetaData ejbMetaData = ejbHome.getEJBMetaData();
            assertNotNull("The EJBMetaData is null", ejbMetaData);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_getHomeHandle() {
        try {
            ejbHomeHandle = ejbHome.getHomeHandle();
            assertNotNull("The HomeHandle is null", ejbHomeHandle);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * ------------------------------------
     * 5.3.2 Removing a session object
     * A client may remove a session object using the remove() method on the jakarta.ejb.EJBObject
     * interface, or the remove(Handle handle) method of the jakarta.ejb.EJBHome interface.
     *
     * Because session objects do not have primary keys that are accessible to clients, invoking the
     * jakarta.ejb.EJBHome.remove(Object primaryKey) method on a session results in the
     * jakarta.ejb.RemoveException.
     *
     * ------------------------------------
     * 5.5 Session object identity
     *
     * Session objects are intended to be private resources used only by the
     * client that created them. For this reason, session objects, from the
     * client's perspective, appear anonymous. In contrast to entity objects,
     * which expose their identity as a primary key, session objects hide their
     * identity. As a result, the EJBObject.getPrimaryKey() and
     * EJBHome.remove(Object primaryKey) methods result in a java.rmi.RemoteException
     * if called on a session bean. If the EJBMetaData.getPrimaryKeyClass()
     * method is invoked on a EJBMetaData object for a Session bean, the method throws
     * the java.lang.RuntimeException.
     * ------------------------------------
     *
     * Sections 5.3.2 and 5.5 conflict.  5.3.2 says to throw jakarta.ejb.RemoveException, 5.5 says to
     * throw java.rmi.RemoteException.
     *
     * For now, we are going with java.rmi.RemoteException.
     */
    public void test03_removeByPrimaryKey() {
        try {
            ejbHome.remove("primaryKey");
        } catch (final RemoveException e) {
            assertTrue(true);
            return;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " instead of jakarta.ejb.RemoveException : " + e.getMessage());
        }
        assertTrue("jakarta.ejb.RemoveException should have been thrown", false);
    }
    //
    // Test ejb home methods
    //===============================
}
