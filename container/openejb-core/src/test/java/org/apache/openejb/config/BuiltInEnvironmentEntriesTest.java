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
package org.apache.openejb.config;

import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.apache.openejb.util.SetAccessible;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Field;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class BuiltInEnvironmentEntriesTest {

    @EJB
    private Blue blue;

    @EJB
    private Red red;

    @Module
    public Class[] fantastic() {
        return new Class[]{Blue.class, Red.class};
    }

    @Test
    public void testBlue() throws Exception {
        blue.test();
    }

    @Test
    public void testRed() throws Exception {
        red.test();
    }

    @Singleton
    @TransactionManagement(value = TransactionManagementType.BEAN)
    public static class Blue {

        @Resource(name = "java:comp/EJBContext")
        private EJBContext ejbContext;

        @Resource(name = "java:comp/Validator")
        private Validator validator;

        @Resource(name = "java:comp/ValidatorFactory")
        private ValidatorFactory validatorFactory;

        @Resource(name = "java:comp/TransactionManager")
        private TransactionManager transactionManager;

        @Resource(name = "java:comp/TransactionSynchronizationRegistry")
        private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

        @Resource(name = "java:comp/UserTransaction")
        private UserTransaction userTransaction;

        @Resource(name = "java:comp/BeanManager")
        private BeanManager beanManager;

        @Resource(name = "java:app/AppName")
        private String app;

        @Resource(name = "java:module/ModuleName")
        private String module;

        @Resource(name = "java:comp/ComponentName")
        private String component;

        public void test() throws Exception {

            final Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                SetAccessible.on(field);
                Assert.assertNotNull(field.getName(), field.get(this));
            }

            Assert.assertEquals("app", "BuiltInEnvironmentEntriesTest", app);
            Assert.assertEquals("module", module, "fantastic");
            Assert.assertEquals("component", component, "Blue");
        }

    }

    @Singleton
    @TransactionManagement(value = TransactionManagementType.BEAN)
    public static class Red {

        @Resource
        private EJBContext ejbContext;

        @Resource
        private Validator validator;

        @Resource
        private ValidatorFactory validatorFactory;

        @Resource
        private TransactionManager transactionManager;

        @Resource
        private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

        @Resource
        private UserTransaction userTransaction;

        @Resource
        private BeanManager beanManager;

        public void test() throws Exception {

            final Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                SetAccessible.on(field);
                Assert.assertNotNull(field.getName(), field.get(this));
            }
        }

    }
}
