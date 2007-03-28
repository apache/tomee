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
import javax.ejb.ObjectNotFoundException;
import javax.rmi.PortableRemoteObject;

/**
 * [4] Should be run as the fourth test suite of the ComplexCmpTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ComplexEjbObjectTests extends ComplexCmpTestClient {

    public ComplexEjbObjectTests() {
        super("EJBObject.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp/ComplexCmpHome");
        ejbHome = (ComplexCmpHome) PortableRemoteObject.narrow(obj, ComplexCmpHome.class);
        ejbObject = ejbHome.createObject("Third Bean");
    }

    protected void tearDown() throws Exception {
        // set to null by test05_remove() method
        if (ejbObject != null) {
            try {
                ejbObject.remove();
            } catch (Exception e) {
                throw e;
            }
        }
        super.tearDown();
    }

    //===============================
    // Test ejb object methods
    //
    public void test01_getHandle() {
        try {
            ejbHandle = ejbObject.getHandle();
            assertNotNull("The Handle is null", ejbHandle);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_getPrimaryKey() {
        try {
            ejbPrimaryKey = ejbObject.getPrimaryKey();
            assertNotNull("The primary key is null", ejbPrimaryKey);
            assertTrue("Expected (ejbPrimaryKey instanceof ComplexCmpBeanPk) but was instanceof " + ejbPrimaryKey.getClass().getName(), ejbPrimaryKey instanceof ComplexCmpBeanPk);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_isIdentical() {
        try {
            assertTrue("The EJBObjects are not equal", ejbObject.isIdentical(ejbObject));
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_getEjbHome() {
        try {
            EJBHome home = ejbObject.getEJBHome();
            assertNotNull("The EJBHome is null", home);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test05_remove(){
        try{
            // remove the ejb
            ejbObject.remove();

            // verify that the ejb was actually removed
            try {
                ejbHome.findByPrimaryKey((ComplexCmpBeanPk) ejbPrimaryKey);
                fail("Entity was not actually removed");
            } catch (ObjectNotFoundException e) {
            }

            // verify the proxy is dead
            try{
                ejbObject.businessMethod("Should throw an exception");
                assertTrue( "Calling business method after removing the EJBObject does not throw an exception", false );
            } catch (Exception e){
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        } finally{
            ejbObject = null;
        }
    }
    //
    // Test ejb object methods
    //===============================


}
