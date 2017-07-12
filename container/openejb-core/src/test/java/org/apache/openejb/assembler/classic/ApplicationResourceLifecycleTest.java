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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ApplicationResourceLifecycleTest {

    private static final AtomicBoolean POST_CONSTRUCT = new AtomicBoolean(false);
    private static final AtomicBoolean PRE_DESTROY = new AtomicBoolean(false);


    @Test
    public void test() throws OpenEJBException, NamingException, IOException {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final AppModule app = new AppModule(ApplicationResourceLifecycleTest.class.getClassLoader(), ApplicationResourceLifecycleTest.class.getSimpleName());

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(ApplicationResource.class));
        app.getEjbModules().add(new EjbModule(ejbJar));
        app.getEjbModules().iterator().next().getAltDDs().put("resources.xml", getClass().getClassLoader().getResource("app-resources.xml"));

        assembler.createApplication(config.configureApplication(app));

        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        properties.setProperty("openejb.embedded.initialcontext.close", "destroy");

        // some hack to be sure to call destroy()
        InitialContext context = new InitialContext(properties);
        assertNotNull(context);

        assertTrue(POST_CONSTRUCT.getAndSet(false));
        assertFalse(PRE_DESTROY.get());

        ApplicationResource bean = (ApplicationResource) context.lookup("ApplicationResourceLocalBean");

        assertNotNull(bean);
        context.close();

        OpenEJB.destroy();
        assertFalse(POST_CONSTRUCT.get());
        assertTrue(PRE_DESTROY.get());
    }

    public static class ApplicationResource {

        @PostConstruct
        public void start() {
            POST_CONSTRUCT.set(true);
        }

        @PreDestroy
        public void stop() {
            PRE_DESTROY.set(true);
        }

    }
}
