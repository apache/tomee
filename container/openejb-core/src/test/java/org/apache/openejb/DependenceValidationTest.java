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
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;


/**
 * @version $Rev$ $Date$
 */
public class DependenceValidationTest extends TestCase {

    private Map<String, Map<String, Integer>> depsOfPackages;

    private TestResult results;

    public void run(TestResult testResult) {
        results = testResult;
        super.run(testResult);
    }

    public void testAssembler() throws Exception {
        DependencyVisitor dependencyVisitor = new DependencyVisitor();

        URL resource = DependenceValidationTest.class.getResource("/org/apache/openejb/OpenEJB.class");
        File file = new File(resource.getPath());
        dir(file.getParentFile(), dependencyVisitor);

        depsOfPackages = dependencyVisitor.groups;

        // Nothing may depend on the Assembler except the config code or spring assembler
        String springAssembler = "org.apache.openejb.assembler.spring";
        String dynamicAssembler = "org.apache.openejb.assembler.dynamic";
        assertNotDependentOn("org.apache.openejb", "org.apache.openejb.assembler.classic", "org.apache.openejb.assembler", "org.apache.openejb.config", springAssembler, "org.apache.openejb.assembler.dynamic");

        // Nothing may depend on the Spring Assembler
        assertNotDependentOn("org.apache.openejb", springAssembler);

        // Nothing may depend on the Dynamic Assembler
        assertNotDependentOn("org.apache.openejb", dynamicAssembler);

        // Nothing may depend on the JAXB Tree except the Config code
        assertNotDependentOn("org.apache.openejb", "org.apache.openejb.jee", "org.apache.openejb.config", "org.apache.openejb.config.rules");

        // Nothing may depend on the Config code except it's subpackages and the Spring Assembler
        assertNotDependentOn("org.apache.openejb", "org.apache.openejb.config", "org.apache.openejb.config.rules", "org.apache.openejb.config.sys", "org.apache.openejb.assembler", springAssembler, dynamicAssembler);

        // The assembler may not be dependent on the config factory Implementation
        assertNotDependentOn("org.apache.openejb.assembler.classic", "org.apache.openejb.config");

        // Nothing should be dependent on any one particular container implementation   (except the Spring and Dynamic Assembler)
        // TODO: This needs fixing... containers are supposed to be pluggable
//        assertNotDependentOn("org.apache.openejb", "org.apache.openejb.core.stateless", springAssembler, dynamicAssembler);
//        assertNotDependentOn("org.apache.openejb", "org.apache.openejb.core.stateful", springAssembler, dynamicAssembler);
//        assertNotDependentOn("org.apache.openejb", "org.apache.openejb.core.entity", "org.apache.openejb.alt.containers.castor_cmp11", springAssembler, dynamicAssembler);
//        assertNotDependentOn("org.apache.openejb", "org.apache.openejb.alt.containers.castor_cmp11", springAssembler, dynamicAssembler);
    }

    private void assertNotDependentOn(String referringPacakge, String referredPackage, String... exemptionsArray) {
        if (referringPacakge.equals(referredPackage)) return;
        List exemptions = new ArrayList(Arrays.asList(exemptionsArray));
        exemptions.add(referredPackage);

        for (Map.Entry<String, Map<String, Integer>> entry : depsOfPackages.entrySet()) {
            String packageName = entry.getKey();
            if (packageName.startsWith(referringPacakge) && !exemptions.contains(packageName)) {
                try {
                    Map<String, Integer> deps = entry.getValue();
                    if (deps.containsKey(referredPackage)) {
                        int references = deps.get(referredPackage);
                        assertEquals(packageName + " should have no dependencies on " + referredPackage, 0, references);
                    }
                } catch (AssertionFailedError e) {
                    results.addFailure(this, e);
                }
            }
        }
    }

    private static void dir(File dir, DependencyVisitor dependencyVisitor) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                dir(file, dependencyVisitor);
            } else if (file.getName().endsWith(".class")) {
                file(file, dependencyVisitor);
            }
        }
    }

    private static void file(File file, DependencyVisitor dependencyVisitor) {
        try {
            FileInputStream in = new FileInputStream(file);
            try {
                ClassReader classReader = new ClassReader(in);
                classReader.accept(dependencyVisitor, true);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
