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

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.SystemInstance;
import org.junit.Test;

/**
 * $Rev$ $Date$
 */
public class ConfigurationFactoryTest {

    @Test
    public void testConfigureApplicationEjbJar() throws OpenEJBException {
        // Just to find out whether the validationDisabled message shows up
        SystemInstance.get().setProperty(ConfigurationFactory.VALIDATION_SKIP_PROPERTY, "true");
        final boolean offline = true;
        ConfigurationFactory factory = new ConfigurationFactory(offline);
        final String id = "testConfigureApplicationEjbJar";
        EjbJar ejbJar = new EjbJar(id);
        // no real classes engaged so disable metadata (annotation) processing
        ejbJar.setMetadataComplete(true);
        EjbJarInfo info = factory.configureApplication(ejbJar);
        // not much to assert
        assertEquals(id, info.moduleName);
    }

    @Test
    public void testConfigureApplicationWebModule() throws OpenEJBException {
    	final String moduleId = "testConfigureApplicationWebModule";
    	final String fileSeparator = System.getProperty("file.separator");
    	
        SystemInstance.get().setProperty(ConfigurationFactory.VALIDATION_SKIP_PROPERTY, "false");
        SystemInstance.get().setProperty(DeploymentsResolver.SEARCH_CLASSPATH_FOR_DEPLOYMENTS_PROPERTY, "false");
        ConfigurationFactory factory = new ConfigurationFactory();        
        WebApp webApp = new WebApp();
        // no real classes engaged so disable metadata (annotation) processing
        webApp.setMetadataComplete(true);
        WebModule webModule = new WebModule(webApp, null, null, fileSeparator + "some" + fileSeparator+ "where.war", moduleId);
        WebAppInfo info = factory.configureApplication(webModule);
        assertEquals(moduleId, info.moduleId);
    }

    @Test
    public void testGetOpenEjbConfiguration() throws OpenEJBException {
        SystemInstance.get().setProperty(ConfigurationFactory.VALIDATION_SKIP_PROPERTY, "false");
        SystemInstance.get().setProperty(DeploymentsResolver.SEARCH_CLASSPATH_FOR_DEPLOYMENTS_PROPERTY, "false");
        final boolean offline = false;
        ConfigurationFactory factory = new ConfigurationFactory(offline);
        OpenEjbConfiguration openEjbConfig = factory.getOpenEjbConfiguration();
        // again, not much to assert
        assertEquals(0, openEjbConfig.containerSystem.applications.size());
    }

    @Test
    public void testGetOpenEJBConfigurationInitedAndNewResource() throws Exception {
        SystemInstance.get().setProperty(ConfigurationFactory.VALIDATION_SKIP_PROPERTY, "true");
        SystemInstance.get().setProperty(DeploymentsResolver.SEARCH_CLASSPATH_FOR_DEPLOYMENTS_PROPERTY, "false");
        SystemInstance.get().setProperty("newDeployment", "new://Deployments?dir=irrelevant");
        ConfigurationFactory factory = new ConfigurationFactory();
        Properties props = new Properties();
        URL configUrl = this.getClass().getClassLoader().getResource(
                "org/apache/openejb/config/configurationfactory-openejb.xml");
        props.setProperty(ConfigurationFactory.CONF_FILE_PROPERTY, configUrl.toExternalForm());
        factory.init(props);
        OpenEjbConfiguration openEjbConfig = factory.getOpenEjbConfiguration();
        assertEquals(0, openEjbConfig.containerSystem.applications.size());
    }

    @Test
    public void testConfigurationFactoryBooleanOpenEjbConfiguration() throws OpenEJBException {
        final boolean offline = false;
        final OpenEjbConfiguration openEjbConfiguration = new OpenEjbConfiguration();
        ConfigurationFactory factory = new ConfigurationFactory(offline, openEjbConfiguration);
        assertEquals(openEjbConfiguration, factory.getOpenEjbConfiguration());
    }

    @Test
    public void testConfigurationFactoryBooleanDynamicDeployerOpenEjbConfiguration() throws OpenEJBException {
        final boolean offline = false;
        final DynamicDeployer dynamicDeployer = null;
        final OpenEjbConfiguration openEjbConfiguration = new OpenEjbConfiguration();
        ConfigurationFactory factory = new ConfigurationFactory(offline, dynamicDeployer, openEjbConfiguration);
        assertEquals(openEjbConfiguration, factory.getOpenEjbConfiguration());
    }

    @Test
    public void testToConfigDeclaration() throws Exception {
        final String path = ".";
        ConfigurationFactory factory = new ConfigurationFactory();
        Deployments deployments = (Deployments) factory.toConfigDeclaration("", new URI("new://Deployments?classpath="
                + path));
        URLClassLoader cl = (URLClassLoader) deployments.getClasspath();
        URL[] urls = cl.getURLs();
        assertEquals(urls[0], new File(path).toURI().normalize().toURL());
    }
}
