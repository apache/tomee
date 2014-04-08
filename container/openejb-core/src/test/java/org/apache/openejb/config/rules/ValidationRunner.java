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

import java.util.List;

import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * This class is created specifically to write tests which test OpenEjb validation code. Specifically, it is used to check the usage of keys defined in
 * org.apache.openejb.config.rules.Messages.properties. To use this runner, simply annotate your test case with @RunWith(ValidationRunner.class). Here are some things to keep in
 * mind when writing tests: 1. A test method needs to be annotated with org.apache.openejb.config.rules.Keys instead of the org.junit.Test 2. Any usage of the @Test annotation will
 * be ignored 3. If the @Keys and @Test annotation are used together on a test method, then the TestCase will error out 4. Every test method should create a EjbJar and return it
 * from the method. It should list the keys being tested in the @Keys annotation 5. The runner will invoke the test method and use the Assembler and ConfigurationFactory to create
 * the application 6. This will kick off validation and this Runner will catch ValidationFailureException and make sure that all the keys specified in the @Keys annotation show up
 * in the ValidationFailureException 7. If the keys listed in the @Keys annotation match the keys found in the ValidationFailureException, the test passes, else the test fails. 8.
 * This Runner also validates that the keys specified in the @Keys annotation are also available in the org.apache.openejb.config.rules.Messages.properties file. If the key is not
 * found, then the Runner throws and exception resulting in your test case not being allowed to run. Sometimes you want to write a test where you do not want any
 * ValidationFailureException to be thrown, in those scenarios, simply annotate your test with @Keys and do not specify any @Key in it
 */
public class ValidationRunner extends BlockJUnit4ClassRunner {
    public ValidationRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * Flags an error if you have annotated a test with both @Test and @Keys. Any method annotated with @Test will be ignored anyways.
     */
    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        List<FrameworkMethod> methodsAnnotatedWithKeys = getTestClass().getAnnotatedMethods(Keys.class);
        for (FrameworkMethod frameworkMethod : methodsAnnotatedWithKeys) {
            if (frameworkMethod.getAnnotation(Test.class) != null) {
                String gripe = "The method " + frameworkMethod.getName() + "() can only be annotated with @Keys";
                errors.add(new Exception(gripe));
            }
        }
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        Object test;
        try {
            test = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable e) {
            return new Fail(e);
        }
        Statement statement = new InvokeMethod(method, test);
        statement = withBefores(method, test, statement);
        statement = withAfters(method, test, statement);
        return statement;
    }

    /**
     * By default JUnit includes all methods annotated with @Test. This method only allows methods annotated with @Keys to be included in the list of methods to be run in a
     * TestCase. Any @Test methods will be ignored
     */
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(Keys.class);
    }
}
