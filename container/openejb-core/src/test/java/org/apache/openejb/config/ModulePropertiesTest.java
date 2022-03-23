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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Archives;

import jakarta.ejb.Singleton;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ModulePropertiesTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        SystemInstance.reset();
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void testFile() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            final Map<String, String> map = new HashMap<>();
            map.put("META-INF/module.properties", "color=orange");

            final File app = Archives.fileArchive(map, WidgetBean.class);

            assembler.createApplication(config.configureApplication(app));
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        assertContexts(containerSystem);
    }

    public void testOverrideAdd() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            SystemInstance.get().getProperties().put("fooModule.color", "orange");

            final Map<String, String> map = new HashMap<>();
            map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"fooModule\"/>");
            final File module = Archives.fileArchive(map, WidgetBean.class);

            final AppModule appModule = config.loadApplication(this.getClass().getClassLoader(), "fooApp", Arrays.asList(module));
            final AppInfo appInfo = config.configureApplication(appModule);
            assembler.createApplication(appInfo);
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        assertContexts(containerSystem);
    }

    public void testOverrideReplace() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            SystemInstance.get().getProperties().put("fooModule.color", "orange");

            final Map<String, String> map = new HashMap<>();
            map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"fooModule\"/>");
            map.put("META-INF/module.properties", "color=white");

            final File module = Archives.fileArchive(map, WidgetBean.class);

            final AppModule appModule = config.loadApplication(this.getClass().getClassLoader(), "fooApp", Arrays.asList(module));
            final AppInfo appInfo = config.configureApplication(appModule);
            assembler.createApplication(appInfo);
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        assertContexts(containerSystem);
    }

    public void testOverrideFromApplicationProperties() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            final Map<String, String> map = new HashMap<>();
            map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"fooModule\"/>");
            map.put("META-INF/module.properties", "color=white");

            final File module = Archives.fileArchive(map, WidgetBean.class);

            final AppModule appModule = config.loadApplication(this.getClass().getClassLoader(), "fooApp", Arrays.asList(module));
            appModule.getProperties().put("fooModule.color", "orange");

            final AppInfo appInfo = config.configureApplication(appModule);
            assembler.createApplication(appInfo);
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        assertContexts(containerSystem);
    }

    public void testOverrideFromFullDottedPath() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            SystemInstance.get().getProperties().put("fooApp.fooModule.color", "orange");

            final Map<String, String> map = new HashMap<>();
            map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"fooModule\"/>");
            map.put("META-INF/module.properties", "color=white");

            final File module = Archives.fileArchive(map, WidgetBean.class);

            final AppModule appModule = config.loadApplication(this.getClass().getClassLoader(), "fooApp", Arrays.asList(module));

            final AppInfo appInfo = config.configureApplication(appModule);
            assembler.createApplication(appInfo);
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        assertContexts(containerSystem);
    }

    private void assertContexts(final ContainerSystem containerSystem) {
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");
        final ModuleContext moduleContext = beanContext.getModuleContext();
        final AppContext appContext = moduleContext.getAppContext();

        { // Assert as Properties

            // ModuleContext should have color property
            assertProperty(moduleContext.getProperties(), "color", "orange");

            // BeanContext and AppContext should not
            assertNoProperty(beanContext.getProperties(), "color");
            assertNoProperty(appContext.getProperties(), "color");

            // Try all the above again with mixed case
            assertProperty(moduleContext.getProperties(), "coLOr", "orange");
            assertNoProperty(beanContext.getProperties(), "coLOr");
            assertNoProperty(appContext.getProperties(), "coLOr");
        }

        { // Assert as Options

            // ModuleContext should have color option
            assertOption(moduleContext.getOptions(), "color", "orange");

            // BeanContext should inherit ModuleContext color
            assertOption(beanContext.getOptions(), "color", "orange");

            // AppContext should remain unpolluted
            assertNoOption(appContext.getOptions(), "color");

            // Try all the above again using mixed case
            assertOption(moduleContext.getOptions(), "coLoR", "orange");
            assertOption(beanContext.getOptions(), "coLoR", "orange");
            assertNoOption(appContext.getOptions(), "coLoR");
        }
    }

    private void assertOption(final Options options, final String key, final String value) {
        assertEquals(value, options.get(key, key + " (not set)"));
    }

    private void assertNoOption(final Options options, final String key) {
        final String defaultValue = key + " (not set)";
        assertEquals(defaultValue, options.get(key, defaultValue));
    }

    private void assertProperty(final Properties properties, final String key, final String value) {
        assertTrue(properties.containsKey(key));
        assertEquals(value, properties.getProperty(key));
    }

    private void assertNoProperty(final Properties properties, final String key) {
        assertFalse(properties.containsKey(key));
    }

    @Singleton
    public static class WidgetBean {

    }
}
