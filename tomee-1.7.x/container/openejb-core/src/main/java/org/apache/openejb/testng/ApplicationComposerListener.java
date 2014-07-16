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

package org.apache.openejb.testng;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.testing.ApplicationComposers;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class ApplicationComposerListener implements IInvokedMethodListener {
    private ApplicationComposers delegate;

    @Override
    public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
        delegate = new ApplicationComposers(method.getTestMethod().getRealClass());
        try {
            delegate.before(testResult.getInstance());
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        try {
            delegate.after();
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }
}
