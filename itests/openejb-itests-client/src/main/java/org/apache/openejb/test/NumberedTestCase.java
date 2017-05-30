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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;
import org.junit.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

public class NumberedTestCase extends Assert implements Test {

    Method[] testMethods = new Method[]{};
    protected static final String standardPrefix = "test##_";

    class MethodComparator implements java.util.Comparator {

        public int compare(final Object o1, final Object o2) {
            final Method m1 = (Method) o1;
            final Method m2 = (Method) o2;
            return m1.getName().compareTo(m2.getName());
        }

        public boolean equals(final Object other) {
            if (other instanceof MethodComparator)
                return true;
            else
                return false;
        }
    }


    public NumberedTestCase() {
        try {
            // Get all methods of the subclass
            final Method[] methods = getClass().getMethods();
            final java.util.TreeSet tm = new java.util.TreeSet(new MethodComparator());

            // Add the ones that start with "test"
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().startsWith("test")) {
                    tm.add(methods[i]);
                }
            }
            testMethods = new Method[tm.size()];
            final Iterator orderedMethods = tm.iterator();
            for (int i = 0; orderedMethods.hasNext(); i++) {
                testMethods[i] = (Method) orderedMethods.next();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Counts the number of test cases that will be run by this test.
     */
    public int countTestCases() {
        return testMethods.length;
    }

    /**
     * Runs a test and collects its result in a TestResult instance.
     */
    public void run(final TestResult result) {
        try {
            setUp();
        } catch (final Exception e) {
            e.printStackTrace();
            final Test test = new TestSetup();

            result.addError(test, e);
            return;
        }
        for (int i = 0; i < testMethods.length; i++) {
            run(result, testMethods[i]);
        }
        try {
            tearDown();
        } catch (final Exception e) {
            e.printStackTrace();
            final Test test = new TestTearDown();

            result.addError(test, e);
            return;
        }
    }

    protected void run(final TestResult result, final Method testMethod) {
        final Test test = createTest(testMethod);
        result.startTest(test);
        final Protectable p = new Protectable() {
            public void protect() throws Throwable {
                runTestMethod(testMethod);
            }
        };
        //System.out.println(">>" + NumberedTestCase.class.getName() + "> started: " + testMethod.toGenericString());
        result.runProtected(test, p);
        result.endTest(test);
        //System.out.println(">>" + NumberedTestCase.class.getName() + "> done: " + testMethod.toGenericString());
    }


    protected Test createTest(final Method testMethod) {
        final Test test = new NamedTest(testMethod);
        return test;
    }

    protected void runTestMethod(final Method testMethod) throws Throwable {
        try {
            testMethod.invoke(this, new Class[0]);
        } catch (final InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
        } catch (final IllegalAccessException e) {
            e.fillInStackTrace();
            throw e;
        }
    }


    public String toString() {
        return name();
    }

    public String name() {
        return "";
    }

    protected String createTestName(final Method testMethod) {
        return name() + removePrefix(testMethod.getName());
    }

    protected static String removePrefix(final String name) {
        return removePrefix(standardPrefix, name);
    }

    protected static String removePrefix(final String prefix, final String name) {
        return name.substring(prefix.length());
    }

    public class NamedTest implements Test {
        private final Method testMethod;

        public NamedTest(final Method testMethod) {
            this.testMethod = testMethod;
        }

        public String getName() {
            return createTestName(testMethod);
        }

        public int countTestCases() {
            return 1;
        }

        public void run(final TestResult result) {
        }

        public String toString() {
            return getName();
        }
    }

    public class TestSetup implements Test {
        public int countTestCases() {
            return 0;
        }

        public void run(final TestResult result) {
        }

        public String getName() {
            return name() + ".setUp()";
        }

        public String toString() {
            return getName();
        }
    }

    public class TestTearDown implements Test {
        public int countTestCases() {
            return 0;
        }

        public void run(final TestResult result) {
        }

        public String getName() {
            return name() + ".tearDown()";
        }

        public String toString() {
            return getName();
        }
    }
}


