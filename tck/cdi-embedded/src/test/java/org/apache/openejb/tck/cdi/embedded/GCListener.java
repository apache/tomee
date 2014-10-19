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
package org.apache.openejb.tck.cdi.embedded;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener2;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class GCListener implements IInvokedMethodListener2 {
    private final MemoryMXBean memory;

    public GCListener() {
        memory = ManagementFactory.getMemoryMXBean();
    }

    @Override
    public void beforeInvocation(final IInvokedMethod iInvokedMethod, final ITestResult iTestResult, final ITestContext iTestContext) {
        dump("b");
    }

    @Override
    public void afterInvocation(final IInvokedMethod iInvokedMethod, final ITestResult iTestResult, final ITestContext iTestContext) {
        dump("a");
    }

    private void dump(final String prefix) {
        System.out.println(prefix + ">>> heap             : " + memory.getHeapMemoryUsage().toString());
        System.out.println(prefix + ">>> non heap         : " + memory.getNonHeapMemoryUsage().toString());
        System.out.println(prefix + ">>> pending instances: " + memory.getObjectPendingFinalizationCount());
    }

    @Override
    public void beforeInvocation(final IInvokedMethod iInvokedMethod, final ITestResult iTestResult) {
        // no-op
    }

    @Override
    public void afterInvocation(final IInvokedMethod iInvokedMethod, final ITestResult iTestResult) {
        // no-op
    }
}
