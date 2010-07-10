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
package org.apache.openejb.config.rules;

import static org.apache.openejb.config.rules.ValidationAssertions.assertFailures;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.util.Join;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * This Statement is the one which runs the test.
 */
public class InvokeMethod extends Statement {
    private ConfigurationFactory config;
    private Assembler assembler;
    // The test method
    private final FrameworkMethod testMethod;
    // The TestCase instance
    private Object target;
    // These are all the keys defined in org.apache.openejb.config.rules.Messages.properties
    private static Set<String> allKeys;
    static {
        ResourceBundle bundle = ResourceBundle.getBundle("org.apache.openejb.config.rules.Messages");
        allKeys = bundle.keySet();
    }

    public InvokeMethod(FrameworkMethod testMethod, Object target) {
        this.testMethod = testMethod;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        List<String> expectedKeys = validateKeys();
        setUp();
        Object obj = testMethod.invokeExplosively(target);
        if (obj instanceof EjbJar) {
            EjbJar ejbJar = (EjbJar) obj;
            try {
                assembler.createApplication(config.configureApplication(ejbJar));
                if (!expectedKeys.isEmpty()) {
                    fail("A ValidationFailedException should have been thrown");
                }
            } catch (ValidationFailedException vfe) {
                if (!expectedKeys.isEmpty()) {
                    assertFailures(expectedKeys, vfe);
                } else {
                    for (ValidationFailure failure : vfe.getFailures()) {
                        System.out.println("failure = " + failure.getMessageKey());
                    }
                    fail("There should be no validation failures");
                }
            }
        }
        tearDown();
    }

    private void setUp() throws Exception {
        config = new ConfigurationFactory();
        assembler = new Assembler();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
    }

    private void tearDown() {}

    /**
     * Tests to see if the keys specified in the @Keys annotation are also available in the org.apache.openejb.config.rules.Messages.properties file. If there are any invalid keys,
     * then it throws an exception and causes the test to error out. If all the keys are valid, then it returns those keys. This list of keys can then be compared with all the Keys
     * in org.apache.openejb.config.rules.Messages.properties and one can then find out the "test coverage" of the keys i.e. this tool can be used to find keys for which tests have
     * not yet been written
     * 
     * @return
     * @throws Exception
     */
    private List<String> validateKeys() throws Exception {
        Keys annotation = testMethod.getAnnotation(Keys.class);
        Key[] keys = annotation.value();
        ArrayList<String> wrongKeys = new ArrayList<String>();
        for (Key key : keys) {
            if (allKeys.contains("1." + key.value())) {
                continue;
            } else {
                wrongKeys.add(key.value());
            }
        }
        if (wrongKeys.isEmpty()) {
            ArrayList<String> validKeys = new ArrayList<String>();
            for (Key key : keys) {
                for (int i = 0; i < key.count(); i++) {
                    validKeys.add(key.value());
                }
            }
            return validKeys;
        } else {
            String commaDelimitedKeys = Join.join(",", wrongKeys);
            throw new Exception("The following keys listed in the @Keys annotation on the method " + testMethod.getName() + "() of " + testMethod.getMethod().getDeclaringClass()
                    + " are invalid : " + commaDelimitedKeys
                    + " . Only keys listed in org.apache.openejb.config.rules.Messages.properties are allowed to be used in this annotation. ");
        }
    }
}
