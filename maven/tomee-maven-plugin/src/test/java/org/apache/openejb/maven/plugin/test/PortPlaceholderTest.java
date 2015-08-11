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

import org.apache.openejb.loader.IO;
import org.apache.openejb.maven.plugin.Config;
import org.apache.openejb.maven.plugin.TomEEMavenPluginRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class PortPlaceholderTest {
    @Rule
    public final TomEEMavenPluginRule TMPRule = new TomEEMavenPluginRule().noRun();

    @Config
    private final String tomeeHttpPort = "${http.port}";

    @Config
    private final File catalinaBase = new File("target/tomee-placeholder");

    @Test
    public void run() throws Exception {
        assertTrue(IO.slurp(new File(catalinaBase, "conf/server.xml")).contains("<Connector port=\"" + tomeeHttpPort + "\""));
    }
}
