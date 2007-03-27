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
package org.apache.openejb.test.entity.cmp2;

import org.apache.openejb.test.object.OperationsPolicy;
import org.apache.openejb.test.entity.cmp.BasicCmpHome;
import org.apache.openejb.test.entity.cmp.BasicCmpObject;

/**
 * [9] Should be run as the nineth test suite of the BasicCmpTestClients
 * <p/>
 * <PRE>
 * =========================================================================
 * Operations allowed in the methods of an entity bean
 * =========================================================================
 * <p/>
 * Bean method           | Bean method can perform the following operations
 * ______________________|__________________________________________________
 * |
 * constructor           | -
 * ______________________|__________________________________________________
 * |
 * setEntityContext      |  EntityContext methods:
 * unsetEntityContext    |     - getEJBHome
 * |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 * |
 * ejbCreate             |  EntityContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbPostCreate         |  EntityContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbRemove             |  EntityContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbFind*              |  EntityContext methods:
 * ejbSelect*            |     - getEJBHome
 * ejbHome               |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbSelectInEntity*    |  EntityContext methods:
 * |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * ejbActivate           |  EntityContext methods:
 * ejbPassivate          |     - getEJBHome
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 * |
 * ejbLoad               |  EntityContext methods:
 * ejbStore              |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * |
 * business method       |  EntityContext methods:
 * from remote interface |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |     - getPrimaryKey
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>
 */
public class Cmp2AllowedOperationsTests extends BasicCmp2TestClient {
    public Cmp2AllowedOperationsTests() {
        super("AllowedOperations.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp2/allowed_operations/EntityHome");
        ejbHome = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicCmpHome.class);
        ejbObject = ejbHome.createObject("Fourth Bean");
        ejbHandle = ejbObject.getHandle();
        /* These tests will only work if the specified
         * method has already been called by the container.
         *
         * TO DO:
         * Implement a little application senario to ensure
         * that all methods tested for below have been called
         * by the container.
         */
        doScenario();


    }

    protected void tearDown() throws Exception {
        try {
            ejbObject.remove();
        } catch (Throwable e) {
            // todo this throws an exception for an unrelated reason....
        }
        super.tearDown();
    }

    /**
     * This method ensures that all thee bean methods have been invoked for correct behaviour
     * of the tests.
     */
    private void doScenario() throws Exception{

        // Call the business method
        ejbObject.businessMethod("Reverse Me");

        ejbHome.sum(1, 2);

        ejbObject = (BasicCmpObject)ejbHandle.getEJBObject();

        ejbHome.findByPrimaryKey((Integer)ejbObject.getPrimaryKey());
        ejbHome.remove(ejbObject.getPrimaryKey());

        ejbObject = ejbHome.createObject("Fourth Bean");
        ejbHome.findEmptyCollection();
    }

    //=====================================
    // Test EJBContext allowed operations       
    //
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * setEntityContext      |  EntityContext methods:
     * unsetEntityContext    |     - getEJBHome
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test01_setEntityContext() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("setEntityContext");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * setEntityContext      |  EntityContext methods:
     * unsetEntityContext    |     - getEJBHome
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test02_unsetEntityContext() {
        try {

            /* TO DO:  This test needs unique functionality to work */
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("unsetEntityContext");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbCreate             |  EntityContext methods:
     * |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test03_ejbCreate() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbCreate");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbPostCreate         |  EntityContext methods:
     * |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test04_ejbPostCreate() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbPostCreate");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbRemove             |  EntityContext methods:
     * |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test05_ejbRemove() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbRemove");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbFind*              |  EntityContext methods:
     * ejbSelect*            |     - getEJBHome
     * ejbHome               |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test08_ejbHome() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbHome");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbActivate           |  EntityContext methods:
     * ejbPassivate          |     - getEJBHome
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test10_ejbActivate() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbActivate");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbActivate           |  EntityContext methods:
     * ejbPassivate          |     - getEJBHome
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test11_ejbPassivate() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbPassivate");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbLoad               |  EntityContext methods:
     * ejbStore              |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test12_ejbLoad() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbLoad");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbLoad               |  EntityContext methods:
     * ejbStore              |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    // todo store is never called
    public void TODO_test13_ejbStore() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbStore");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * business method       |  EntityContext methods:
     * from remote interface |     - getEJBHome
     * |     - getCallerPrincipal
     * |     - getRollbackOnly
     * |     - isCallerInRole
     * |     - setRollbackOnly
     * |     - getEJBObject
     * |     - getPrimaryKey
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test14_businessMethod() {
        try {

            OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
//        policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.Context_getPrimaryKey);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("businessMethod");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test EJBContext allowed operations       
    //=====================================
}


