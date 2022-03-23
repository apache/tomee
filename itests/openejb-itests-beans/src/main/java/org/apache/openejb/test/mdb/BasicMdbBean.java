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

import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.MessageDrivenContext;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import javax.naming.InitialContext;
import java.util.HashMap;
import java.util.Properties;

public class BasicMdbBean implements BasicMdbObject, MessageDrivenBean, MessageListener {
    private MessageDrivenContext mdbContext = null;
    private static final HashMap<String, OperationsPolicy> allowedOperationsTable = new HashMap<String, OperationsPolicy>();
    protected MdbInvoker mdbInvoker;

    @Override
    public void setMessageDrivenContext(final MessageDrivenContext ctx) throws EJBException {
        this.mdbContext = ctx;
        testAllowedOperations("setMessageDrivenContext");
        try {
            final ConnectionFactory connectionFactory = (ConnectionFactory) new InitialContext().lookup("java:comp/env/jms");
            mdbInvoker = new MdbInvoker(connectionFactory, this);
        } catch (final Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public void onMessage(final Message message) {
        try {
//            System.out.println("\n" +
//                    "***************************************\n" +
//                    "Got message: " + message + "\n" +
//                    "***************************************\n\n");
            try {
                message.acknowledge();
            } catch (final JMSException e) {
                e.printStackTrace();
            }
            mdbInvoker.onMessage(message);
        } catch (final Throwable e) {
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
    @Override
    public String businessMethod(final String text) {
        testAllowedOperations("businessMethod");
        final StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }


    /**
     * Throws an ApplicationException when invoked
     */
    @Override
    public void throwApplicationException() throws ApplicationException {
        throw new ApplicationException("Testing ability to throw Application Exceptions");
    }

    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the
     * destruction of the instance and invalidation of the
     * remote reference.
     */
    @Override
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
    @Override
    public Properties getPermissionsReport() {
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
    @Override
    public OperationsPolicy getAllowedOperationsReport(final String methodName) {
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }

    //
    // Remote interface methods
    //=============================


    //================================
    // MessageDrivenBean interface methods
    //

    public void ejbCreate() throws jakarta.ejb.CreateException {
        testAllowedOperations("ejbCreate");
    }

    @Override
    public void ejbRemove() throws EJBException {

        if (null != mdbInvoker) {
            mdbInvoker.destroy();
        }

        testAllowedOperations("ejbRemove");
    }

    //
    // SessionBean interface methods
    //================================

    protected void testAllowedOperations(final String methodName) {
        final OperationsPolicy policy = new OperationsPolicy();

        /*[0] Test getEJBHome /////////////////*/
        try {
            mdbContext.getEJBHome();
            policy.allow(OperationsPolicy.Context_getEJBHome);
        } catch (final IllegalStateException ignored) {
        }

        /*[1] Test getCallerPrincipal /////////*/
        try {
            mdbContext.getCallerPrincipal();
            policy.allow(OperationsPolicy.Context_getCallerPrincipal);
        } catch (final IllegalStateException ignored) {
        }

        /*[2] Test isCallerInRole /////////////*/
        try {
            mdbContext.isCallerInRole("TheMan");
            policy.allow(OperationsPolicy.Context_isCallerInRole);
        } catch (final IllegalStateException ignored) {
        }

        /*[3] Test getRollbackOnly ////////////*/
        try {
            mdbContext.getRollbackOnly();
            policy.allow(OperationsPolicy.Context_getRollbackOnly);
        } catch (final IllegalStateException ignored) {
        }

        /*[4] Test setRollbackOnly ////////////*/
        // Rollback causes message redelivery

        /*[5] Test getUserTransaction /////////*/
        try {
            mdbContext.getUserTransaction();
            policy.allow(OperationsPolicy.Context_getUserTransaction);
        } catch (final IllegalStateException ignored) {
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
            final InitialContext jndiContext = new InitialContext();

            final String actual = (String) jndiContext.lookup("java:comp/env/stateless/references/JNDI_access_to_java_comp_env");

            policy.allow(OperationsPolicy.JNDI_access_to_java_comp_env);
        } catch (final IllegalStateException | javax.naming.NamingException ignored) {
        }

        /*[11] Test lookup /////////*/
        try {
            mdbContext.lookup("stateless/references/JNDI_access_to_java_comp_env");
            policy.allow(OperationsPolicy.Context_lookup);
        } catch (final IllegalArgumentException ignored) {
        }

        allowedOperationsTable.put(methodName, policy);

    }

}
