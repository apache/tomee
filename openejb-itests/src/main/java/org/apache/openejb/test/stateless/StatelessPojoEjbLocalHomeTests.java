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

import javax.ejb.EJBException;


/**
 * [3] Should be run as the third test suite of the BasicStatelessTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessPojoEjbLocalHomeTests extends BasicStatelessLocalTestClient {

    public StatelessPojoEjbLocalHomeTests(){
        super("PojoEJBLocalHome.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        ejbLocalHome = (BasicStatelessLocalHome) initialContext.lookup("client/tests/stateless/BasicStatelessPojoHomeLocal");
    }

    //===============================
    // Test ejb local-home methods
    //

    /**
     * ------------------------------------
     * 5.3.2 Removing a session object
     * A client may remove a session object using the remove() method on the javax.ejb.EJBObject
     * interface, or the remove(Handle handle) method of the javax.ejb.EJBHome interface.
     *
     * Because session objects do not have primary keys that are accessible to clients, invoking the
     * javax.ejb.EJBHome.remove(Object primaryKey) method on a session results in the
     * javax.ejb.RemoveException.
     *
     * ------------------------------------
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
     * ------------------------------------
     *
     * Sections 5.3.2 and 5.5 conflict.  5.3.2 says to throw javax.ejb.RemoveException, 5.5 says to
     * throw java.rmi.RemoteException.
     *
     * For now, we are going with java.rmi.RemoteException.
     * ==============================================================================================
     * TODO - MNour: Please add related sections from EJB3.0 Core contracts and requirements specification
     * 		(Sections: 3.6.2.2, 3.6.3.2 and 3.6.5)
     */
    public void test03_removeByPrimaryKey(){
        try{
            ejbLocalHome.remove("primaryKey");
        } catch (EJBException e){
            assertTrue( true );
            return;
        } catch (Exception e){
            fail("Received "+e.getClass()+" instead of javax.ejb.EJBException");
        }
        assertTrue("javax.ejb.EJBException should have been thrown", false );
    }
    //
    // Test ejb local-home methods
    //===============================
}
