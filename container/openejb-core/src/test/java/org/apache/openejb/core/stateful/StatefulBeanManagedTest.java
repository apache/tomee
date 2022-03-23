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
package org.apache.openejb.core.stateful;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.annotation.Resource;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * This test case serves as a nice tiny template for other test cases
 * and purposely doesn't do anything very complicated.
 *
 * @version $Rev$ $Date$
 */
public class StatefulBeanManagedTest {

    @BeforeClass
    public static void beforeClass() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(MyBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @Test
    public void testExpected() throws Exception {
        MyBean beanOne = (MyBean) new InitialContext().lookup("MyBeanLocalBean");

        beanOne.one("bob");
        assertEquals("bob", beanOne.two());
        beanOne.three();

        beanOne = (MyBean) new InitialContext().lookup("MyBeanLocalBean");
        beanOne.one("sally");
        assertEquals("sally", beanOne.two());
        beanOne.three();

        final MyBean beanTwo = (MyBean) new InitialContext().lookup("MyBeanLocalBean");

        assertNull(beanTwo.two());
    }

    @Test(expected = jakarta.transaction.NotSupportedException.class)
    public void testUnexpected() throws Exception {

        final MyBean beanOne = (MyBean) new InitialContext().lookup("MyBeanLocalBean");

        final String bob = "bob";

        beanOne.one("bob");
        assertEquals("bob", beanOne.two());

        //This should throw an exception because of the existing transaction
        beanOne.one("sally");
    }

    @Stateful
    @LocalBean
    @TransactionManagement(TransactionManagementType.BEAN)
    public static class MyBean {

        String string;

        @Resource
        private UserTransaction transaction;

        public void one(final String string) throws SystemException, NotSupportedException {
            this.transaction.begin();
            this.string = string;
        }

        public String two() throws SystemException, NotSupportedException {
            return this.string;
        }

        @Remove
        public void three() throws HeuristicRollbackException, HeuristicMixedException, RollbackException, SystemException {
            this.string = null;
            this.transaction.commit();
        }
    }
}
