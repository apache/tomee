/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.cditest.test;

import org.apache.webbeans.cditest.CdiTestContainer;
import org.apache.webbeans.cditest.CdiTestContainerLoader;
import org.junit.Test;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestOwbTestContainer extends TestCase {

    private static final int DEFAULT_VAL = 42;

    @Test
    public void testInstanceRetrieval() throws Exception
    {
        CdiTestContainer cdi = CdiTestContainerLoader.getCdiContainer();
        cdi.bootContainer();
        cdi.startContexts();

        assertAll(cdi, 0);

        cdi.stopContexts();
        cdi.startContexts();

        assertAll(cdi, 0);

        cdi.stopRequestScope();
        cdi.stopSessionScope();
        cdi.stopApplicationScope();
        cdi.startRequestScope();
        cdi.startSessionScope();
        cdi.startApplicationScope();

        assertAll(cdi, 0);

        cdi.stopRequestScope();
        cdi.startRequestScope();

        assertReq(cdi, 0);
        assertSess(cdi, DEFAULT_VAL);
        assertApp(cdi, DEFAULT_VAL);

        cdi.shutdownContainer();
    }

    private void assertAll(CdiTestContainer cdi, int value) {
        assertReq(cdi, value);
        assertSess(cdi, value);
        assertApp(cdi, value);
    }

    private void assertReq(CdiTestContainer cdi, int value)
    {
        RequestScopedTestBean testReq = cdi.getInstance(RequestScopedTestBean.class);
        Assert.assertNotNull(testReq);
        Assert.assertEquals(value, testReq.getI());
        testReq.setI(DEFAULT_VAL);

    }

    private void assertSess(CdiTestContainer cdi, int value)
    {
        SessionScopedTestBean testSess = cdi.getInstance(SessionScopedTestBean.class);
        Assert.assertNotNull(testSess);
        Assert.assertEquals(value, testSess.getI());
        testSess.setI(DEFAULT_VAL);

    }

    private void assertApp(CdiTestContainer cdi, int value)
    {
        ApplicationScopedTestBean testApp = cdi.getInstance(ApplicationScopedTestBean.class);
        Assert.assertNotNull(testApp);
        Assert.assertEquals(value, testApp.getI());
        testApp.setI(DEFAULT_VAL);
    }
}
