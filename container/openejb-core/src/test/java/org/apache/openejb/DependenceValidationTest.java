/**
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
package org.apache.openejb;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.apache.openejb.loader.IO;
import org.apache.xbean.asm8.ClassReader;
import org.apache.xbean.asm8.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.openejb.util.URLs.toFile;

public class DependenceValidationTest extends TestCase {

    private Map<String, Map<String, Integer>> depsOfPackages;

    private TestResult results;

    @Override
    public void run(final TestResult testResult) {
        results = testResult;
        super.run(testResult);
    }

    public void testPackageDependencies() throws Exception {
        final DependencyVisitor dependencyVisitor = new DependencyVisitor();

        final URL resource = DependenceValidationTest.class.getResource("/org/apache/openejb/OpenEJB.class");
        final File file = toFile(resource);
        dir(file.getParentFile(), dependencyVisitor);

        depsOfPackages = dependencyVisitor.groups;

        // Nothing may depend on the Assembler except the config code and events
        final String dynamicAssembler = "org.apache.openejb.assembler.dynamic";
        assertNotDependentOn("org.apache.tomee", "org.apache.openejb.assembler.classic", "org.apache.openejb.config.typed.util", "org.apache.openejb.assembler", "org.apache.openejb.assembler.classic.util", "org.apache.openejb.config", "org.apache.openejb.assembler.dynamic", "org.apache.openejb.assembler.classic.cmd", "org.apache.openejb.assembler.monitoring", "org.apache.openejb.cdi", "org.apache.openejb.junit", "org.apache.openejb.assembler.classic.event", "org.apache.openejb.web", "org.apache.openejb.testng", "org.apache.openejb.testing");

        // Nothing may depend on the Dynamic Assembler
        assertNotDependentOn("org.apache.tomee", dynamicAssembler);

        // Nothing may depend on the JAXB Tree except the Config code
        assertNotDependentOn("org.apache.tomee", "org.apache.openejb.jee",
                "org.apache.openejb.config", "org.apache.openejb.config.rules", "org.apache.openejb.config.sys",
                "org.apache.openejb.cdi", "org.apache.openejb.junit", "org.apache.openejb.testng", "org.apache.openejb.testing",
                "org.apache.openejb.service");

        // Nothing may depend on the Config code except it's subpackages
        assertNotDependentOn("org.apache.tomee", "org.apache.openejb.config",
                "org.apache.openejb.config.event", "org.apache.openejb.config.typed.util", "org.apache.openejb.config.rules",
                "org.apache.openejb.config.sys", "org.apache.openejb.assembler", "org.apache.openejb.cdi", "org.apache.openejb.junit",
                "org.apache.openejb.testng", "org.apache.openejb.testing", dynamicAssembler, "org.apache.openejb.service");

        // The assembler may not be dependent on the config factory Implementation
        assertNotDependentOn("org.apache.openejb.assembler.classic", "org.apache.openejb.config");

        // Nothing should be dependent on any one particular container implementation   (except the Dynamic Assembler)
        assertNotDependentOn("org.apache.tomee", "org.apache.openejb.core.singleton", dynamicAssembler);
        assertNotDependentOn("org.apache.tomee", "org.apache.openejb.core.stateless", dynamicAssembler);
        assertNotDependentOn("org.apache.tomee", "org.apache.openejb.core.stateful", dynamicAssembler);
        /* TODO: This needs fixing... containers are supposed to be pluggable
        // assertNotDependentOn("org.apache.tomee", "org.apache.openejb.core.entity", dynamicAssembler);
         */
    }

    private void assertNotDependentOn(final String referringPacakge, final String referredPackage, final String... exemptionsArray) {
        if (referringPacakge.equals(referredPackage)) return;
        final List<String> exemptions = new ArrayList<>(Arrays.asList(exemptionsArray));
        exemptions.add(referredPackage);

        for (final Map.Entry<String, Map<String, Integer>> entry : depsOfPackages.entrySet()) {
            final String packageName = entry.getKey();
            if (packageName.startsWith(referringPacakge) && !exemptions.contains(packageName)) {
                try {
                    final Map<String, Integer> deps = entry.getValue();
                    if (deps.containsKey(referredPackage)) {
                        final int references = deps.get(referredPackage);
                        assertEquals(packageName + " should have no dependencies on " + referredPackage + " - #ref " + references, 0, references);
                    }
                } catch (final AssertionFailedError e) {
                    results.addFailure(this, e);
                }
            }
        }
    }

    private static void dir(final File dir, final DependencyVisitor dependencyVisitor) {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    dir(file, dependencyVisitor);
                } else if (file.getName().endsWith(".class")) {
                    file(file, dependencyVisitor);
                }
            }
        }
    }

    private static void file(final File file, final DependencyVisitor dependencyVisitor) {
        try {
            final InputStream in = IO.read(file);
            try {
                final ClassReader classReader = new ClassReader(in);
                classReader.accept(dependencyVisitor, ClassWriter.COMPUTE_MAXS);
            } finally {
                IO.close(in);
            }
        } catch (final IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
