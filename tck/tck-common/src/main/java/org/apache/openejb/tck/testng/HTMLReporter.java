/*
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
package org.apache.openejb.tck.testng;

import org.testng.ITestResult;
import org.testng.reporters.TestHTMLReporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class HTMLReporter extends TestHTMLReporter {
    private static final Class<?>[] API = new Class<?>[]{ITestResult.class};

    @Override
    public List<ITestResult> getFailedTests() {
        return doWrap(super.getFailedTests());
    }

    @Override
    public List<ITestResult> getPassedTests() {
        return doWrap(super.getPassedTests());
    }

    private List<ITestResult> doWrap(final List<ITestResult> raw) {
        final List<ITestResult> wrapped = new ArrayList<>(raw.size());
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (final ITestResult result : raw) {
            wrapped.add(ITestResult.class.cast(
                Proxy.newProxyInstance(loader, API,
                    new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if (method.getName().equals("getParameters")) {
                                return new Object[method.getParameterTypes().length];
                            }
                            return method.invoke(result, args);
                        }
                    })
            ));
        }
        return wrapped;
    }
}
