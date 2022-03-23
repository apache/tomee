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

import jakarta.ejb.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class EarUnpackTest extends TestCase {

    public void test() throws Exception {
        final File appsDir = Files.tmpdir();

        new Assembler(); //Self register
        final ConfigurationFactory factory = new ConfigurationFactory();

        final File ear = new File(appsDir, "colors.ear");

        { // First Version of the EAR
            final Map<String, Object> contents = new HashMap<>();
            contents.put("orange.jar", Archives.jarArchive(Orange.class));
            Archives.jarArchive(ear, contents);

            final AppInfo appInfo = factory.configureApplication(ear);
            assertEquals(1, appInfo.ejbJars.size());
            assertEquals("orange", appInfo.ejbJars.get(0).moduleId);
        }

        { // First Version of the EAR
            final Map<String, Object> contents = new HashMap<>();
            contents.put("yellow.jar", Archives.jarArchive(Yellow.class));
            Archives.jarArchive(ear, contents);

            final AppInfo appInfo = factory.configureApplication(ear);
            assertEquals(1, appInfo.ejbJars.size());
            assertEquals("yellow", appInfo.ejbJars.get(0).moduleId);
        }

    }


    @Singleton
    public static class Orange {

    }

    @Singleton
    public static class Yellow {

    }

}
