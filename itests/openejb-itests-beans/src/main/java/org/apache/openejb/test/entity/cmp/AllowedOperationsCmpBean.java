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
package org.apache.openejb.test.entity.cmp;

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openejb.test.object.OperationsPolicy;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class AllowedOperationsCmpBean implements EntityBean{
    private static int nextId;
    public Integer primaryKey;
    public String firstName;
    public String lastName;
    public int number;
    public EntityContext ejbContext;
    public static Map<String,OperationsPolicy> allowedOperationsTable = new TreeMap<String,OperationsPolicy>();
    
    
    //=============================
    // Home interface methods
    //    
    
    /**
     * Maps to BasicCmpHome.sum
     * 
     * Adds x and y and returns the result.
     * 
     * @param x
     * @param y
     * @return x + y
     * @see BasicCmpHome#sum
     */
    public int ejbHomeSum(int x, int y) {
        testAllowedOperations("ejbHome");
        return x+y;
    }
    
    public void ejbHomeVoidSelect() {
    }

    /**
     * Maps to BasicCmpHome.create
     */
    public Integer ejbCreateObject(String name) throws CreateException{
        primaryKey = nextId++;
        testAllowedOperations("ejbCreate");
        StringTokenizer st = new StringTokenizer(name, " ");    
        firstName = st.nextToken();
        lastName = st.nextToken();
        return new Integer(primaryKey);
    }
    
    public void ejbPostCreateObject(String name) throws CreateException{
        testAllowedOperations("ejbPostCreate");
    }

    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    /**
     * Maps to BasicCmpObject.businessMethod
     * 
     * @return 
     * @see BasicCmpObject#businessMethod
     */
    public String businessMethod(String text){
        testAllowedOperations("businessMethod");
        number++;
        StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }

    /**
     * Throws an ApplicationException when invoked
     * 
     */
    public void throwApplicationException() throws org.apache.openejb.test.ApplicationException{
        throw new org.apache.openejb.test.ApplicationException("Don't Panic");
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
     * Maps to BasicCmpObject.getPermissionsReport
     * 
     * Returns a report of the bean's
     * runtime permissions
     * 
     * @return 
     * @see BasicCmpObject#getPermissionsReport
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }
    
    /**
     * Maps to BasicCmpObject.getAllowedOperationsReport
     * 
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     * @return 
     * @see BasicCmpObject#getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return allowedOperationsTable.get(methodName);
    }
    
    //    
    // Remote interface methods
    //=============================


    //================================
    // EntityBean interface methods
    //    
    
    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by loading it state from the
     * underlying database.
     */
    public void ejbLoad() throws EJBException,RemoteException {
        testAllowedOperations("ejbLoad");
    }
    
    /**
     * Set the associated entity context. The container invokes this method
     * on an instance after the instance has been created.
     */
    public void setEntityContext(EntityContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
        testAllowedOperations("setEntityContext");
    }
    
    /**
     * Unset the associated entity context. The container calls this method
     * before removing the instance.
     */
    public void unsetEntityContext() throws EJBException,RemoteException {
        testAllowedOperations("unsetEntityContext");
    }
    
    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by storing it to the underlying
     * database.
     */
    public void ejbStore() throws EJBException,RemoteException {
        testAllowedOperations("ejbStore");
    }
    
    /**
     * A container invokes this method before it removes the EJB object
     * that is currently associated with the instance. This method
     * is invoked when a client invokes a remove operation on the
     * enterprise Bean's home interface or the EJB object's remote interface.
     * This method transitions the instance from the ready state to the pool
     * of available instances.
     */
    public void ejbRemove() throws RemoveException,EJBException,RemoteException {
        testAllowedOperations("ejbRemove");
    }
    
    /**
     * A container invokes this method when the instance
     * is taken out of the pool of available instances to become associated
     * with a specific EJB object. This method transitions the instance to
     * the ready state.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbActivate");
    }
    
    /**
     * A container invokes this method on an instance before the instance
     * becomes disassociated with a specific EJB object. After this method
     * completes, the container will place the instance into the pool of
     * available instances.
     */
    public void ejbPassivate() throws EJBException,RemoteException {

        testAllowedOperations("ejbPassivate");
    }
    //    
    // EntityBean interface methods
    //================================
    protected void testAllowedOperations(String methodName) {
        OperationsPolicy policy = new OperationsPolicy();

        /*[0] Test getEJBHome /////////////////*/
        try {
            ejbContext.getEJBHome();
            policy.allow(OperationsPolicy.Context_getEJBHome);
        } catch (IllegalStateException ise) {
        }

        /*[1] Test getCallerPrincipal /////////*/
        try {
            ejbContext.getCallerPrincipal();
            policy.allow(OperationsPolicy.Context_getCallerPrincipal );
        } catch (IllegalStateException ise) {
        }

        /*[2] Test isCallerInRole /////////////*/
        try {
            ejbContext.isCallerInRole("TheMan");
            policy.allow(OperationsPolicy.Context_isCallerInRole );
        } catch (IllegalStateException ise) {
        }

        /*[3] Test getRollbackOnly ////////////*/
        try {
            ejbContext.getRollbackOnly();
            policy.allow(OperationsPolicy.Context_getRollbackOnly );
        } catch (IllegalStateException ise) {
        }

        /*[4] Test setRollbackOnly ////////////*/
//        try {
//            ejbContext.setRollbackOnly();
//            policy.allow(OperationsPolicy.Context_setRollbackOnly );
//        } catch (IllegalStateException ise) {
//        }
	
        /*[5] Test getUserTransaction /////////*/
        try {
            ejbContext.getUserTransaction();
            policy.allow(OperationsPolicy.Context_getUserTransaction );
        } catch (IllegalStateException ise) {
        }

        /*[6] Test getEJBObject ///////////////*/
        try {
            ejbContext.getEJBObject();
            policy.allow(OperationsPolicy.Context_getEJBObject );
        } catch (IllegalStateException ise) {
        }

        /*[7] Test Context_getPrimaryKey ///////////////
         *
         * TODO: Write this test.
         */
        try {
            ejbContext.getPrimaryKey();
            policy.allow(OperationsPolicy.Context_getPrimaryKey );
        } catch (IllegalStateException ise) {
        }

        /*[8] Test JNDI_access_to_java_comp_env ///////////////*/
        try {
            InitialContext jndiContext = new InitialContext();

            jndiContext.lookup("java:comp/env/stateless/references/JNDI_access_to_java_comp_env");

            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env );
        } catch (IllegalStateException ise) {
        } catch (NamingException ne) {
        }

        allowedOperationsTable.put(methodName, policy);
    }
}
