/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tomee.loader;

import org.apache.tomcat.JarScanType;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TomEEJarScannerTest {
    @Test
    public void check() {
        {
            final TomEEJarScanner.TomEEFilter filter = new TomEEJarScanner.TomEEFilter();
            assertFalse(filter.check(JarScanType.OTHER, "log4j.jar"));
            assertFalse(filter.check(JarScanType.TLD, "log4j.jar"));
            assertFalse(filter.check(JarScanType.PLUGGABILITY, "log4j.jar"));
            assertTrue(filter.check(JarScanType.PLUGGABILITY, "simple-log4j.jar"));
            assertTrue(filter.check(JarScanType.TLD, "simple-log4j.jar"));
            assertTrue(filter.check(JarScanType.OTHER, "simple-log4j.jar"));
        }
        {
            final TomEEJarScanner.TomEEFilter filter = new TomEEJarScanner.TomEEFilter(new StandardJarScanFilter() {{
                setTldSkip(".*log4j.*");
                setDefaultTldScan(false);
                setDefaultPluggabilityScan(false);
            }});
            assertFalse(filter.check(JarScanType.OTHER, "log4j.jar"));
            assertFalse(filter.check(JarScanType.TLD, "log4j.jar"));
            assertFalse(filter.check(JarScanType.PLUGGABILITY, "log4j.jar"));
            assertFalse(filter.check(JarScanType.PLUGGABILITY, "simple-log4j.jar"));
            assertFalse(filter.check(JarScanType.TLD, "simple-log4j.jar"));
            assertTrue(filter.check(JarScanType.OTHER, "simple-log4j.jar"));
        }
    }
}
