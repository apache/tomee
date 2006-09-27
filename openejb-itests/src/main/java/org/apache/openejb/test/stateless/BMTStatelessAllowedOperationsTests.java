package org.apache.openejb.test.stateless;

import org.apache.openejb.test.object.OperationsPolicy;

/**
 * 
 * [10] Should be run as the nineth test suite of the BasicStatelessTestClients
 * 
 * <PRE>
 * =========================================================================
 * Operations allowed in the methods of a stateless SessionBean with 
 * bean-managed transaction demarcation
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
 *                       |     - getUserTransaction
 *                       |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 *                       |
 * business method       |  SessionContext methods:
 * from remote interface |     - getEJBHome        
 *                       |     - getCallerPrincipal
 *                       |     - isCallerInRole    
 *                       |     - getEJBObject      
 *                       |     - getUserTransaction
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BMTStatelessAllowedOperationsTests extends BasicStatelessTestClient{

    public BMTStatelessAllowedOperationsTests(){
        super("BMTAllowedOperations.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BeanManagedBasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
        ejbObject = ejbHome.create();
        ejbHandle = ejbObject.getHandle();
        ejbObject.businessMethod("let's go!");
        /* These tests will only work if the specified
         * method has already been called by the container.
         *
         * TO DO:
         * Implement a little application senario to ensure
         * that all methods tested for below have been called
         * by the container.
         */         
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
        try{
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow( policy.Context_getEJBHome );
            policy.allow( policy.JNDI_access_to_java_comp_env );
            
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
     *                       |     - getUserTransaction
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test02_ejbCreate(){             
        try{
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow( policy.Context_getEJBHome );
            policy.allow( policy.Context_getEJBObject );
            policy.allow( policy.Context_getUserTransaction );
            policy.allow( policy.JNDI_access_to_java_comp_env );
            
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
     *                       |     - getUserTransaction
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void TODO_test03_ejbRemove(){             
        try{
            /* TO DO:  This test needs unique functionality to work */
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow( policy.Context_getEJBHome );
            policy.allow( policy.Context_getEJBObject );
            policy.allow( policy.Context_getUserTransaction );
            policy.allow( policy.JNDI_access_to_java_comp_env );
        
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
     *                       |     - isCallerInRole
     *                       |     - getEJBObject
     *                       |     - getUserTransaction
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test04_businessMethod(){
        try{
            OperationsPolicy policy = new OperationsPolicy();
            policy.allow( policy.Context_getEJBHome );
            policy.allow( policy.Context_getCallerPrincipal );
            policy.allow( policy.Context_isCallerInRole );
            policy.allow( policy.Context_getEJBObject );
            policy.allow( policy.Context_getUserTransaction );
            policy.allow( policy.JNDI_access_to_java_comp_env );
            policy.allow( policy.Resource_manager_access );
            policy.allow( policy.Enterprise_bean_access );
        
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


