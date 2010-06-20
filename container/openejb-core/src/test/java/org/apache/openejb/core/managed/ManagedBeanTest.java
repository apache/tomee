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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.managed;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * This test case serves as a nice tiny template for other test cases
 * and purposely doesn't do anything very complicated.
 *
 * @version $Rev$ $Date$
 */
public class ManagedBeanTest extends TestCase {

    @Override
    protected void setUp() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new ManagedBean(MyBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    public void test() throws Exception {
        InitialContext context = new InitialContext();
        MyBean myBean = (MyBean) context.lookup("MyBeanLocalBean");

        assertEquals("pan", myBean.echo("nap"));

        //TODO -- implement this
//        assertTrue(myBean.noTransaction());
//        assertTrue(myBean.inTransaction());
//
//        try {
//            myBean.noDestroyBean();
//            fail("Should have thrown MyRuntimeException");
//        } catch (MyRuntimeException e) {
//            // good -- this was expected
//        }
//
//        // Instance should still be alive
//        assertEquals("pan", myBean.echo("nap"));
    }

    public static class MyRuntimeException extends RuntimeException {
        public MyRuntimeException(String message) {
            super(message);
        }
    }

    public static class MyBean {

        /**
         * This should not remove the bean
         */
        public void noDestroyBean() {
            throw new MyRuntimeException("I threw an exception");
        }

        /**
         * Plain business method to test bean is still alive
         *
         * @param string a string to reverse
         * @return the reversed string
         */
        public String echo(String string) {
            StringBuilder sb = new StringBuilder(string);
            return sb.reverse().toString();
        }

        /**
         * This method should not run in a transaction
         *
         * @return true if not in Transaction
         */
        public boolean noTransaction() {
            return !inTransaction();
        }

        private boolean inTransaction() {
            try {
                TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
                int status = transactionManager.getStatus();
                return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * This method should run in a transaction
         *
         * @return true if in Transaction
         */
        @TransactionAttribute(REQUIRED)
        public boolean withTransaction() {
            return inTransaction();
        }

    }
}