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
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.Files;
import org.apache.openejb.util.Archives;

import javax.ejb.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class EarModuleNamesTest extends TestCase {

    public void testDefaultIdEjbJar() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");

        Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("orange.jar", Archives.jarArchive(Orange.class));
        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.ejbJars.size(), 1);
        assertEquals("orange", appInfo.ejbJars.get(0).moduleId);
    }

    public void testDefaultIdWebapp() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");

        Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("orange.war", Archives.jarArchive(Orange.class));
        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.ejbJars.size(), 1);
        assertEquals("orange", appInfo.webApps.get(0).moduleId);
    }

    public void testModuleNameEjbJar() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");
        Map<String, Object> contents = new HashMap<String, Object>();

        Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("META-INF/ejb-jar.xml", "<ejb-jar><module-name>orange</module-name></ejb-jar>");
        final File ejbJar = Archives.jarArchive(metaInf, "orange", Orange.class);
        contents.put("green.jar", ejbJar);

        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.ejbJars.size(), 1);
        assertEquals("orange", appInfo.ejbJars.get(0).moduleId);
    }

    public void testModuleNameAppClient() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");
        Map<String, Object> contents = new HashMap<String, Object>();

        Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("META-INF/application-client.xml", "<application-client><module-name>orange</module-name></application-client>");
        final File ejbJar = Archives.jarArchive(metaInf, "orange", Orange.class);
        contents.put("green.jar", ejbJar);

        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.clients.size(), 1);
        assertEquals("orange", appInfo.clients.get(0).moduleId);
    }

    public void testModuleNameWebapp() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");
        Map<String, Object> contents = new HashMap<String, Object>();

        Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("WEB-INF/web.xml", "<webapp><module-name>orange</module-name></webapp>");
        final File ejbJar = Archives.jarArchive(metaInf, "orange", Orange.class);
        contents.put("green.war", ejbJar);

        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.webApps.size(), 1);
        assertEquals("orange", appInfo.webApps.get(0).moduleId);
    }

    public void testIdEjbJar() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");

        Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"orange\" />");
        final File ejbJar = Archives.jarArchive(metaInf, "orange", Orange.class);

        Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("green.jar", ejbJar);
        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.ejbJars.size(), 1);
        assertEquals("orange", appInfo.ejbJars.get(0).moduleId);
    }

    public void testIdApplicationClient() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");

        Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("META-INF/application-client.xml", "<application-client id=\"orange\" />");
        final File jar = Archives.jarArchive(metaInf, "orange", Orange.class);

        Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("green.jar", jar);
        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.clients.size(), 1);
        assertEquals("orange", appInfo.clients.get(0).moduleId);
    }

    public void testIdWebapp() throws Exception {
        final File appsDir = Files.tmpdir();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");
        Map<String, Object> contents = new HashMap<String, Object>();

        Map<String, Object> metaInf = new HashMap<String, Object>();
        metaInf.put("WEB-INF/web.xml", "<webapp id=\"orange\" />");
        final File ejbJar = Archives.jarArchive(metaInf, "orange", Orange.class);
        contents.put("green.war", ejbJar);

        Archives.jarArchive(ear, contents);

        final AppInfo appInfo = factory.configureApplication(ear);
        assertEquals(appInfo.webApps.size(), 1);
        assertEquals("orange", appInfo.webApps.get(0).moduleId);
    }




    @Singleton
    public static class Orange {

    }

    @Singleton
    public static class Yellow {

    }

}
