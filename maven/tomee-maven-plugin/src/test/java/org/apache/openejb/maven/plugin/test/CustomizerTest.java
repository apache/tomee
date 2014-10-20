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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CustomizerTest {
    @Rule
    public TomEEMavenPluginRule TMPRule = new TomEEMavenPluginRule();

    @Config
    private final List<String> customizers = asList(MyCustomizer.class.getName(), MyCustomizer2.class.getName());

    @Test
    public void wasExecutedAndCorrectlyInitialized() throws Exception {
        assertNotNull(MyCustomizer.BASE);
        assertTrue(MyCustomizer2.DONE);
    }

    public static class MyCustomizer implements Runnable {
        private static File BASE;
        private final File base;

        public MyCustomizer(final File base) {
            this.base = base;
        }

        @Override
        public void run() {
            BASE = base;
        }
    }
    public static class MyCustomizer2 implements Runnable {
        private static boolean DONE;

        @Override
        public void run() {
            DONE = true;
        }
    }
}
