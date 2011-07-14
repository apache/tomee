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
package org.apache.openejb.tck.cdi.tomee;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class RequestScopeTestListener implements ITestListener {

    private boolean entered = false;
    private ThreadContext oldContext;

    @Override
    public void onTestStart(ITestResult iTestResult) {
        entered = true;
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        if (containerSystem == null) return;

        BeanContext context = null;
        for (BeanContext beanContext : containerSystem.deployments()) {
            BeanType beanType = BeanType.MANAGED;
            if (beanContext.getComponentType() == beanType) {
                context = beanContext;
                break;
            }
        }

        if (context == null) return;

        ThreadContext newContext = new ThreadContext(context, null);
        oldContext = ThreadContext.enter(newContext);

    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        exit();
    }

    private void exit() {
        try {
            ThreadContext.exit(oldContext);
        } catch (Exception e) {
        }
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        exit();
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        exit();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
    }

    @Override
    public void onStart(ITestContext iTestContext) {
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
    }
}
