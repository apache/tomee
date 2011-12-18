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

import java.lang.management.ManagementFactory;
import java.util.Scanner;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Note: each MBeans are at least deployed twice (using old and spec module name).
 *
 *         openejb.user.mbeans.list=org.apache.openejb.mbeans.Empty,
 *         org.apache.openejb.mbeans.Inheritance,
 *         org.apache.openejb.mbeans.MBeanDescription,
 *         org.apache.openejb.mbeans.MBeanNotificationlistener,
 *         org.apache.openejb.mbeans.MultipleNotifications,
 *         org.apache.openejb.mbeans.Notificater,
 *         org.apache.openejb.mbeans.Operation,
 *         org.apache.openejb.mbeans.OperationDescription,
 *         org.apache.openejb.mbeans.Reader,
 *         org.apache.openejb.mbeans.ReaderWriter,
 *         org.apache.openejb.mbeans.ReaderDescription
 */
public class MBeanDeployerTest {
    private static final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    private static final String LIST_PROPR = "openejb.user.mbeans.list";

    private static AppModule appModule;
    private static Assembler assembler;
    private static ConfigurationFactory config;

    /**
     * needs properties so it is not done in a @Before
     * because SystemInstance.get().setProperty(LIST_PROPR, ...) is done in @Test methods.
     */
    private void startOpenEJB() throws Exception {
        config = new ConfigurationFactory();
        assembler = new Assembler();
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        EjbModule ejbModule = new EjbModule(ejbJar);

        appModule = new AppModule(ejbModule.getClassLoader(), "mbeans");
        appModule.getEjbModules().add(ejbModule);
        assembler.createApplication(config.configureApplication(appModule));
    }

    @After public void resetList() {
        assembler.destroy();
        SystemInstance.reset();
    }

    @Test public void empty() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.Empty");
        startOpenEJB();

        assertEquals(2, appModule.getMBeans().size());

        ObjectName on = new ObjectName(appModule.getMBeans().iterator().next());
        String cn = on.getCanonicalName();
        assertTrue(cn.startsWith("openejb.user.mbeans"));
        assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
        assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
        assertTrue(cn.contains("name=Empty"));
        assertTrue(server.isRegistered(on));
    }

    @Test public void reader() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.Reader");
        startOpenEJB();

        assertEquals(2, appModule.getMBeans().size());
        String cn = appModule.getMBeans().iterator().next();
        ObjectName on = new ObjectName(cn);
        assertTrue(cn.startsWith("openejb.user.mbeans"));
        assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
        assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
        assertTrue(cn.contains("name=Reader"));
        assertTrue(server.isRegistered(on));
        assertEquals(2, server.getAttribute(on, "value"));
    }

    @Test public void writer() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.ReaderWriter");
        startOpenEJB();

        assertEquals(2, appModule.getMBeans().size());
        String cn = appModule.getMBeans().iterator().next();
        ObjectName on = new ObjectName(cn);        assertTrue(cn.startsWith("openejb.user.mbeans"));
        assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
        assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
        assertTrue(cn.contains("name=ReaderWriter"));
        assertTrue(server.isRegistered(on));
        assertEquals(2, server.getAttribute(on, "value"));
        server.setAttribute(on, new Attribute("value", 5));
        assertEquals(5, server.getAttribute(on, "value"));
    }

    @Test public void emptyAndReader() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.Empty,org.apache.openejb.mbeans.Reader");
        startOpenEJB();

        assertEquals(4, appModule.getMBeans().size());
        for (String cn : appModule.getMBeans()) {
            ObjectName on = new ObjectName(cn);
            assertTrue(cn.startsWith("openejb.user.mbeans"));
            assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
            assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
            assertTrue(cn.contains("name=Reader") || cn.contains("name=Empty"));
            assertTrue(server.isRegistered(on));
        }
    }

    @Test public void inheritance() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.Inheritance");
        startOpenEJB();

        assertEquals(2, appModule.getMBeans().size());

        String cn = appModule.getMBeans().iterator().next();
        ObjectName on = new ObjectName(cn);
        assertTrue(cn.startsWith("openejb.user.mbeans"));
        assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
        assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
        assertTrue(cn.contains("name=Inheritance"));
        assertTrue(server.isRegistered(on));
        assertEquals(2, server.getAttribute(on, "value"));
        server.setAttribute(on, new Attribute("value", 5));
        assertEquals(5, server.getAttribute(on, "value"));
        assertEquals("yes - no", server.invoke(on, "returnValue", null, null));
    }

    @Test public void description() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.MBeanDescription");
        startOpenEJB();

        assertEquals(2, appModule.getMBeans().size());

        String cn = appModule.getMBeans().iterator().next();
        ObjectName on = new ObjectName(cn);
        assertTrue(cn.startsWith("openejb.user.mbeans"));
        assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
        assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
        assertTrue(cn.contains("name=MBeanDescription"));
        assertTrue(server.isRegistered(on));
        assertEquals("descr ;)", server.getMBeanInfo(on).getDescription());
    }

    @Test public void operation() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.Operation");
        startOpenEJB();

        assertEquals(2, appModule.getMBeans().size());

        String cn = appModule.getMBeans().iterator().next();
        ObjectName on = new ObjectName(cn);
        assertTrue(cn.startsWith("openejb.user.mbeans"));
        assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
        assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
        assertTrue(cn.contains("name=Operation"));
        assertTrue(server.isRegistered(on));
        assertEquals("yes - no", server.invoke(on, "returnValue", null, null));
    }

    @Test public void readerDescription() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.ReaderDescription");
        startOpenEJB();

        assertEquals(2, appModule.getMBeans().size());

        String cn = appModule.getMBeans().iterator().next();
        ObjectName on = new ObjectName(cn);
        assertTrue(cn.startsWith("openejb.user.mbeans"));
        assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
        assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
        assertTrue(cn.contains("name=ReaderDescription"));
        assertTrue(server.isRegistered(on));
        assertEquals(1, server.getMBeanInfo(on).getAttributes().length);
        assertEquals("just a value", server.getMBeanInfo(on).getAttributes()[0].getDescription());
    }

    @Test public void operationDescription() throws Exception {
        SystemInstance.get().setProperty(LIST_PROPR, "org.apache.openejb.mbeans.OperationDescription");
        startOpenEJB();

        assertEquals(2, appModule.getMBeans().size());

        String cn = appModule.getMBeans().iterator().next();
        ObjectName on = new ObjectName(cn);
        assertTrue(cn.startsWith("openejb.user.mbeans"));
        assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
        assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
        assertTrue(cn.contains("name=OperationDescription"));
        assertTrue(server.isRegistered(on));
        assertEquals(1, server.getMBeanInfo(on).getOperations().length);
        assertEquals("just an op", server.getMBeanInfo(on).getOperations()[0].getDescription());
    }
}

