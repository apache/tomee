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
package org.apache.openejb.meta;

import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class MetaRunner extends BlockJUnit4ClassRunner {
    public MetaRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * Flags an error if you have annotated a test with both @Test and @Keys. Any method annotated with @Test will be ignored anyways.
     */
    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        List<FrameworkMethod> methodsAnnotatedWithKeys = getTestClass().getAnnotatedMethods(MetaTest.class);
        for (FrameworkMethod frameworkMethod : methodsAnnotatedWithKeys) {
            if (frameworkMethod.getAnnotation(Test.class) != null) {
                String gripe = "The method " + frameworkMethod.getName() + "() can only be annotated with @MetaTest";
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
        Statement statement = new MetaTest.$(method, test);
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
        TestClass testClass = getTestClass();
        List<FrameworkMethod> methods = testClass.getAnnotatedMethods(MetaTest.class);
        return methods;
    }
}
