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

import javax.ejb.EJBObject;

import org.apache.openejb.test.object.OperationsPolicy;

/**
 * 
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
 *                       |
 * constructor           | -
 * ______________________|__________________________________________________
 *                       |
 * setSessionContext     |  SessionContext methods:
 *                       |     - getEJBHome
 *                       |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 *                       |
 * ejbCreate             |  SessionContext methods:
 * ejbRemove             |     - getEJBHome
 *                       |     - getEJBObject
 *                       |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 *                       |
 * business method       |  SessionContext methods:
 * from remote interface |     - getEJBHome        
 *                       |     - getCallerPrincipal
 *                       |     - getRollbackOnly   
 *                       |     - isCallerInRole    
 *                       |     - setRollbackOnly   
 *                       |     - getEJBObject      
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessAllowedOperationsTests extends BasicStatelessTestClient{

    public StatelessAllowedOperationsTests(){
        super("AllowedOperations.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
        ejbObject = ejbHome.create();
        ejbHandle = ejbObject.getHandle();
        //setUp_ejbActivate_Passivate();        
        
        /* These tests will only work if the specified
         * method has already been called by the container.
         *
         * TO DO:
         * Implement a little application senario to ensure
         * that all methods tested for below have been called
         * by the container.
         */         
        ejbObject.businessMethod("activate me please");
    }
    
    private void setUp_ejbActivate_Passivate() throws Exception{

        /* Create more instances to fill the pool size 
         * causing instances to be passivated
         */
        EJBObject[] ejbObjects = new EJBObject[10];
        for (int i=0; i < ejbObjects.length; i++){
            ejbObjects[i] = ejbHome.create();
        }
        ejbObject.businessMethod("activate me please");
    }
    
    protected void tearDown() throws Exception{
        try {
            ejbObject.remove();
        } catch (Exception e){
            throw e;
        } finally {
            super.tearDown();
        }
    }

    //=====================================
    // Test EJBContext allowed operations       
    //
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * setSessionContext     |  SessionContext methods:
     *                       |     - getEJBHome
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test01_setSessionContext(){
        try {
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow( OperationsPolicy.Context_getEJBHome );
            policy.allow( OperationsPolicy.JNDI_access_to_java_comp_env );
            
            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("setSessionContext");
            
            assertNotNull("The OperationsPolicy is null", actual );
            assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     *                       |     - getEJBObject
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test02_ejbCreate() {
	// The stateless session bean has container managed transactions
	// so, the test Context_getUserTransaction should fail, but,
	// it does not.  Someone should see why it does not fail.
        try {
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow( OperationsPolicy.Context_getEJBHome );
            policy.allow( OperationsPolicy.Context_getEJBObject );
            policy.allow( OperationsPolicy.JNDI_access_to_java_comp_env );
            
            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbCreate");
            
            assertNotNull("The OperationsPolicy is null", actual );
            assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     *                       |     - getEJBObject
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test03_ejbRemove(){
        try {
            /* TO DO:  This test needs unique functionality to work */
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow( OperationsPolicy.Context_getEJBHome );
            policy.allow( OperationsPolicy.Context_getEJBObject );
            policy.allow( OperationsPolicy.JNDI_access_to_java_comp_env );
        
            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("ejbRemove");
        
            assertNotNull("The OperationsPolicy is null", actual );
            assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * business method       |  SessionContext methods:
     * from remote interface |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |     - getEJBObject
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test04_businessMethod(){
        try {
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow( OperationsPolicy.Context_getEJBHome );
            policy.allow( OperationsPolicy.Context_getCallerPrincipal );
            policy.allow( OperationsPolicy.Context_getRollbackOnly );
            policy.allow( OperationsPolicy.Context_isCallerInRole );
            policy.allow( OperationsPolicy.Context_setRollbackOnly );
            policy.allow( OperationsPolicy.Context_getEJBObject );
            policy.allow( OperationsPolicy.JNDI_access_to_java_comp_env );
            policy.allow( OperationsPolicy.Resource_manager_access );
            policy.allow( OperationsPolicy.Enterprise_bean_access );
        
            Object expected = policy;
            Object actual = ejbObject.getAllowedOperationsReport("businessMethod");
        
            assertNotNull("The OperationsPolicy is null", actual );
            assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test EJBContext allowed operations       
    //=====================================
}


