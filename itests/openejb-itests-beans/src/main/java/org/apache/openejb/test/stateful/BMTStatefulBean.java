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
package org.apache.openejb.test.stateful;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;

import org.apache.openejb.test.ApplicationException;
import org.apache.openejb.test.object.OperationsPolicy;

/**
 * A Stateful SessionBean with bean-managed transaction demarcation
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BMTStatefulBean implements javax.ejb.SessionBean {
    
    private String name;
    private SessionContext ejbContext;
    private Hashtable allowedOperationsTable = new Hashtable();
    
    
    //=============================
    // Home interface methods
    //    
    /**
     * Maps to BasicStatefulHome.create
     * 
     * @param name
     * @exception javax.ejb.CreateException
     * @see BasicStatefulHome#createObject
     */
    public void ejbCreateObject(String name)
    throws javax.ejb.CreateException{
        testAllowedOperations("ejbCreate");
        this.name = name;
    }
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    /**
     * Maps to BasicStatefulObject.businessMethod
     * 
     * @return 
     * @see BasicStatefulObject#businessMethod
     */
    public String businessMethod(String text){
        testAllowedOperations("businessMethod");
        StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }

    /**
     * Throws an ApplicationException when invoked
     * 
     */
    public void throwApplicationException() throws ApplicationException{
        throw new ApplicationException("Don't Panic");
    }
    
    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the 
     * destruction of the instance and invalidation of the
     * remote reference.
     * 
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Panic");
    }
    
    /**
     * Maps to BasicStatefulObject.getPermissionsReport
     * 
     * Returns a report of the bean's
     * runtime permissions
     * 
     * @return 
     * @see BasicStatefulObject#getPermissionsReport
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }
    
    /**
     * Maps to BasicStatefulObject.getAllowedOperationsReport
     * 
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     * @return 
     * @see BasicStatefulObject#getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }
    
    //    
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
        testAllowedOperations("setSessionContext");
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
        testAllowedOperations("ejbRemove");
    }
    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbActivate");
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbPassivate");
    }
    //    
    // StatefulBean interface methods
    //==================================

    protected void testAllowedOperations(String methodName){
        OperationsPolicy policy = new OperationsPolicy();
        
        /*[1] Test getEJBHome /////////////////*/ 
        try{
            ejbContext.getEJBHome();
            policy.allow(policy.Context_getEJBHome);
        }catch(IllegalStateException ise){}
        
        /*[2] Test getCallerPrincipal /////////*/ 
        try{
            ejbContext.getCallerPrincipal();
            policy.allow( policy.Context_getCallerPrincipal );
        }catch(IllegalStateException ise){}
        
        /*[3] Test isCallerInRole /////////////*/ 
        try{
            ejbContext.isCallerInRole("ROLE");
            policy.allow( policy.Context_isCallerInRole );
        }catch(IllegalStateException ise){}
        
        /*[4] Test getRollbackOnly ////////////*/ 
        try{
            ejbContext.getRollbackOnly();
            policy.allow( policy.Context_getRollbackOnly );
        }catch(IllegalStateException ise){}
        
        /*[5] Test setRollbackOnly ////////////*/ 
        try{
            ejbContext.setRollbackOnly();
            policy.allow( policy.Context_setRollbackOnly );
        }catch(IllegalStateException ise){}
        
        /*[6] Test getUserTransaction /////////*/ 
        try{
            ejbContext.getUserTransaction();
            policy.allow( policy.Context_getUserTransaction );
        }catch(IllegalStateException ise){}
        
        /*[7] Test getEJBObject ///////////////*/ 
        try{
            ejbContext.getEJBObject();
            policy.allow( policy.Context_getEJBObject );
        }catch(IllegalStateException ise){}
         
        /* TO DO:  
         * Check for policy.Enterprise_bean_access       
         * Check for policy.JNDI_access_to_java_comp_env 
         * Check for policy.Resource_manager_access      
         */
        allowedOperationsTable.put(methodName, policy);
    }
}
