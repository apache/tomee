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
package org.apache.openejb.examples.interceptors;

import static org.junit.Assert.*;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class FullyInterceptedTest {

    private InitialContext initCtx;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*interceptors.*");

        initCtx = new InitialContext(properties);
    }

    @Test
    public void testBusinessMethod() throws Exception {
        FullyIntercepted fullyIntercepted = (FullyIntercepted) initCtx.lookup("FullyInterceptedBeanLocal");

        assertNotNull(fullyIntercepted);

        String expected = "businessMethod-bean-methodone-methodtwo-classone-classtwo";

        String actual = fullyIntercepted.businessMethod();
        assert expected.equals(actual) : "Expected \"" + expected + "\", but got \"" + actual + "\"";

    }

    @After
    public void tearDown() throws Exception {
        initCtx.close();
    }

}
