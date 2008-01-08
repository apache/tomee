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
 * [8] Should be run as the eigth test suite of the BasicStatelessTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessEjbMetaDataTests extends BasicStatelessTestClient{

    public StatelessEjbMetaDataTests(){
        super("EJBMetaData.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
        ejbMetaData = ejbHome.getEJBMetaData();
    }

    //=================================
    // Test meta data methods
    //
    public void test01_getEJBHome(){
        try{

        EJBHome home = ejbMetaData.getEJBHome();
        assertNotNull( "The EJBHome is null", home );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_getHomeInterfaceClass(){
        try{
        Class clazz = ejbMetaData.getHomeInterfaceClass();
        assertNotNull( "The Home Interface class is null", clazz );
        assertEquals(clazz , BasicStatelessHome.class);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    /**
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
     */
    public void test03_getPrimaryKeyClass(){
        try{
            Class clazz = ejbMetaData.getPrimaryKeyClass();
            assertNull("Should not return a primary key.  Method should throw an java.lang.RuntimeException", clazz );
        } catch (UnsupportedOperationException e){
            assertTrue( true );
            return;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        assertTrue( "Method should throw an java.lang.RuntimeException", false );
    }

    public void test04_getRemoteInterfaceClass(){
        try{
        Class clazz = ejbMetaData.getRemoteInterfaceClass();
        assertNotNull( "The Remote Interface class is null", clazz );
        assertEquals(clazz , BasicStatelessObject.class);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test05_isSession(){
        try{
        assertTrue( "EJBMetaData says this is not a session bean", ejbMetaData.isSession() );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test06_isStatelessSession(){
        try{
        assertTrue( "EJBMetaData says this is not a stateless session bean", ejbMetaData.isStatelessSession() );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test meta data methods
    //=================================
}
