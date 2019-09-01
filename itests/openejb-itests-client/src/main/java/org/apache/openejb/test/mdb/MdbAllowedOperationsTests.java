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
package org.apache.openejb.test.mdb;

import org.apache.openejb.test.object.OperationsPolicy;

import javax.jms.Destination;

/**
 * [9] Should be run as the nineth test suite of the BasicStatelessTestClients
 *
 * <PRE>
 * =========================================================================
 * Operations allowed in the methods of a stateless SessionBean with
 * container-managed transaction demarcation
 * =========================================================================
 *
 * Bean method           | Bean method can perform the following operations
 * ______________________|__________________________________________________
 * |
 * constructor           | -
 * ______________________|__________________________________________________
 * |
 * setSessionContext     |  SessionContext methods:
 * |     - getEJBHome
 * |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 * |
 * ejbCreate             |  SessionContext methods:
 * ejbRemove             |     - getEJBHome
 * |     - getEJBObject
 * |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 * |
 * business method       |  SessionContext methods:
 * from remote interface |     - getEJBHome
 * |     - getCallerPrincipal
 * |     - getRollbackOnly
 * |     - isCallerInRole
 * |     - setRollbackOnly
 * |     - getEJBObject
 * |  JNDI access to java:comp/env
 * |  Resource manager access
 * |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>
 */
public class MdbAllowedOperationsTests extends MdbTestClient {
    protected BasicMdbObject basicMdbObject;

    public MdbAllowedOperationsTests() {
        super("AllowedOperations.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Destination destination = (Destination) initialContext.lookup("client/tests/messagedriven/mdb/BasicMdb");
        basicMdbObject = MdbProxy.newProxyInstance(BasicMdbObject.class, connectionFactory, destination);
        basicMdbObject.businessMethod("foo");
    }

    protected void tearDown() throws Exception {
        MdbProxy.destroyProxy(basicMdbObject);
        super.tearDown();
    }

    //=====================================
    // Test EJBContext allowed operations
    //

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * dependency injection  |  MessageDrivenContext methods:lookup
     * methods (e.g., setMes-|
     * sageDrivenContext)    |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test01_setSessionContext() {
        try {
            final OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_lookup);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            final Object expected = policy;
            final Object actual = basicMdbObject.getAllowedOperationsReport("setMessageDrivenContext");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getTimerService
     * |     - lookup
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test02_ejbCreate() {
        try {
            final OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_lookup);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            final Object expected = policy;
            final Object actual = basicMdbObject.getAllowedOperationsReport("ejbCreate");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * |     - getEJBObject
     * |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test03_ejbRemove() {
        try {
            /* TO DO:  This test needs unique functionality to work */
            final OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getEJBHome);
            policy.allow(OperationsPolicy.Context_getEJBObject);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            final Object expected = policy;
            final Object actual = basicMdbObject.getAllowedOperationsReport("ejbRemove");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     * |
     * business method       |  SessionContext methods:
     * from remote interface |     - getRollbackOnly
     * |     - setRollbackOnly
     * |     - getCallerPrincipal
     * |     - getTimerService
     * |     - lookup
     * |  JNDI access to java:comp/env
     * |  Resource manager access
     * |  Enterprise bean access
     * |  EntityManagerFactory access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test04_businessMethod() {
        try {
            final OperationsPolicy policy = new OperationsPolicy();
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
            // policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
            policy.allow(OperationsPolicy.Context_isCallerInRole);
            policy.allow(OperationsPolicy.Context_lookup);
            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);

            final Object expected = policy;
            final Object actual = basicMdbObject.getAllowedOperationsReport("businessMethod");

            assertNotNull("The OperationsPolicy is null", actual);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test EJBContext allowed operations
    //=====================================
}
