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

/**
 * Here we test class level dual config, with method level overrides. We check some
 * properties in all tests (even when not in it's config), to ensure overrides work
 * properly, and also that context sharing doesn't happen.
 *
 */
@RunWith(OpenEjbRunner.class)
@ContextConfig(
        configFile = "/META-INF/test-config.properties",
        properties = {
                @Property("junit.test-property-override=New Class Property Value")
        }
)
public class TestDualConfigOverride {
    @TestResource(TestResourceTypes.CONTEXT_CONFIG)
    private Hashtable<String, String> contextConfig;

    public TestDualConfigOverride() {
    }

    @Test
    public void testClassConfig() {
        assertNotNull(contextConfig);

        checkProperty("junit.test-property", "Test String");
        checkProperty("junit.test-property-override", "New Class Property Value");
        checkProperty("junit.test-property-override2", "Original Value 2");
        checkProperty("junit.test-property-override3", "Original Value 3");
        assertNull(contextConfig.get("junit.test-new-method-file-property"));
        assertNull(contextConfig.get("junit.test-new-method-property"));
    }

    @Test
    @ContextConfig(
            configFile = "/META-INF/test-config-method.properties"
    )
    public void testMethodFileOverride() {
        assertNotNull(contextConfig);

        checkProperty("junit.test-property", "Test String");
        checkProperty("junit.test-property-override", "New Class Property Value");
        checkProperty("junit.test-property-override2", "New Method File Value 2");
        checkProperty("junit.test-property-override3", "Original Value 3");
        checkProperty("junit.test-new-method-file-property", "New Method Value");
        assertNull(contextConfig.get("junit.test-new-method-property"));
    }

    @Test
    @ContextConfig(
            properties = {
                    @Property("junit.test-property-override3=New Method Property Value 3"),
                    @Property("junit.test-new-method-property=New Method Property Value")
            }
    )
    public void testMethodPropertiesOverride() {
        assertNotNull(contextConfig);

        checkProperty("junit.test-property", "Test String");
        checkProperty("junit.test-property-override", "New Class Property Value");
        checkProperty("junit.test-property-override2", "Original Value 2");
        checkProperty("junit.test-property-override3", "New Method Property Value 3");
        checkProperty("junit.test-new-method-property", "New Method Property Value");
        assertNull(contextConfig.get("junit.test-new-method-file-property"));
    }

    @Test
    @ContextConfig(
            configFile = "/META-INF/test-config-method.properties",
            properties = {
                    @Property("junit.test-property-override2=New Method Property Value 2"),
                    @Property("junit.test-new-method-property=New Method Property Value")
            }
    )
    public void testMethodDualOverride() {
        assertNotNull(contextConfig);

        checkProperty("junit.test-property", "Test String");
        checkProperty("junit.test-property-override", "New Class Property Value");
        checkProperty("junit.test-property-override2", "New Method Property Value 2");
        checkProperty("junit.test-property-override3", "Original Value 3");
        checkProperty("junit.test-new-method-file-property", "New Method Value");
        checkProperty("junit.test-new-method-property", "New Method Property Value");
    }

    private void checkProperty(String key, String expected) {
        String value = contextConfig.get(key);
        assertEquals(expected, value);
    }
}
