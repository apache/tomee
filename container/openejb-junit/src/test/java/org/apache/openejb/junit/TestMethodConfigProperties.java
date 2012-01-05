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

import org.apache.openejb.junit.ContextConfig;
import org.apache.openejb.junit.Property;
import org.apache.openejb.junit.TestResource;
import org.apache.openejb.junit.TestResourceTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

@RunWith(OpenEjbRunner.class)
public class TestMethodConfigProperties {
    @TestResource(TestResourceTypes.CONTEXT_CONFIG)
    private Hashtable<String, String> contextConfig;

    /**
     * We store it in a field to be sure that we check the same key in the 2 separate
     * tests.
     */
    private static final String CHECK_PROPERTY = "junit.test-property";

    public TestMethodConfigProperties() {
    }

    @Test
    @ContextConfig(
            properties = {
                    @Property("java.naming.factory.initial=org.apache.openejb.core.LocalInitialContextFactory"),
                    @Property(CHECK_PROPERTY + "=Test String from Properties")
            }
    )
    public void testConfig() {
        assertNotNull(contextConfig);

        checkProperty(CHECK_PROPERTY, "Test String from Properties");
    }

    @Test
    public void testConfigNotPresent() {
        assertNotNull(contextConfig);

        assertNull(contextConfig.get(CHECK_PROPERTY));
    }

    private void checkProperty(String key, String expected) {
        String value = contextConfig.get(key);
        assertEquals(expected, value);
    }
}
