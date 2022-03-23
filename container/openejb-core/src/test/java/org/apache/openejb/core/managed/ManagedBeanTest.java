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
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.loader.SystemInstance;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.TransactionAttribute;
import javax.naming.InitialContext;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import java.util.concurrent.atomic.AtomicInteger;

import static jakarta.ejb.TransactionAttributeType.REQUIRED;

/**
 * This test case serves as a nice tiny template for other test cases
 * and purposely doesn't do anything very complicated.
 *
 * @version $Rev$ $Date$
 */
public class ManagedBeanTest extends TestCase {

    @Override
    protected void setUp() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new ManagedBean(MyBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        MyBean.instances.set(0);

        final InitialContext context = new InitialContext();
        final MyBean myBean = (MyBean) context.lookup("MyBeanLocalBean");

        assertEquals("pan", myBean.echo("nap"));
        assertEquals(1, MyBean.instances.get());

        context.lookup("MyBeanLocalBean");
        assertEquals(2, MyBean.instances.get());


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
        public MyRuntimeException(final String message) {
            super(message);
        }
    }

    public static class MyBean {

        public static AtomicInteger instances = new AtomicInteger();

        public MyBean() {
        }

        @PostConstruct
        private void construct() {
            instances.incrementAndGet();
        }

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
        public String echo(final String string) {
            final StringBuilder sb = new StringBuilder(string);
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
                final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
                final int status = transactionManager.getStatus();
                return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
            } catch (final SystemException e) {
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