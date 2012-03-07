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

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.mbeans.Empty;
import org.apache.openejb.mbeans.Inheritance;
import org.apache.openejb.mbeans.MBeanDescription;
import org.apache.openejb.mbeans.Operation;
import org.apache.openejb.mbeans.OperationDescription;
import org.apache.openejb.mbeans.Reader;
import org.apache.openejb.mbeans.ReaderDescription;
import org.apache.openejb.mbeans.ReaderWriter;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class MBeanDeployerTest {
    private static final MBeanServer server = LocalMBeanServer.get();

    private AppModule appModule;
    private Assembler assembler;
    private ConfigurationFactory config;
    private AppInfo appInfo;

    @Before
    public void startOpenEJB() throws Exception {
        config = new ConfigurationFactory();
        assembler = new Assembler();
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        final EjbModule ejbModule = new EjbModule(ejbJar);
        ejbModule.setBeans(new Beans());
        ejbModule.setModuleId("mbeans-test");

        for (Class<?> clazz : Arrays.asList(Operation.class, OperationDescription.class,
                Reader.class, ReaderWriter.class, ReaderDescription.class,
                Empty.class, Inheritance.class, MBeanDescription.class)) {
            ejbModule.getBeans().addManagedClass(clazz);
            ejbModule.getMbeans().add(clazz.getName());
        }

        appModule = new AppModule(ejbModule.getClassLoader(), "mbeans");
        appModule.getEjbModules().add(ejbModule);
        appInfo = config.configureApplication(appModule);
        assembler.createApplication(appInfo);
    }

    @After
    public void resetList() {
        assembler.destroy();
        SystemInstance.reset();
    }

    @Test
    public void mbeans() throws Exception {
        final Set<String> parsed = new HashSet<String>();
        for (String name : appInfo.jmx.values()) {
            final ObjectName on = new ObjectName(name);
            final String cn = on.getCanonicalName();
            if (cn.contains("name=Empty")) {
                assertTrue(cn.startsWith("openejb.user.mbeans"));
                assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
                assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
                assertTrue(server.isRegistered(on));
                parsed.add(cn);
            } else if (cn.contains("name=Reader")) {
                assertTrue(cn.startsWith("openejb.user.mbeans"));
                assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
                assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
                assertTrue(server.isRegistered(on));
                assertEquals(2, server.getAttribute(on, "value"));
                parsed.add(cn);
            } else if (cn.contains("name=ReaderWriter")) {
                assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
                assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
                assertTrue(server.isRegistered(on));
                assertEquals(2, server.getAttribute(on, "value"));
                server.setAttribute(on, new Attribute("value", 5));
                assertEquals(5, server.getAttribute(on, "value"));
                parsed.add(cn);
            } else if (cn.contains("name=Inheritance")) {
                assertTrue(cn.startsWith("openejb.user.mbeans"));
                assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
                assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
                assertTrue(server.isRegistered(on));
                assertEquals(2, server.getAttribute(on, "value"));
                server.setAttribute(on, new Attribute("value", 5));
                assertEquals(5, server.getAttribute(on, "value"));
                assertEquals("yes - no", server.invoke(on, "returnValue", null, null));
                parsed.add(cn);
            } else if (cn.contains("name=MBeanDescription")) {
                assertTrue(cn.startsWith("openejb.user.mbeans"));
                assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
                assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
                assertTrue(server.isRegistered(on));
                assertEquals("descr ;)", server.getMBeanInfo(on).getDescription());
                parsed.add(cn);
            } else if (cn.contains("name=Operation")) {
                assertTrue(cn.startsWith("openejb.user.mbeans"));
                assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
                assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
                assertTrue(server.isRegistered(on));
                assertEquals("yes - no", server.invoke(on, "returnValue", null, null));
                parsed.add(cn);
            } else if (cn.contains("name=ReaderDescription")) {
                assertTrue(cn.startsWith("openejb.user.mbeans"));
                assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
                assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
                assertTrue(cn.contains("name=ReaderDescription"));
                assertTrue(server.isRegistered(on));
                assertEquals(1, server.getMBeanInfo(on).getAttributes().length);
                assertEquals("just a value", server.getMBeanInfo(on).getAttributes()[0].getDescription());
                parsed.add(cn);
            } else if (cn.contains("name=OperationDescription")) {
                assertTrue(cn.startsWith("openejb.user.mbeans"));
                assertTrue(cn.contains("group=org.apache.openejb.mbeans"));
                assertTrue(cn.contains("application=mbeans") || cn.contains("application=EjbModule"));
                assertTrue(server.isRegistered(on));
                assertEquals(1, server.getMBeanInfo(on).getOperations().length);
                assertEquals("just an op", server.getMBeanInfo(on).getOperations()[0].getDescription());
                parsed.add(cn);
            }
        }
        assertEquals(8, parsed.size());
    }
}

