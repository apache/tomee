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
package org.apache.openejb.test.stateless;

import javax.ejb.EJBHome;

/**
 * [4] Should be run as the fourth test suite of the BasicStatelessTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessEjbObjectTests extends BasicStatelessTestClient{

    public StatelessEjbObjectTests(){
        super("EJBObject.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
        ejbObject = ejbHome.create();
    }

    protected void tearDown() throws Exception {
        try {
            //ejbObject.remove();
        } catch (Exception e){
            throw e;
        } finally {
            super.tearDown();
        }
    }

    //===============================
    // Test ejb object methods
    //
    public void test01_getHandle(){
        try{
            ejbHandle = ejbObject.getHandle();
            assertNotNull( "The Handle is null", ejbHandle );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_isIdentical(){
        try{
            assertTrue( "The EJBObjects are not identical", ejbObject.isIdentical(ejbObject) );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_getEjbHome(){
        try{
            EJBHome home = ejbObject.getEJBHome();
            assertNotNull( "The EJBHome is null", home );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * 5.5 Session object identity
     *
     * Session objects are intended to be private resources used only by the
     * client that created them. For this reason, session objects, from the
     * client’s perspective, appear anonymous. In contrast to entity objects,
     * which expose their identity as a primary key, session objects hide their
     * identity. As a result, the EJBObject.getPrimaryKey() and
     * EJBHome.remove(Object primaryKey) methods result in a java.rmi.RemoteException
     * if called on a session bean. If the EJBMetaData.getPrimaryKeyClass()
     * method is invoked on a EJBMetaData object for a Session bean, the method throws
     * the java.lang.RuntimeException.
     */
    public void test04_getPrimaryKey(){
        try{
            Object key = ejbObject.getPrimaryKey();
        } catch (java.rmi.RemoteException e){
            assertTrue(true);
            return;
        } catch (Exception e){
            fail("A RuntimeException should have been thrown.  Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        fail("A RuntimeException should have been thrown.");
    }

    public void test05_remove(){
        try{
            ejbObject.remove();
            try{
                ejbObject.businessMethod("Should throw an exception");
                assertTrue( "Calling business method after removing the EJBObject does not throw an exception", false );
            } catch (Exception e){
                assertTrue( true );
                return;
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test ejb object methods
    //===============================
}
