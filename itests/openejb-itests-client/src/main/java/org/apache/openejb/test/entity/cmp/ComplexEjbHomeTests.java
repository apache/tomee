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

import javax.ejb.EJBMetaData;
import javax.ejb.ObjectNotFoundException;
import javax.rmi.PortableRemoteObject;

/**
 * [3] Should be run as the third test suite of the ComplexCmpTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ComplexEjbHomeTests extends ComplexCmpTestClient {

    public ComplexEjbHomeTests() {
        super("EJBHome.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp/ComplexCmpHome");
        ejbHome = (ComplexCmpHome) PortableRemoteObject.narrow(obj, ComplexCmpHome.class);
        ejbObject = ejbHome.createObject("Second Bean");
        ejbPrimaryKey = ejbObject.getPrimaryKey();
    }

    //===============================
    // Test ejb home methods
    //
    public void test01_getEJBMetaData() {
        try {
            EJBMetaData ejbMetaData = ejbHome.getEJBMetaData();
            assertNotNull("The EJBMetaData is null", ejbMetaData);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_getHomeHandle() {
        try {
            ejbHomeHandle = ejbHome.getHomeHandle();
            assertNotNull("The HomeHandle is null", ejbHomeHandle);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_removeByPrimaryKey(){
        try{
            // remove the ejb
            ejbHome.remove(ejbPrimaryKey);

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

            // create a new ejb for the next test
            ejbObject = ejbHome.createObject("Second Bean");
            ejbPrimaryKey = ejbObject.getPrimaryKey();
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_removeByPrimaryHandle(){
        try{
            // remove the ejb
            ejbHome.remove(ejbObject.getHandle());

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

            // create a new ejb for the next test
            ejbObject = ejbHome.createObject("Second Bean");
            ejbPrimaryKey = ejbObject.getPrimaryKey();
        } catch (Exception e){
            e.printStackTrace();
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test05_ejbHomeMethod() {
        try {
            assertEquals(8+9, ejbHome.sum(8, 9));
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test ejb home methods
    //===============================
}
