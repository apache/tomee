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

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.util.Archives;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * If an app exists in the apps/ directory in both the
 * packed and unpacked states, only deploy the app once
 *
 * Do not treat this as two applications and deploy each one separately.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentsElementTest extends Assert {

    @Rule
    public final ExpectedException exceptions = ExpectedException.none();

    /**
     * <Deployments dir="myapps"/>
     *
     * EAR file not extracted
     * Application.xml
     *
     * @throws Exception
     */
    @Test
    public void deploymentsDir_Ear_Packed_ApplicationXml() throws Exception {
        final Server server = new Server();
        final File apps = server.deploymentsDir("myapps");
        
        final String appName = "deploymentsDir_Ear_Packed_ApplicationXml";

        final File ear = new File(apps, appName + ".ear");
        {
            final Map<String, Object> contents = new HashMap<>();
            contents.put("foo.jar", Archives.jarArchive(Orange.class));
            contents.put("META-INF/application.xml", "<application><module><ejb>foo.jar</ejb></module></application>");
            Archives.jarArchive(ear, contents);
        }

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(1, configuration.containerSystem.applications.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps"/>
     *
     * EAR file not extracted
     *
     * @throws Exception
     */
    @Test
    public void deploymentsDir_Ear_Packed_NoApplicationXml() throws Exception {
        final Server server = new Server();
        final File apps = server.deploymentsDir("myapps");

        final String appName = "deploymentsDir_Ear_Packed_NoApplicationXml";

        final File ear = new File(apps, appName + ".ear");
        {
            final Map<String, Object> contents = new HashMap<>();
            contents.put("foo.jar", Archives.jarArchive(Orange.class));
            Archives.jarArchive(ear, contents);
        }

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(1, configuration.containerSystem.applications.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps"/>
     *
     * EAR file extracted
     * Application.xml
     *
     * @throws Exception
     */
    @Test
    public void deploymentsDir_Ear_Unpacked_ApplicationXml() throws Exception {
        final Server server = new Server();
        final File apps = server.deploymentsDir("myapps");

        final String appName = "deploymentsDir_Ear_Unpacked_ApplicationXml";

        final File ear = new File(server.getBase(), appName + ".ear");
        {
            final Map<String, Object> contents = new HashMap<>();
            contents.put("foo.jar", Archives.jarArchive(Orange.class));
            contents.put("META-INF/application.xml", "<application><module><ejb>foo.jar</ejb></module></application>");
            Archives.jarArchive(ear, contents);
        }

        Zips.unzip(ear, Files.mkdir(apps, appName));

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(1, configuration.containerSystem.applications.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps"/>
     *
     * EAR file extracted
     *
     * @throws Exception
     */
    @Test
    public void deploymentsDir_Ear_Unpacked_NoApplicationXml() throws Exception {
        final Server server = new Server();
        final File apps = server.deploymentsDir("myapps");

        final String appName = "deploymentsDir_Ear_Unpacked_NoApplicationXml";

        final File ear = new File(server.getBase(), appName + ".ear");
        {
            final Map<String, Object> contents = new HashMap<>();
            contents.put("foo.jar", Archives.jarArchive(Orange.class));
            Archives.jarArchive(ear, contents);
        }

        Zips.unzip(ear, Files.mkdir(apps, appName));

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(1, configuration.containerSystem.applications.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps"/>
     *
     * EAR file packed
     * EAR file extracted
     * Application.xml
     *
     * @throws Exception
     */
    @Test
    public void deploymentsDir_Ear_Packed_Unpacked_ApplicationXml() throws Exception {
        final Server server = new Server();
        final File apps = server.deploymentsDir("myapps");

        final String appName = "deploymentsDir_Ear_Packed_Unpacked_ApplicationXml";

        final File ear = new File(server.getBase(), appName + ".ear");
        {
            final Map<String, Object> contents = new HashMap<>();
            contents.put("foo.jar", Archives.jarArchive(Orange.class));
            contents.put("META-INF/application.xml", "<application><module><ejb>foo.jar</ejb></module></application>");
            Archives.jarArchive(ear, contents);
        }

        IO.copy(ear, Files.path(apps, appName + ".ear"));  // packed
        Zips.unzip(ear, Files.mkdir(apps, appName));  // unpacked

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(1, configuration.containerSystem.applications.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps"/>
     *
     * EAR file packed
     * EAR file extracted
     *
     * @throws Exception
     */
    @Test
    public void deploymentsDir_Ear_Packed_Unpacked_NoApplicationXml() throws Exception {
        final Server server = new Server();
        final File apps = server.deploymentsDir("myapps");

        final String appName = "deploymentsDir_Ear_Packed_Unpacked_NoApplicationXml";

        final File ear = new File(server.getBase(), appName + ".ear");
        {
            final Map<String, Object> contents = new HashMap<>();
            contents.put("foo.jar", Archives.jarArchive(Orange.class));
            Archives.jarArchive(ear, contents);
        }

        IO.copy(ear, Files.path(apps, appName + ".ear"));  // packed
        Zips.unzip(ear, Files.mkdir(apps, appName));  // unpacked

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(1, configuration.containerSystem.applications.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps"/>
     *
     * EAR file packed
     * EAR file extracted
     *
     * The packed version is newer and an updated version of the packed version
     * We should remove the extracted version and unpack the newer ear
     *
     * @throws Exception
     */
    @Test
    public void deploymentsDir_Ear_Packed_Unpacked_Modified() throws Exception {
        final Server server = new Server();
        final File apps = server.deploymentsDir("myapps");

        final String appName = "deploymentsDir_Ear_Packed_Unpacked_Modified";

        { // Unpacked version -- Orange

            final File ear = new File(server.getBase(), appName + ".ear");
            final Map<String, Object> contents = new HashMap<>();
            contents.put("foo.jar", Archives.jarArchive(Orange.class));
            contents.put("META-INF/application.xml", "<application><module><ejb>foo.jar</ejb></module></application>");
            Archives.jarArchive(ear, contents);
            Zips.unzip(ear, Files.mkdir(apps, appName));  // unpacked
        }

        Thread.sleep(100);

        { // Packed version -- Yellow

            final File ear = new File(server.getBase(), appName + ".ear");
            final Map<String, Object> contents = new HashMap<>();
            contents.put("foo.jar", Archives.jarArchive(Yellow.class));
            contents.put("META-INF/application.xml", "<application><module><ejb>foo.jar</ejb></module></application>");
            Archives.jarArchive(ear, contents);
            Zips.unzip(ear, Files.mkdir(apps, appName));  // unpacked
        }

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(1, configuration.containerSystem.applications.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.size());
        assertEquals(1, configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Yellow", configuration.containerSystem.applications.get(0).ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps"/>
     *
     * Two ejb jars
     *
     * @throws Exception
     */
    @Test
    public void deploymentsDir_Jars_Packed() throws Exception {
        final Server server = new Server();
        final File apps = server.deploymentsDir("myapps");

        { // Jar one
            final File ear = new File(apps, "yellow.jar");
            Archives.jarArchive(ear, null, Yellow.class);
        }

        { // Jar two
            final File ear = new File(apps, "orange.jar");
            Archives.jarArchive(ear, null, Orange.class);
        }

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(2, configuration.containerSystem.applications.size());

        final AppInfo yellow = select(configuration.containerSystem.applications, "yellow");
        assertEquals(1, yellow.ejbJars.size());
        assertEquals(1, yellow.ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Yellow", yellow.ejbJars.get(0).enterpriseBeans.get(0).ejbName);

        final AppInfo orange = select(configuration.containerSystem.applications, "orange");
        assertEquals(1, orange.ejbJars.size());
        assertEquals(1, orange.ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", orange.ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps"/>
     *
     * Two ejb jars
     *
     * Order should be guaranteed to be the same as
     * they are declared in the openejb.xml
     *
     * To test, the jars are named intentionally in an order
     * that would naturally sort to be the reverse.
     *
     * @throws Exception
     */
    @Test
    public void deploymentsFile_Jars_Order() throws Exception {
        final Server server = new Server();

        { // Jar one
            final File jar = server.deploymentsFile("2000.jar");
            Archives.jarArchive(jar, null, Yellow.class);
        }

        { // Jar two
            final File jar = server.deploymentsFile("1000.jar");
            Archives.jarArchive(jar, null, Orange.class);
        }

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(2, configuration.containerSystem.applications.size());
        assertEquals("2000", configuration.containerSystem.applications.get(0).appId);
        assertEquals("1000", configuration.containerSystem.applications.get(1).appId);

        final AppInfo yellow = configuration.containerSystem.applications.get(0);
        assertEquals(1, yellow.ejbJars.size());
        assertEquals(1, yellow.ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Yellow", yellow.ejbJars.get(0).enterpriseBeans.get(0).ejbName);

        final AppInfo orange = configuration.containerSystem.applications.get(1);
        assertEquals(1, orange.ejbJars.size());
        assertEquals(1, orange.ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", orange.ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps/2000.jar"/>
     * <Deployments dir="myapps"/>
     *
     * Order should be guaranteed to be the same as
     * they are declared in the openejb.xml
     *
     * To test, the jars are named intentionally in an order
     * that would naturally sort to be the reverse.
     *
     * @throws Exception
     */
    @Test
    public void deploymentsFile_and_Dir_Jars_Order() throws Exception {
        final Server server = new Server();

        final File apps = Files.mkdir(server.getBase(), "myapps");

        { // Jar one
            final File jar = server.deploymentsFile("myapps/2000.jar");
            Archives.jarArchive(jar, null, Yellow.class);
        }

        { // Jar two
            server.deploymentsDir("myapps");

            final File jar = Files.path(apps, "1000.jar");
            Archives.jarArchive(jar, null, Orange.class);
        }

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(2, configuration.containerSystem.applications.size());
        assertEquals("2000", configuration.containerSystem.applications.get(0).appId);
        assertEquals("1000", configuration.containerSystem.applications.get(1).appId);

        final AppInfo yellow = configuration.containerSystem.applications.get(0);
        assertEquals(1, yellow.ejbJars.size());
        assertEquals(1, yellow.ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Yellow", yellow.ejbJars.get(0).enterpriseBeans.get(0).ejbName);

        final AppInfo orange = configuration.containerSystem.applications.get(1);
        assertEquals(1, orange.ejbJars.size());
        assertEquals(1, orange.ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", orange.ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * <Deployments dir="myapps/2000.jar"/>
     * <Deployments dir="myapps/2000.jar"/>
     * <Deployments dir="myapps"/>
     * <Deployments dir="myapps"/>
     *
     * Order should be guaranteed to be the same as
     * they are declared in the openejb.xml
     *
     * To test, the jars are named intentionally in an order
     * that would naturally sort to be the reverse.
     *
     * @throws Exception
     */
    @Test
    public void deployments_Duplicates() throws Exception {
        final Server server = new Server();

        final File apps = Files.mkdir(server.getBase(), "myapps");

        { // Jar one
            final File jar = server.deploymentsFile("myapps/2000.jar");
            server.deploymentsFile("myapps/2000.jar");
            Archives.jarArchive(jar, null, Yellow.class);
        }

        { // Jar two
            server.deploymentsDir("myapps");
            server.deploymentsDir("myapps");

            final File jar = Files.path(apps, "1000.jar");
            Archives.jarArchive(jar, null, Orange.class);
        }

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(2, configuration.containerSystem.applications.size());
        assertEquals("2000", configuration.containerSystem.applications.get(0).appId);
        assertEquals("1000", configuration.containerSystem.applications.get(1).appId);

        final AppInfo yellow = configuration.containerSystem.applications.get(0);
        assertEquals(1, yellow.ejbJars.size());
        assertEquals(1, yellow.ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Yellow", yellow.ejbJars.get(0).enterpriseBeans.get(0).ejbName);

        final AppInfo orange = configuration.containerSystem.applications.get(1);
        assertEquals(1, orange.ejbJars.size());
        assertEquals(1, orange.ejbJars.get(0).enterpriseBeans.size());
        assertEquals("Orange", orange.ejbJars.get(0).enterpriseBeans.get(0).ejbName);
    }

    /**
     * We do not treat this as a failure case due to backwards compatibility
     *
     * @throws Exception
     */
    @Test
    public void invalidDir_doesNotExist() throws Exception {
        final Server server = new Server();

        final File dir = server.deploymentsDir("myapps");
        Files.delete(dir);

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(0, configuration.containerSystem.applications.size());
    }

    @Test
    public void invalidDir_notADirectory() throws Exception {
        exceptions.expect(RuntimeException.class);
        exceptions.expectMessage("Deployments dir=");
        exceptions.expectMessage("Not a directory");
        exceptions.expectMessage("myapps");

        final Server server = new Server();

        final File dir = server.deploymentsDir("myapps");

        // turn the directory into a file
        Files.delete(dir);
        assertTrue(dir.createNewFile());

        server.init();
    }

    @Test
    public void invalidDir_notReadable() throws Exception {

        if (!System.getProperty("os.name", "unknown").toLowerCase().startsWith("win")
            && !"root".equals(System.getProperty("user.name", "openejb"))) {

            //File.setReadable(false) does nothing on win platforms

            exceptions.expect(RuntimeException.class);
            exceptions.expectMessage("Deployments dir=");
            exceptions.expectMessage("Not readable");
            exceptions.expectMessage("myapps");
            final Server server = new Server();

            final File dir = server.deploymentsDir("myapps");

            assertTrue(dir.setReadable(false));

            final OpenEjbConfiguration configuration = server.init();
            assertEquals(0, configuration.containerSystem.applications.size());
        }
    }

    @Test
    public void invalidFile_notAFile() throws Exception {
        exceptions.expect(RuntimeException.class);
        exceptions.expectMessage("Deployments file=");
        exceptions.expectMessage("Not a file");
        exceptions.expectMessage("myapp.jar");

        final Server server = new Server();

        final File file = server.deploymentsFile("myapp.jar");
        Files.delete(file);
        Files.mkdir(file);

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(0, configuration.containerSystem.applications.size());
    }

    @Test
    public void invalidFile_doesNotExist() throws Exception {
        exceptions.expect(RuntimeException.class);
        exceptions.expectMessage("Deployments file=");
        exceptions.expectMessage("not exist");
        exceptions.expectMessage("myapp.jar");

        final Server server = new Server();

        final File file = server.deploymentsFile("myapp.jar");
        Files.delete(file);

        final OpenEjbConfiguration configuration = server.init();

        assertEquals(0, configuration.containerSystem.applications.size());
    }

    @Test
    public void invalidFile_notReadable() throws Exception {

        if (!System.getProperty("os.name", "unknown").toLowerCase().startsWith("win")
            && !"root".equals(System.getProperty("user.name", "openejb"))) {

            //File.setReadable(false) does nothing on win platforms

            exceptions.expect(RuntimeException.class);
            exceptions.expectMessage("Deployments file=");
            exceptions.expectMessage("Not readable");
            exceptions.expectMessage("myapp.jar");

            final Server server = new Server();

            final File file = server.deploymentsFile("myapp.jar");
            assertTrue(file.createNewFile());
            assertTrue(file.setReadable(false));

            final OpenEjbConfiguration configuration = server.init();
            assertEquals(0, configuration.containerSystem.applications.size());
        }
    }

    /**
     * If something is in the process of being deployed, do not try to autoDeploy it
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void alreadyInDeployProcess() throws Exception {

    }

    public AppInfo select(final List<AppInfo> appInfos, final String id) {
        for (final AppInfo appInfo : appInfos) {
            if (id.equals(appInfo.appId)) {
                return appInfo;
            }
        }

        throw new RuntimeException("No AppInfo with id: " + id);
    }


    @Before
    public void after() {
        SystemInstance.reset();
    }

    @Singleton
    @Startup
    public static class Orange {
    }

    @Singleton
    @Startup
    public static class Yellow {
    }

    private class Server {
        private final File base;
        private final Properties properties;
        private final Openejb openejb = new Openejb();
        private final File configFile;

        private Server() {
            base = Files.tmpdir();

            final File conf = Files.mkdir(base, "conf");

            properties = new Properties();
            properties.setProperty("openejb.deployments.classpath", "false");
            properties.setProperty("openejb.home", base.getAbsolutePath());
            properties.setProperty("openejb.base", base.getAbsolutePath());

            configFile = new File(conf, "openejb.xml");

            properties.setProperty("openejb.configuration", configFile.getAbsolutePath());
        }


        public OpenEjbConfiguration init() throws Exception {
            try {
                IO.writeString(configFile, JaxbOpenejb.marshal(Openejb.class, openejb));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            SystemInstance.init(properties);

            final ConfigurationFactory configurationFactory = new ConfigurationFactory();
            configurationFactory.init(properties);

            return configurationFactory.getOpenEjbConfiguration();
        }

        public File getBase() {
            return base;
        }

        public Properties getProperties() {
            return properties;
        }

        public File deploymentsDir(final String dir) {
            openejb.getDeployments().add(new Deployments().dir(dir));
            return Files.mkdir(base, dir);
        }

        public File deploymentsFile(final String file) {
            openejb.getDeployments().add(new Deployments().file(file));
            return Files.path(base, file);
        }
    }
}
