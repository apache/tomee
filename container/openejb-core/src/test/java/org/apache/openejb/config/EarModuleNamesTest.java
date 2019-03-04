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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Archives;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * @version $Rev$ $Date$
 */
public class EarModuleNamesTest {
    private static final String[] ORIGINAL_EXCLUSIONS = NewLoaderLogic.getExclusions();

    @BeforeClass
    public static void preventDefaults() {
        System.setProperty("openejb.environment.default", "false");
        SystemInstance.reset();
        // we use it in a bunch of other tests but not here
        NewLoaderLogic.setExclusions(
                Stream.concat(Stream.of(ORIGINAL_EXCLUSIONS),
                        Stream.of("openejb-itest", "failover-ejb"))
                      .toArray(String[]::new));
    }

    @AfterClass
    public static void reset() {
        System.clearProperty("openejb.environment.default");
        NewLoaderLogic.setExclusions(ORIGINAL_EXCLUSIONS);
        SystemInstance.reset();
    }

    @Test
    public void testDefaultIdEjbJar() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "testDefaultIdEjbJar.ear");

        final Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("testDefaultIdEjbJar.jar", Archives.jarArchive(Orange.class));
        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.ejbJars.size(), 1);
        assertEquals("testDefaultIdEjbJar", appInfo.ejbJars.get(0).moduleId);
    }

    @Test
    public void testDefaultIdWebapp() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "testDefaultIdWebapp.ear");

        final Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("testDefaultIdWebapp.war", Archives.jarArchive(Orange.class));
        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.ejbJars.size(), 1);
        assertEquals("testDefaultIdWebapp", appInfo.webApps.get(0).moduleId);
    }

    @Test
    public void testModuleNameEjbJar() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "testModuleNameEjbJar.ear");
        final Map<String, Object> contents = new HashMap<String, Object>();

        final Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("META-INF/ejb-jar.xml", "<ejb-jar><module-name>orange</module-name></ejb-jar>");
        final File ejbJar = Archives.jarArchive(metaInf, "orange", Orange.class);
        contents.put("green.jar", ejbJar);

        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.ejbJars.size(), 1);
        assertEquals("orange", appInfo.ejbJars.get(0).moduleId);
    }

    @Test
    public void testModuleNameAppClient() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "testModuleNameAppClient.ear");
        final Map<String, Object> contents = new HashMap<String, Object>();

        final Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("META-INF/application-client.xml", "<application-client><module-name>testModuleNameAppClient</module-name></application-client>");
        final File ejbJar = Archives.jarArchive(metaInf, "testModuleNameAppClient", Orange.class);
        contents.put("green.jar", ejbJar);

        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.clients.size(), 1);
        assertEquals("testModuleNameAppClient", appInfo.clients.get(0).moduleId);
    }

    @Test
    public void testModuleNameWebapp() throws Exception {
        final File appsDir = Files.tmpdir();
        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "testModuleNameWebapp.ear");
        final Map<String, Object> contents = new HashMap<String, Object>();

        final Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("WEB-INF/web.xml", "<webapp><module-name>testModuleNameWebapp</module-name></webapp>");
        final File ejbJar = Archives.jarArchive(metaInf, "testModuleNameWebapp", Orange.class);
        contents.put("green.war", ejbJar);

        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.webApps.size(), 1);
        assertEquals("testModuleNameWebapp", appInfo.webApps.get(0).moduleId);
    }

    @Test
    public void testIdEjbJar() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "testIdEjbJar.ear");

        final Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"testIdEjbJar\" />");
        final File ejbJar = Archives.jarArchive(metaInf, "testIdEjbJar", Orange.class);

        final Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("green.jar", ejbJar);
        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.ejbJars.size(), 1);
        assertEquals("testIdEjbJar", appInfo.ejbJars.get(0).moduleId);
    }

    @Test
    public void testIdApplicationClient() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "testIdApplicationClient.ear");

        final Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("META-INF/application-client.xml", "<application-client id=\"testIdApplicationClient\" />");
        final File jar = Archives.jarArchive(metaInf, "testIdApplicationClient", Orange.class);

        final Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("green.jar", jar);
        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.clients.size(), 1);
        assertEquals("testIdApplicationClient", appInfo.clients.get(0).moduleId);
    }

    @Test
    public void testIdWebapp() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "testIdWebapp.ear");
        final Map<String, Object> contents = new HashMap<String, Object>();

        final Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("WEB-INF/web.xml", "<webapp id=\"testIdWebapp\" />");
        final File ejbJar = Archives.jarArchive(metaInf, "testIdWebapp", Orange.class);
        contents.put("green.war", ejbJar);

        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.webApps.size(), 1);
        assertEquals("testIdWebapp", appInfo.webApps.get(0).moduleId);
    }

    @Singleton
    public static class Orange {

    }

    @Singleton
    public static class Yellow {

    }

}
