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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.resource.PropertiesResourceProvider;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.SystemInstance;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * $Rev$ $Date$
 */
public class ConfigurationFactoryTest {

    @Test
    public void testConfigureApplicationEjbJar() throws OpenEJBException {
        // Just to find out whether the validationDisabled message shows up
        SystemInstance.get().setProperty(ConfigurationFactory.VALIDATION_SKIP_PROPERTY, "true");
        final boolean offline = true;
        final ConfigurationFactory factory = new ConfigurationFactory(offline);
        final String id = "testConfigureApplicationEjbJar";
        final EjbJar ejbJar = new EjbJar(id);
        // no real classes engaged so disable metadata (annotation) processing
        ejbJar.setMetadataComplete(true);
        final EjbJarInfo info = factory.configureApplication(ejbJar);
        // not much to assert
        assertEquals(id, info.moduleName);
    }

    @Test
    public void testConfigureApplicationWebModule() throws OpenEJBException {
        SystemInstance.get().setProperty("openejb.environment.default", "false");
        final String moduleId = "testConfigureApplicationWebModule";
        final String fileSeparator = System.getProperty("file.separator");

        SystemInstance.get().setProperty(ConfigurationFactory.VALIDATION_SKIP_PROPERTY, "false");
        SystemInstance.get().setProperty(DeploymentsResolver.SEARCH_CLASSPATH_FOR_DEPLOYMENTS_PROPERTY, "false");
        final ConfigurationFactory factory = new ConfigurationFactory();
        final WebApp webApp = new WebApp();
        // no real classes engaged so disable metadata (annotation) processing
        webApp.setMetadataComplete(true);
        final WebModule webModule = new WebModule(webApp, null, null, fileSeparator + "some" + fileSeparator + "where.war", moduleId);
        final WebAppInfo info = factory.configureApplication(webModule);
        assertEquals(moduleId, info.moduleId);
        SystemInstance.get().getProperties().remove("openejb.environment.default");
    }

    @Test
    public void testGetOpenEjbConfiguration() throws OpenEJBException {
        SystemInstance.get().setProperty(ConfigurationFactory.VALIDATION_SKIP_PROPERTY, "false");
        SystemInstance.get().setProperty(DeploymentsResolver.SEARCH_CLASSPATH_FOR_DEPLOYMENTS_PROPERTY, "false");
        final boolean offline = false;
        final ConfigurationFactory factory = new ConfigurationFactory(offline);
        final OpenEjbConfiguration openEjbConfig = factory.getOpenEjbConfiguration();
        // again, not much to assert
        assertEquals(0, openEjbConfig.containerSystem.applications.size());
    }

    @Test
    public void testGetOpenEJBConfigurationInitedAndNewResource() throws Exception {
        SystemInstance.get().setProperty(ConfigurationFactory.VALIDATION_SKIP_PROPERTY, "true");
        SystemInstance.get().setProperty(DeploymentsResolver.SEARCH_CLASSPATH_FOR_DEPLOYMENTS_PROPERTY, "false");
        SystemInstance.get().setProperty("newDeployment", "new://Deployments?dir=irrelevant");
        final ConfigurationFactory factory = new ConfigurationFactory();
        final Properties props = new Properties();
        final URL configUrl = this.getClass().getClassLoader().getResource(
            "org/apache/openejb/config/configurationfactory-openejb.xml");
        props.setProperty(ConfigurationFactory.CONF_FILE_PROPERTY, configUrl.toExternalForm());
        factory.init(props);
        final OpenEjbConfiguration openEjbConfig = factory.getOpenEjbConfiguration();
        assertEquals(0, openEjbConfig.containerSystem.applications.size());
    }

    @Test
    public void testConfigurationFactoryBooleanOpenEjbConfiguration() throws OpenEJBException {
        final boolean offline = false;
        final OpenEjbConfiguration openEjbConfiguration = new OpenEjbConfiguration();
        final ConfigurationFactory factory = new ConfigurationFactory(offline, openEjbConfiguration);
        assertEquals(openEjbConfiguration, factory.getOpenEjbConfiguration());
    }

    @Test
    public void testConfigurationFactoryBooleanDynamicDeployerOpenEjbConfiguration() throws OpenEJBException {
        final boolean offline = false;
        final DynamicDeployer dynamicDeployer = null;
        final OpenEjbConfiguration openEjbConfiguration = new OpenEjbConfiguration();
        final ConfigurationFactory factory = new ConfigurationFactory(offline, dynamicDeployer, openEjbConfiguration);
        assertEquals(openEjbConfiguration, factory.getOpenEjbConfiguration());
    }

    @Test
    public void testToConfigDeclaration() throws Exception {
        final String path = ".";
        final ConfigurationFactory factory = new ConfigurationFactory();
        final Deployments deployments = (Deployments) factory.toConfigDeclaration("", new URI("new://Deployments?classpath="
            + path));
        final URLClassLoader cl = (URLClassLoader) deployments.getClasspath();
        final URL[] urls = cl.getURLs();
        assertEquals(urls[0], new File(path).toURI().normalize().toURL());
    }

    @Test
    public void testUsePropertiesProviderWhenCreatingAContainer() throws Exception {
        final ConfigurationFactory configurationFactory = new ConfigurationFactory();
        final Container container = new Container();
        container.setPropertiesProvider(MyPropertiesProvider.class.getName());
        container.setCtype("STATELESS");

        final ContainerInfo containerInfo = configurationFactory.createContainerInfo(container);
        assertEquals("newproperty", containerInfo.properties.getProperty("test"));
    }

    public static class MyPropertiesProvider implements PropertiesResourceProvider {

        private Properties properties;

        @Override
        public Properties provides() {
            final Properties p = new Properties();
            p.putAll(properties);
            p.setProperty("test", "newproperty");

            return p;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }
    }
}
