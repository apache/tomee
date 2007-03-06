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
package org.apache.openejb.test.mdb;

import org.apache.openejb.test.ApplicationException;
import org.apache.openejb.test.object.OperationsPolicy;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Properties;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BasicMdbBean implements BasicMdbObject, MessageDrivenBean, MessageListener {
	private MessageDrivenContext mdbContext = null;
    private Hashtable allowedOperationsTable = new Hashtable();
    protected MdbInvoker mdbInvoker;


    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        this.mdbContext = ctx;
        testAllowedOperations("setMessageDrivenContext");
        try {
            ConnectionFactory connectionFactory = (ConnectionFactory) new InitialContext().lookup("java:comp/env/jms");
            mdbInvoker = new MdbInvoker(connectionFactory, this);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    public void onMessage(Message message) {
        try {
//            System.out.println("\n" +
//                    "***************************************\n" +
//                    "Got message: " + message + "\n" +
//                    "***************************************\n\n");
            try {
                message.acknowledge();
            } catch (JMSException e) {
                e.printStackTrace();
            }
            mdbInvoker.onMessage(message);
        } catch (Throwable e) {
            e.printStackTrace();  
        }
    }

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
        throw new ApplicationException("Testing ability to throw Application Exceptions");
    }

    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the
     * destruction of the instance and invalidation of the
     * remote reference.
     *
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Testing ability to throw System Exceptions");
    }

    /**
     * Maps to BasicStatelessObject.getPermissionsReport
     *
     * Returns a report of the bean's
     * runtime permissions
     *
     * @return
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }

    /**
     * Maps to BasicStatelessObject.getAllowedOperationsReport
     *
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     *
     * @param methodName The method for which to get the allowed opperations report
     * @return
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }

    //
    // Remote interface methods
    //=============================


    //================================
    // MessageDrivenBean interface methods
    //

    public void ejbCreate() throws javax.ejb.CreateException{
        testAllowedOperations("ejbCreate");
    }

    public void ejbRemove() throws EJBException {
        testAllowedOperations("ejbRemove");
    }

    //
    // SessionBean interface methods
    //================================

	protected void testAllowedOperations(String methodName) {
		OperationsPolicy policy = new OperationsPolicy();

		/*[0] Test getEJBHome /////////////////*/
		try {
			mdbContext.getEJBHome();
			policy.allow(policy.Context_getEJBHome);
		} catch (IllegalStateException ise) {
		}

		/*[1] Test getCallerPrincipal /////////*/
		try {
			mdbContext.getCallerPrincipal();
			policy.allow( policy.Context_getCallerPrincipal );
		} catch (IllegalStateException ise) {
		}

		/*[2] Test isCallerInRole /////////////*/
		try {
			mdbContext.isCallerInRole("ROLE");
			policy.allow( policy.Context_isCallerInRole );
		} catch (IllegalStateException ise) {
		}

		/*[3] Test getRollbackOnly ////////////*/
		try {
			mdbContext.getRollbackOnly();
			policy.allow( policy.Context_getRollbackOnly );
		} catch (IllegalStateException ise) {
		}

		/*[4] Test setRollbackOnly ////////////*/
        // Rollback causes message redelivery

		/*[5] Test getUserTransaction /////////*/
		try {
			mdbContext.getUserTransaction();
			policy.allow( policy.Context_getUserTransaction );
		} catch (IllegalStateException ise) {
		}

		/*[6] Test getEJBObject ///////////////
		 *
		 * MDBs don't have an ejbObject
		 */

		/*[7] Test Context_getPrimaryKey ///////////////
		 *
		 * Can't really do this
		 */

		/*[8] Test JNDI_access_to_java_comp_env ///////////////*/
		try {
			InitialContext jndiContext = new InitialContext();

			String actual = (String)jndiContext.lookup("java:comp/env/stateless/references/JNDI_access_to_java_comp_env");

			policy.allow( policy.JNDI_access_to_java_comp_env );
		} catch (IllegalStateException ise) {
		} catch (javax.naming.NamingException ne) {
		}

        /*[11] Test lookup /////////*/
        try {
            mdbContext.lookup("stateless/references/JNDI_access_to_java_comp_env");
            policy.allow( policy.Context_lookup );
        } catch (IllegalArgumentException ise) {
        }

		allowedOperationsTable.put(methodName, policy);

    }

}
