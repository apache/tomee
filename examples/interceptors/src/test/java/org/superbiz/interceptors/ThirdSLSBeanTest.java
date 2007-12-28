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
package org.superbiz.interceptors;

import org.junit.Before;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
public class ThirdSLSBeanTest {
    private InitialContext initCtx;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*interceptors/target/classes.*");

        initCtx = new InitialContext(properties);
    }

    // TODO: Enable it once OPENEJB-583 gets fixed
    //@Test
    public void testMethodWithDefaultInterceptorsExcluded() throws Exception {
        ThirdSLSBeanLocal bean = (ThirdSLSBeanLocal) initCtx.lookup("ThirdSLSBeanLocal");

        assert bean != null;

        List<String> expected = new ArrayList<String>();
        expected.add("ClassLevelInterceptorOne");
        expected.add("ClassLevelInterceptorTwo");
        expected.add("MethodLevelInterceptorOne");
        expected.add("MethodLevelInterceptorTwo");
        expected.add("ThirdSLSBean");
        expected.add("businessMethod");

        List<String> actual = bean.businessMethod();
        assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
    }

    // TODO: Enable it once OPENEJB-583 gets fixed
    //@Test
    public void testMethodWithDefaultAndClassInterceptorsExcluded() throws Exception {
        ThirdSLSBeanLocal bean = (ThirdSLSBeanLocal) initCtx.lookup("ThirdSLSBeanLocal");

        assert bean != null;

        List<String> expected = new ArrayList<String>();
        expected.add("MethodLevelInterceptorOne");
        expected.add("MethodLevelInterceptorTwo");
        expected.add("ThirdSLSBean");
        expected.add("anotherBusinessMethod");

        List<String> actual = bean.anotherBusinessMethod();
        assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
    }
}
