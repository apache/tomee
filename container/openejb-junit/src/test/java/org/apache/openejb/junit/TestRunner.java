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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.junit;

import org.apache.openejb.junit.TestResource;
import org.apache.openejb.junit.TestResourceTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.InitialContext;
import java.util.Hashtable;

@RunWith(OpenEjbRunner.class)
public class TestRunner {
    @TestResource(TestResourceTypes.INITIALCONTEXT)
    private InitialContext initialContext;

    @TestResource(TestResourceTypes.CONTEXT_CONFIG)
    private Hashtable<String, String> contextConfig;

    private boolean calledBefore = false;

    public TestRunner() {
    }

    @Before
    public void setUp() {
        calledBefore = true;
    }

    @Test
    public void testStatement() {
        // we assume that if Before was called, then they'll all be called. It's not
        // really possible to test the class teardown reliably, though we're only
        // really testing if our integration works as expected, so if the runner was
        // execute, and the Before was executed, it means our code was in fact executed
        // and it did run the code which configures these Before/After methods.
        // If it isn't, it could be a bug in our code, but most likely is a bug in JUnit
        assertTrue(calledBefore);
    }

    @Test
    public void testInitialContextInjection() {
        assertNotNull(initialContext);
    }

    @Test
    public void testDefaultConfigInjection() {
        assertNotNull(contextConfig);
        String value = contextConfig.get("org.apache.openejb.junit.default-config");
        assertEquals("true", value);
    }
}
