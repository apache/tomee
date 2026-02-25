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

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.reporters.TestHTMLReporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HTMLReporter extends TestHTMLReporter {
    private static final Class<?>[] API = new Class<?>[]{ITestResult.class};

    @Override
    public void onFinish(final ITestContext context) {
        // wrap the result and add them again. They should replace previous entries
        context.getPassedConfigurations().getAllResults().addAll(doWrap(context.getPassedConfigurations().getAllResults()));
        context.getFailedConfigurations().getAllResults().addAll(doWrap(context.getFailedConfigurations().getAllResults()));
        context.getSkippedConfigurations().getAllResults().addAll(doWrap(context.getSkippedConfigurations().getAllResults()));
        context.getPassedTests().getAllResults().addAll(doWrap(context.getPassedTests().getAllResults()));
        context.getFailedTests().getAllResults().addAll(doWrap(context.getFailedTests().getAllResults()));
        context.getSkippedTests().getAllResults().addAll(doWrap(context.getSkippedTests().getAllResults()));
        context.getFailedButWithinSuccessPercentageTests().getAllResults().addAll(doWrap(context.getFailedButWithinSuccessPercentageTests().getAllResults()));
        super.onFinish(context);
    }



    private List<ITestResult> doWrap(final Set<ITestResult> raw) {
        final List<ITestResult> wrapped = new ArrayList<>(raw.size());
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (final ITestResult result : raw) {
            wrapped.add(ITestResult.class.cast(
                Proxy.newProxyInstance(loader, API,
                        (proxy, method, args) -> {
                            if (method.getName().equals("getParameters")) {
                                return new Object[method.getParameterTypes().length];
                            }
                            return method.invoke(result, args);
                        })
            ));
        }
        return wrapped;
    }
}
