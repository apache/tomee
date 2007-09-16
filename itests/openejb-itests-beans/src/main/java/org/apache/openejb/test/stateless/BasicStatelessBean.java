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

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openejb.test.ApplicationException;
import org.apache.openejb.test.beans.TimerSync;
import org.apache.openejb.test.object.OperationsPolicy;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BasicStatelessBean implements SessionBean, TimedObject {

    private String name;
    private SessionContext ejbContext;
    private Hashtable allowedOperationsTable = new Hashtable();

    //=============================
    // Home interface methods
    //    
    //    
    // Home interface methods
    //=============================

    //=============================
    // Remote interface methods
    //    

    /**
     * Maps to BasicStatelessObject.businessMethod
     *
     * @return
     * @see BasicStatelessObject#businessMethod
     */
    public String businessMethod(String text) {
        testAllowedOperations("businessMethod");
        StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }

    public void scheduleTimer(String name) {
        ejbContext.getTimerService().createTimer(1, name);
    }

    /**
     * Throws an ApplicationException when invoked
     */
    public void throwApplicationException() throws ApplicationException {
        throw new ApplicationException("Testing ability to throw Application Exceptions");
    }

    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the
     * destruction of the instance and invalidation of the
     * remote reference.
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Testing ability to throw System Exceptions");
    }

    /**
     * Maps to BasicStatelessObject.getPermissionsReport
     * <p/>
     * Returns a report of the bean's
     * runtime permissions
     *
     * @return
     * @see BasicStatelessObject#getPermissionsReport
     */
    public Properties getPermissionsReport() {
        /* TO DO: */
        return null;
    }

    /**
     * Maps to BasicStatelessObject.getAllowedOperationsReport
     * <p/>
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     *
     * @param methodName The method for which to get the allowed opperations report
     * @return
     * @see BasicStatelessObject#getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName) {
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }
    
    public String remove(String str){
        return str;
    }    

    //    
    // Remote interface methods
    //=============================

    //================================
    // SessionBean interface methods
    //    

    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
        ejbContext = ctx;
        testAllowedOperations("setSessionContext");
    }

    /**
     * @throws javax.ejb.CreateException
     */
    public void ejbCreateObject() throws javax.ejb.CreateException {
        testAllowedOperations("ejbCreate");
        this.name = "nameless automaton";
    }

    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException, RemoteException {
        testAllowedOperations("ejbRemove");
    }

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException, RemoteException {
        testAllowedOperations("ejbActivate");
        // Should never called.
    }

    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException, RemoteException {
        testAllowedOperations("ejbPassivate");
        // Should never called.
    }

    public void ejbTimeout(Timer timer) {
        testAllowedOperations("ejbTimeout");
        try {
            String name = (String) timer.getInfo();
            TimerSync timerSync = (TimerSync) ejbContext.lookup("TimerSyncBeanBusinessRemote");
            timerSync.countDown(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // SessionBean interface methods
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
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
        } catch (IllegalStateException ise) {
        }

        /*[2] Test isCallerInRole /////////////*/
        try {
            ejbContext.isCallerInRole("TheMan");
            policy.allow(OperationsPolicy.Context_isCallerInRole);
        } catch (IllegalStateException ise) {
        }

        /*[3] Test getRollbackOnly ////////////*/
        try {
            ejbContext.getRollbackOnly();
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
        } catch (IllegalStateException ise) {
        }

        /*[4] Test setRollbackOnly ////////////*/
        try {
            ejbContext.setRollbackOnly();
            policy.allow(OperationsPolicy.Context_setRollbackOnly);
        } catch (IllegalStateException ise) {
        }

        /*[5] Test getUserTransaction /////////*/
        try {
            ejbContext.getUserTransaction();
            policy.allow(OperationsPolicy.Context_getUserTransaction);
        } catch (IllegalStateException ise) {
        }

        /*[6] Test getEJBObject ///////////////*/
        try {
            ejbContext.getEJBObject();
            policy.allow(OperationsPolicy.Context_getEJBObject);
        } catch (IllegalStateException ise) {
        }

        /*[7] Test Context_getPrimaryKey ///////////////
                  *
                  * Can't really do this
                  */

        /*[8] Test JNDI_access_to_java_comp_env ///////////////*/
        try {
            InitialContext jndiContext = new InitialContext();

            String actual = (String) jndiContext.lookup("java:comp/env/stateless/references/JNDI_access_to_java_comp_env");

            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
        } catch (IllegalStateException ise) {
        } catch (NamingException ne) {
        }

        /*[11] Test lookup /////////*/
        try {
            ejbContext.lookup("stateless/references/JNDI_access_to_java_comp_env");
            policy.allow(OperationsPolicy.Context_lookup);
        } catch (IllegalArgumentException ise) {
        }

        /*[12] Test getTimerService/////////*/
        try {
            ejbContext.getTimerService();
            policy.allow(OperationsPolicy.Context_getTimerService);
        } catch (IllegalStateException ise) {
        }

        allowedOperationsTable.put(methodName, policy);
    }

}
