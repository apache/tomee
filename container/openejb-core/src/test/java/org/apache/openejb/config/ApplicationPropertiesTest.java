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
public class ApplicationPropertiesTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        SystemInstance.reset();
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            final Map<String, String> map = new HashMap<>();
            map.put("META-INF/application.properties", "color=orange");

            final File app = Archives.fileArchive(map, WidgetBean.class);

            assembler.createApplication(config.configureApplication(app));
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        assertContexts(containerSystem);
    }

    /**
     * A child module META-INF/application.properties sets color to white
     *
     * In the root ear META-INF/application.properties color is set to orange
     *
     * The root ear META-INF/application.properties wins
     *
     * @throws Exception
     */
    public void testConflictingFiles() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            final Map<String, String> moduleFiles = new HashMap<>();
            moduleFiles.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"fooModule\"/>");
            moduleFiles.put("META-INF/application.properties", "color=white");

            final File module = Archives.jarArchive(moduleFiles, "fooModule", WidgetBean.class);

            final Map<String, String> appFiles = new HashMap<>();
            appFiles.put("META-INF/application.xml", "" +
                "<application id=\"fooApp\">\n" +
                "  <module>\n" +
                "    <ejb>" + module.getName() + "</ejb>\n" +
                "  </module>\n" +
                "</application>");

            appFiles.put("META-INF/application.properties", "color=orange");
            final File app = Archives.fileArchive(appFiles);

            assertTrue(module.renameTo(new File(app, module.getName())));

            final AppInfo appInfo = config.configureApplication(app);
            assembler.createApplication(appInfo);
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
            SystemInstance.get().getProperties().put("fooApp.color", "orange");

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
            SystemInstance.get().getProperties().put("fooApp.color", "orange");

            final Map<String, String> map = new HashMap<>();
            map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"fooModule\"/>");
            map.put("META-INF/application.properties", "color=white");

            final File module = Archives.fileArchive(map, WidgetBean.class);

            final AppModule appModule = config.loadApplication(this.getClass().getClassLoader(), "fooApp", Arrays.asList(module));
            final AppInfo appInfo = config.configureApplication(appModule);
            assembler.createApplication(appInfo);
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        assertContexts(containerSystem);
    }

    public void testOverrideUnprefixedVsPrefixedOpenEJB() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            SystemInstance.get().getProperties().put("openejb.fooApp.color", "orange");
            SystemInstance.get().getProperties().put("fooApp.color", "green");

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

    public void testOverrideUnprefixedVsPrefixedTomEE() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            SystemInstance.get().getProperties().put("tomee.fooApp.color", "orange");
            SystemInstance.get().getProperties().put("fooApp.color", "green");

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

    /**
     * Not implemented.  Don't do it if you want deterministic behavior
     *
     * Use one or the other, use both and no guarantee is made
     *
     * @throws Exception
     */
    public void _testOverrideUnprefixedVsPrefixedTomEEAndOpenEJB() throws Exception {

        SystemInstance.get().getProperties().put("tomee.fooApp.color", "green");
        SystemInstance.get().getProperties().put("openejb.fooApp.color", "blue");
    }

    private void assertContexts(final ContainerSystem containerSystem) {
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");
        final ModuleContext moduleContext = beanContext.getModuleContext();
        final AppContext appContext = moduleContext.getAppContext();

        { // Assert as Properties

            // AppContext should have color property
            assertProperty(appContext.getProperties(), "color", "orange");

            // BeanContext and ModuleContext should not
            assertNoProperty(beanContext.getProperties(), "color");
            assertNoProperty(moduleContext.getProperties(), "color");

            // Try all the above again with mixed case
            assertProperty(appContext.getProperties(), "coLOr", "orange");
            assertNoProperty(beanContext.getProperties(), "coLOr");
            assertNoProperty(moduleContext.getProperties(), "coLOr");
        }

        { // Assert as Options

            // AppContext should have color option
            assertOption(appContext.getOptions(), "color", "orange");

            // BeanContext and ModuleContext should inherit AppContext color
            assertOption(beanContext.getOptions(), "color", "orange");
            assertOption(moduleContext.getOptions(), "color", "orange");

            // Try all the above again using mixed case
            assertOption(appContext.getOptions(), "coLoR", "orange");
            assertOption(moduleContext.getOptions(), "coLoR", "orange");
            assertOption(beanContext.getOptions(), "coLoR", "orange");
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
