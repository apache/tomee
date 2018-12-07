/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.interceptors;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class MethodLevelInterceptorOnlyTest extends TestCase {

    private InitialContext initCtx;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*interceptors/target/classes.*");

        initCtx = new InitialContext(properties);
    }

    @Test
    public void testInterceptedGenerifiedBusinessIntfMethod() throws Exception {
        MethodLevelInterceptorOnlyParent bean = (MethodLevelInterceptorOnlyParent) initCtx.lookup("MethodLevelInterceptorOnlySLSBeanLocal");

        assert bean != null;

        List<String> expected = new ArrayList<String>();
        expected.add("MethodLevelInterceptorOne");
        expected.add("makePersistent");

        List<String> actual = bean.makePersistent(null);
        assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
    }
}