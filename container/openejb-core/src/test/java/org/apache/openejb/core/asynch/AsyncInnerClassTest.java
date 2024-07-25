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

package org.apache.openejb.core.asynch;

import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.naming.InitialContext;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AsyncInnerClassTest {

    private Assembler assembler;

    private ConfigurationFactory config;

    @Before
    public void beforeTest() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        config = new ConfigurationFactory();
        assembler = new Assembler();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
    }

    @After
    public void afterTest() throws Exception {
        assembler.destroy();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @Test
    public void test() throws Exception {
        final AppModule app = new AppModule(this.getClass().getClassLoader(), "test");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(RepXYZ.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        final AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        final InitialContext context = new InitialContext();
        final RepXYZ test = (RepXYZ) context.lookup("RepXYZLocalBean");
        assertNotNull(test);

        RepXYZ.RepTaskRequest r = new RepXYZ.RepTaskRequest();
        final Future<RepXYZ.RepTaskRequest> future = test.processAsynchronously(r);
        assertNotNull(future);
        assertEquals("test_success", future.get().test);

    }

    public static class RepXYZ {
        @Asynchronous
        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public Future<RepTaskRequest> processAsynchronously(RepTaskRequest r) {
            r.test = "test_success";
            return new AsyncResult<>(r);
        }

        public static class RepTaskRequest{
            public String test;
        }
    }
}
