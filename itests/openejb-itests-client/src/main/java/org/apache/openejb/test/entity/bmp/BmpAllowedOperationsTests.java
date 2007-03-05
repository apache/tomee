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
package org.apache.openejb.test.entity.bmp;

import org.apache.openejb.test.object.OperationsPolicy;

/**
 * [9] Should be run as the nineth test suite of the BasicBmpTestClients
 * 
 * <PRE>
 * =========================================================================
 * Operations allowed in the methods of an entity bean
 * =========================================================================
 * 
 * Bean method           | Bean method can perform the following operations
 * ______________________|__________________________________________________
 *                       |
 * constructor           | -
 * ______________________|__________________________________________________
 *                       |
 * setEntityContext      |  EntityContext methods: 
 * unsetEntityContext    |     - getEJBHome
 *                       |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 *                       |
 * ejbCreate             |  EntityContext methods: 
 *                       |     - getEJBHome 
 *                       |     - getCallerPrincipal
 *                       |     - getRollbackOnly
 *                       |     - isCallerInRole
 *                       |     - setRollbackOnly
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 *                       |
 * ejbPostCreate         |  EntityContext methods: 
 *                       |     - getEJBHome 
 *                       |     - getCallerPrincipal
 *                       |     - getRollbackOnly
 *                       |     - isCallerInRole
 *                       |     - setRollbackOnly
 *                       |     - getEJBObject
 *                       |     - getPrimaryKey
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 *                       |
 * ejbRemove             |  EntityContext methods: 
 *                       |     - getEJBHome 
 *                       |     - getCallerPrincipal
 *                       |     - getRollbackOnly
 *                       |     - isCallerInRole
 *                       |     - setRollbackOnly
 *                       |     - getEJBObject
 *                       |     - getPrimaryKey
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 *                       | 
 * ejbFind*              |  EntityContext methods: 
 * ejbSelect*            |     - getEJBHome                                                           
 * ejbHome               |     - getCallerPrincipal                                   
 *                       |     - getRollbackOnly                                      
 *                       |     - isCallerInRole                                       
 *                       |     - setRollbackOnly                                      
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 *                       |
 * ejbSelectInEntity*    |  EntityContext methods:
 *                       |     - getEJBHome 
 *                       |     - getCallerPrincipal
 *                       |     - getRollbackOnly
 *                       |     - isCallerInRole
 *                       |     - setRollbackOnly
 *                       |     - getEJBObject
 *                       |     - getPrimaryKey
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 *                       |
 * ejbActivate           |  EntityContext methods: 
 * ejbPassivate          |     - getEJBHome 
 *                       |     - getEJBObject
 *                       |     - getPrimaryKey
 *                       |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 *                       |
 * ejbLoad               |  EntityContext methods: 
 * ejbStore              |     - getEJBHome        
 *                       |     - getCallerPrincipal
 *                       |     - getRollbackOnly   
 *                       |     - isCallerInRole    
 *                       |     - setRollbackOnly   
 *                       |     - getEJBObject      
 *                       |     - getPrimaryKey     
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 *                       |
 * business method       |  EntityContext methods:
 * from remote interface |     - getEJBHome           
 *                       |     - getCallerPrincipal
 *                       |     - getRollbackOnly   
 *                       |     - isCallerInRole    
 *                       |     - setRollbackOnly   
 *                       |     - getEJBObject      
 *                       |     - getPrimaryKey     
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>                
 *                       
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BmpAllowedOperationsTests extends BasicBmpTestClient{

    public BmpAllowedOperationsTests(){
        super("AllowedOperations.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/bmp/allowed_operations/EntityHome");
        ejbHome = (BasicBmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicBmpHome.class);
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
    
    protected void tearDown() throws Exception{
        ejbObject.remove();
        super.tearDown();
    }
    
    /**
     * This method ensures that all thee bean methods have been invoked for correct behaviour
     * of the tests. 
     *
     */  
    private void doScenario() throws Exception{
      
      // Call the business method
      ejbObject.businessMethod("Reverse Me");
      
      ejbHome.findByPrimaryKey(null);
      
      // TO BE FIXED LATER IN PROXIES
      /*try {
        ejbHome.sum(1, 2); 
      }catch( java.lang.Exception e ) {e.printStackTrace();} */
                
      ejbObject = (BasicBmpObject)javax.rmi.PortableRemoteObject.narrow(ejbHandle.getEJBObject(), BasicBmpObject.class);
     
      ejbHome.findByPrimaryKey((Integer)ejbObject.getPrimaryKey());
      ejbHome.remove((Integer)ejbObject.getPrimaryKey());
      
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
     *                       |
     * setEntityContext      |  EntityContext methods:
     * unsetEntityContext    |     - getEJBHome
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test01_setEntityContext(){     
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.JNDI_access_to_java_comp_env );
        
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("setEntityContext");
        
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * setEntityContext      |  EntityContext methods: 
     * unsetEntityContext    |     - getEJBHome
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test02_unsetEntityContext(){ 
        try{
            
        /* TO DO:  This test needs unique functionality to work */
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.JNDI_access_to_java_comp_env );
        
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("unsetEntityContext");
        
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbCreate             |  EntityContext methods:
     *                       |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test03_ejbCreate(){   
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbCreate");
        
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbPostCreate         |  EntityContext methods:
     *                       |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |     - getEJBObject
     *                       |     - getPrimaryKey
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test04_ejbPostCreate(){     
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getPrimaryKey );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbPostCreate");
    
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbRemove             |  EntityContext methods:
     *                       |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |     - getEJBObject
     *                       |     - getPrimaryKey
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void _test05_ejbRemove(){   
        try{ 
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getPrimaryKey );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbRemove");
    
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbFind*              |  EntityContext methods:
     * ejbSelect*            |     - getEJBHome
     * ejbHome               |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test06_ejbFind(){      
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbFind");
        
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbFind*              |  EntityContext methods:
     * ejbSelect*            |     - getEJBHome
     * ejbHome               |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test07_ejbSelect(){     
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbSelect");
        
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbFind*              |  EntityContext methods:
     * ejbSelect*            |     - getEJBHome
     * ejbHome               |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test08_ejbHome(){   
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbHome");
    
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbSelectInEntity*    |  EntityContext methods:
     *                       |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |     - getEJBObject
     *                       |     - getPrimaryKey
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test09_ejbSelectInEntity(){  
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getPrimaryKey );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbSelectInEntity");
    
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbActivate           |  EntityContext methods:
     * ejbPassivate          |     - getEJBHome
     *                       |     - getEJBObject
     *                       |     - getPrimaryKey
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test10_ejbActivate(){ 
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getPrimaryKey );
        policy.allow( policy.JNDI_access_to_java_comp_env );
    
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbActivate");
    
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbActivate           |  EntityContext methods:
     * ejbPassivate          |     - getEJBHome
     *                       |     - getEJBObject
     *                       |     - getPrimaryKey
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test11_ejbPassivate(){          
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getPrimaryKey );
        policy.allow( policy.JNDI_access_to_java_comp_env );
    
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbPassivate");
    
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbLoad               |  EntityContext methods:
     * ejbStore              |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |     - getEJBObject
     *                       |     - getPrimaryKey
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test12_ejbLoad(){  
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getPrimaryKey );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbLoad");
    
        assertNotNull("The OpperationsPolicy is null", actual );
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
     * ejbLoad               |  EntityContext methods:
     * ejbStore              |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |     - getEJBObject
     *                       |     - getPrimaryKey
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void BUG_test13_ejbStore(){  
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getPrimaryKey );
        policy.allow( policy.JNDI_access_to_java_comp_env );

        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbStore");
    
        assertNotNull( "The OperationsPolicy returned is null", actual);
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
     * business method       |  EntityContext methods:
     * from remote interface |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - getRollbackOnly
     *                       |     - isCallerInRole
     *                       |     - setRollbackOnly
     *                       |     - getEJBObject
     *                       |     - getPrimaryKey
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test14_businessMethod(){
        try{
            
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        //TODO:0:policy.allow( policy.Context_getRollbackOnly );
        policy.allow( policy.Context_isCallerInRole );
        //TODO:0:policy.allow( policy.Context_setRollbackOnly );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getPrimaryKey );
        policy.allow( policy.JNDI_access_to_java_comp_env );
    
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("businessMethod");
    
        assertNotNull("The OpperationsPolicy is null", actual );
        assertEquals( expected, actual );
  
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test EJBContext allowed operations       
    //=====================================
}


