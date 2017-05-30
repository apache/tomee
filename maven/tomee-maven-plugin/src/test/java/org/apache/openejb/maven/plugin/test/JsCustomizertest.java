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

import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.openejb.maven.plugin.Config;
import org.apache.openejb.maven.plugin.TomEEMavenPluginRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.zip.ZipFile;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;

public class JsCustomizertest {
    @Rule
    public final TomEEMavenPluginRule TMPRule = new TomEEMavenPluginRule().noRun();

    @Config
    private final List<String> jsCustomizers = singletonList(
        // copy junit in lib/junit-test.jar
        "var File = Java.type('java.io.File');" +
        "var Files = Java.type('java.nio.file.Files');" +
        "var StandardCopyOption = Java.type('java.nio.file.StandardCopyOption');" +
        "" +
        "var junit = resolver.resolve('junit', 'junit', '4.12');" +
        "Files.copy(junit.toPath(), new File(catalinaBase, 'lib/JsCustomizertest.jar').toPath(), StandardCopyOption.REPLACE_EXISTING);"

    );
    @Config
    private final File catalinaBase = new File("target/JsCustomizertest");

    @Test
    public void run() throws Exception {
        final File file = new File(catalinaBase, "lib/JsCustomizertest.jar");
        assertTrue(file.isFile());
        assertTrue(Collections.list(new ZipFile(file).entries()).size() > 300);
    }
}
