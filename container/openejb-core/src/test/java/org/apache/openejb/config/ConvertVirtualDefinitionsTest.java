/*
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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.ManagedExecutor;
import org.apache.openejb.jee.ManagedScheduledExecutor;
import org.apache.openejb.jee.ManagedThreadFactory;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.jba.JndiName;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Verifies that the {@code virtual} attribute flows correctly from
 * DD model classes through the Convert*Definitions deployers to Resource properties.
 */
public class ConvertVirtualDefinitionsTest {

    @Test
    public void threadFactoryVirtualTrue() throws OpenEJBException {
        final ManagedThreadFactory factory = new ManagedThreadFactory();
        factory.setName(jndi("java:comp/env/concurrent/VirtualTF"));
        factory.setContextService(jndi("java:comp/DefaultContextService"));
        factory.setPriority(5);
        factory.setVirtual(Boolean.TRUE);

        final AppModule appModule = createAppModuleWithThreadFactory(factory);
        new ConvertManagedThreadFactoryDefinitions().deploy(appModule);

        final List<Resource> resources = new ArrayList<>(appModule.getResources());
        assertEquals(1, resources.size());
        assertEquals("true", resources.get(0).getProperties().getProperty("Virtual"));
    }

    @Test
    public void threadFactoryVirtualNull() throws OpenEJBException {
        final ManagedThreadFactory factory = new ManagedThreadFactory();
        factory.setName(jndi("java:comp/env/concurrent/PlatformTF"));
        factory.setContextService(jndi("java:comp/DefaultContextService"));
        factory.setPriority(5);
        // virtual not set — should be null

        final AppModule appModule = createAppModuleWithThreadFactory(factory);
        new ConvertManagedThreadFactoryDefinitions().deploy(appModule);

        final List<Resource> resources = new ArrayList<>(appModule.getResources());
        assertEquals(1, resources.size());
        assertNull("Virtual should not be set when null",
                resources.get(0).getProperties().getProperty("Virtual"));
    }

    @Test
    public void executorVirtualTrue() throws OpenEJBException {
        final ManagedExecutor executor = new ManagedExecutor();
        executor.setName(jndi("java:comp/env/concurrent/VirtualMES"));
        executor.setContextService(jndi("java:comp/DefaultContextService"));
        executor.setVirtual(Boolean.TRUE);

        final AppModule appModule = createAppModuleWithExecutor(executor);
        new ConvertManagedExecutorServiceDefinitions().deploy(appModule);

        final List<Resource> resources = new ArrayList<>(appModule.getResources());
        assertEquals(1, resources.size());
        assertEquals("true", resources.get(0).getProperties().getProperty("Virtual"));
    }

    @Test
    public void scheduledExecutorVirtualTrue() throws OpenEJBException {
        final ManagedScheduledExecutor executor = new ManagedScheduledExecutor();
        executor.setName(jndi("java:comp/env/concurrent/VirtualMSES"));
        executor.setContextService(jndi("java:comp/DefaultContextService"));
        executor.setVirtual(Boolean.TRUE);

        final AppModule appModule = createAppModuleWithScheduledExecutor(executor);
        new ConvertManagedScheduledExecutorServiceDefinitions().deploy(appModule);

        final List<Resource> resources = new ArrayList<>(appModule.getResources());
        assertEquals(1, resources.size());
        assertEquals("true", resources.get(0).getProperties().getProperty("Virtual"));
    }

    @Test
    public void threadFactoryWithNullContextServiceDefaults() throws OpenEJBException {
        final ManagedThreadFactory factory = new ManagedThreadFactory();
        factory.setName(jndi("java:comp/env/concurrent/NoCtxTF"));
        // contextService intentionally NOT set — should default to DefaultContextService
        factory.setPriority(5);

        final AppModule appModule = createAppModuleWithThreadFactory(factory);
        new ConvertManagedThreadFactoryDefinitions().deploy(appModule);

        final List<Resource> resources = new ArrayList<>(appModule.getResources());
        assertEquals(1, resources.size());
        assertEquals("Default Context Service",
                resources.get(0).getProperties().getProperty("Context"));
    }

    @Test
    public void executorWithNullContextServiceDefaults() throws OpenEJBException {
        final ManagedExecutor executor = new ManagedExecutor();
        executor.setName(jndi("java:comp/env/concurrent/NoCtxMES"));
        // contextService intentionally NOT set

        final AppModule appModule = createAppModuleWithExecutor(executor);
        new ConvertManagedExecutorServiceDefinitions().deploy(appModule);

        final List<Resource> resources = new ArrayList<>(appModule.getResources());
        assertEquals(1, resources.size());
        assertEquals("Default Context Service",
                resources.get(0).getProperties().getProperty("Context"));
    }

    @Test
    public void scheduledExecutorWithNullContextServiceDefaults() throws OpenEJBException {
        final ManagedScheduledExecutor executor = new ManagedScheduledExecutor();
        executor.setName(jndi("java:comp/env/concurrent/NoCtxMSES"));
        // contextService intentionally NOT set

        final AppModule appModule = createAppModuleWithScheduledExecutor(executor);
        new ConvertManagedScheduledExecutorServiceDefinitions().deploy(appModule);

        final List<Resource> resources = new ArrayList<>(appModule.getResources());
        assertEquals(1, resources.size());
        assertEquals("Default Context Service",
                resources.get(0).getProperties().getProperty("Context"));
    }

    @Test
    public void threadFactoryQualifierIsPreserved() {
        final ManagedThreadFactory factory = new ManagedThreadFactory();
        factory.setName(jndi("java:comp/env/concurrent/QualifiedTF"));
        factory.setContextService(jndi("java:comp/DefaultContextService"));

        final List<String> qualifiers = new ArrayList<>();
        qualifiers.add("com.example.MyQualifier");
        qualifiers.add("com.example.AnotherQualifier");
        factory.setQualifier(qualifiers);

        assertEquals(2, factory.getQualifier().size());
        assertEquals("com.example.MyQualifier", factory.getQualifier().get(0));
        assertEquals("com.example.AnotherQualifier", factory.getQualifier().get(1));
    }

    @Test
    public void executorQualifierIsPreserved() {
        final ManagedExecutor executor = new ManagedExecutor();
        executor.setName(jndi("java:comp/env/concurrent/QualifiedMES"));
        executor.setContextService(jndi("java:comp/DefaultContextService"));

        final List<String> qualifiers = new ArrayList<>();
        qualifiers.add("com.example.ExecutorQualifier");
        executor.setQualifier(qualifiers);

        assertEquals(1, executor.getQualifier().size());
        assertEquals("com.example.ExecutorQualifier", executor.getQualifier().get(0));
    }

    // --- helpers ---

    private static JndiName jndi(final String value) {
        final JndiName name = new JndiName();
        name.setvalue(value);
        return name;
    }

    private static AppModule createAppModuleWithThreadFactory(final ManagedThreadFactory factory) {
        final WebApp webApp = new WebApp();
        webApp.getManagedThreadFactoryMap().put(factory.getKey(), factory);

        final AppModule appModule = new AppModule(ConvertVirtualDefinitionsTest.class.getClassLoader(), "test");
        final WebModule webModule = new WebModule(webApp, "test", ConvertVirtualDefinitionsTest.class.getClassLoader(), "target", "test");
        appModule.getWebModules().add(webModule);
        return appModule;
    }

    private static AppModule createAppModuleWithExecutor(final ManagedExecutor executor) {
        final WebApp webApp = new WebApp();
        webApp.getManagedExecutorMap().put(executor.getKey(), executor);

        final AppModule appModule = new AppModule(ConvertVirtualDefinitionsTest.class.getClassLoader(), "test");
        final WebModule webModule = new WebModule(webApp, "test", ConvertVirtualDefinitionsTest.class.getClassLoader(), "target", "test");
        appModule.getWebModules().add(webModule);
        return appModule;
    }

    private static AppModule createAppModuleWithScheduledExecutor(final ManagedScheduledExecutor executor) {
        final WebApp webApp = new WebApp();
        webApp.getManagedScheduledExecutorMap().put(executor.getKey(), executor);

        final AppModule appModule = new AppModule(ConvertVirtualDefinitionsTest.class.getClassLoader(), "test");
        final WebModule webModule = new WebModule(webApp, "test", ConvertVirtualDefinitionsTest.class.getClassLoader(), "target", "test");
        appModule.getWebModules().add(webModule);
        return appModule;
    }
}
