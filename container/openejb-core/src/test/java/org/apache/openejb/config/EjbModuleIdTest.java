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

import org.junit.Assert;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Archives;
import org.junit.Test;

import jakarta.ejb.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OPENEJB-1442
 *
 * @version $Rev$ $Date$
 */
public class EjbModuleIdTest extends Assert {

    @Test
    public void testDefault() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("META-INF/ejb-jar.xml", "<ejb-jar/>");

        final File file = Archives.jarArchive(map, "test", OrangeBean.class);

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();
        final AppInfo appInfo = factory.configureApplication(file);
        final EjbJarInfo ejbJarInfo = appInfo.ejbJars.get(0);

        assertEquals(file.getName().substring(0, file.getName().length() - 4), ejbJarInfo.moduleName);
    }

    @Test
    public void testId() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"orange\"/>");

        final File file = Archives.jarArchive(map, "test", OrangeBean.class);

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();
        final AppInfo appInfo = factory.configureApplication(file);
        final EjbJarInfo ejbJarInfo = appInfo.ejbJars.get(0);

        assertEquals("orange", ejbJarInfo.moduleName);
    }

    @Test
    public void testModuleName() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("META-INF/ejb-jar.xml", "<ejb-jar><module-name>orange</module-name></ejb-jar>");

        final File file = Archives.jarArchive(map, "test", OrangeBean.class);

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();
        final AppInfo appInfo = factory.configureApplication(file);
        final EjbJarInfo ejbJarInfo = appInfo.ejbJars.get(0);

        assertEquals("orange", ejbJarInfo.moduleName);
    }


    @Test
    public void testModuleNameAndId() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"orangeId\"><module-name>orangeName</module-name></ejb-jar>");

        final File file = Archives.jarArchive(map, "test", OrangeBean.class);

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();
        final AppInfo appInfo = factory.configureApplication(file);
        final EjbJarInfo ejbJarInfo = appInfo.ejbJars.get(0);

        assertEquals("orangeName", ejbJarInfo.moduleName);
    }

    /**
     * OPENEJB-1555
     *
     * @throws Exception
     */
    @Test
    public void testSystemProperty() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"orangeId\"><module-name>orangeName</module-name></ejb-jar>");

        final File file = Archives.jarArchive(map, "test", OrangeBean.class);

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        SystemInstance.get().setProperty(file.getName() + ".moduleId", "orangeSystem");

        final AppInfo appInfo = factory.configureApplication(file);
        final EjbJarInfo ejbJarInfo = appInfo.ejbJars.get(0);

        assertEquals("orangeSystem", ejbJarInfo.moduleName);
    }

    /**
     * OPENEJB-1366
     *
     * @throws Exception
     */
    @Test
    public void testInvalidNames() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("META-INF/ejb-jar.xml", "<ejb-jar/>");

        final List<String> dirs = new ArrayList<>();
        dirs.add("orangeDir");
        dirs.add("classes");   // invalid
        dirs.add("test-classes");   // invalid
        dirs.add("target");   // invalid
        dirs.add("build");   // invalid
        dirs.add("dist");   // invalid
        dirs.add("bin");   // invalid

        final File file = Archives.fileArchive(map, dirs, OrangeBean.class);

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();
        final AppInfo appInfo = factory.configureApplication(file);
        final EjbJarInfo ejbJarInfo = appInfo.ejbJars.get(0);

        assertEquals("orangeDir", ejbJarInfo.moduleName);
    }


    @Singleton
    public static class OrangeBean {

    }
}
