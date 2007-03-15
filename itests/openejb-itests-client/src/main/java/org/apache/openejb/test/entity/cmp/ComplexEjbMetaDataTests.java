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
package org.apache.openejb.test.entity.cmp;

import javax.ejb.EJBHome;
import javax.rmi.PortableRemoteObject;

/**
 * [8] Should be run as the eigth test suite of the ComplexCmpTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ComplexEjbMetaDataTests extends ComplexCmpTestClient {

    public ComplexEjbMetaDataTests() {
        super("EJBMetaData.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp/ComplexCmpHome");
        ejbHome = (ComplexCmpHome) PortableRemoteObject.narrow(obj, ComplexCmpHome.class);
        ejbMetaData = ejbHome.getEJBMetaData();
    }

    //=================================
    // Test meta data methods
    //
    public void test01_getEJBHome() {
        try {
            EJBHome home = ejbMetaData.getEJBHome();
            assertNotNull("The EJBHome is null", home);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_getHomeInterfaceClass() {
        try {
            Class clazz = ejbMetaData.getHomeInterfaceClass();
            assertNotNull("The Home Interface class is null", clazz);
            assertEquals(clazz, ComplexCmpHome.class);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_getPrimaryKeyClass() {
        try {
            Class clazz = ejbMetaData.getPrimaryKeyClass();
            assertNotNull("The EJBMetaData is null", clazz);
            assertEquals(clazz, ComplexCmpBeanPk.class);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_getRemoteInterfaceClass() {
        try {
            Class clazz = ejbMetaData.getRemoteInterfaceClass();
            assertNotNull("The Remote Interface class is null", clazz);
            assertEquals(clazz, ComplexCmpObject.class);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test05_isSession() {
        try {
            assertTrue("EJBMetaData says this is a session bean", !ejbMetaData.isSession());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test06_isStatelessSession() {
        try {
            assertTrue("EJBMetaData says this is a stateless session bean", !ejbMetaData.isStatelessSession());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test meta data methods
    //=================================
}
