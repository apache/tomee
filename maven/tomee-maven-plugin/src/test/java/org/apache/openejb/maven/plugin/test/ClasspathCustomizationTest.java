/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin.test;

import org.apache.openejb.maven.plugin.Config;
import org.apache.openejb.maven.plugin.TomEEMavenPluginRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClasspathCustomizationTest {
    @Rule
    public TomEEMavenPluginRule TMPRule = new TomEEMavenPluginRule();

    private static final String LOG4J_VERSION = resolveLog4jVersion();

    @Config
    private final List<String> classpaths = asList(
            "org.apache.logging.log4j:log4j-api:" + LOG4J_VERSION,
            "org.apache.logging.log4j:log4j-jul:" + LOG4J_VERSION);

    @Config
    private final File catalinaBase = new File("target/tomee-classpath");

    @Test
    public void log4j2WasCopied() throws Exception {
        final File boot = new File(catalinaBase, "boot");
        assertTrue(boot.isDirectory());
        final File[] log4jJars = boot.listFiles((dir, name) ->
                name.startsWith("log4j-") && name.endsWith("-" + LOG4J_VERSION + ".jar"));
        assertEquals(2, log4jJars.length);
    }

    private static String resolveLog4jVersion() {
        return ClasspathHelper.getJarVersion("log4j-api");
    }
}
