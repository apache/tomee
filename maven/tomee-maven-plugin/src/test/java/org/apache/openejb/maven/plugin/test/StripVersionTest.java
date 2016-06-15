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
import java.io.FilenameFilter;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StripVersionTest {
    @Rule
    public TomEEMavenPluginRule TMPRule = new TomEEMavenPluginRule();

    @Config
    private final List<String> javaagents = singletonList("org.apache.sirona:sirona-javaagent:0.2-incubating:jar:shaded");

    @Config
    private final List<String> libs = singletonList("org.codehaus.plexus:plexus-utils:3.0.17");

    @Config
    private final List<String> webapps = singletonList("org.apache.tomee:tomee-webaccess:7.0.0" /*use release to avoid nasty deps*/);

    @Config // otherwise they are not copied
    private final boolean persistJavaagents = true;

    @Config
    private boolean stripVersion = true;

    @Config
    private final File catalinaBase = new File("target/tomee-stripversion");

    @Test
    public void sironaIsInstalledAndPersisted() throws Exception {
        assertTrue(catalinaBase.exists());

        assertEquals(1, new File(catalinaBase, "javaagent").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.equals("sirona-javaagent-shaded.jar");
            }
        }).length);
        assertEquals(1, new File(catalinaBase, "lib").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return  name.equals("log4j2-tomee.jar");
            }
        }).length);
        assertEquals(1, new File(catalinaBase, "webapps").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.equals("tomee-webaccess.war");
            }
        }).length);

    }
}
