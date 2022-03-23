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
package org.apache.openejb.resource;

import org.apache.openejb.api.resource.PropertiesResourceProvider;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class DefaultPropertiesProviderTest {
    @Resource(name = "r1")
    private MyResource r1;

    @Resource(name = "_r2")
    private MyResource r2;

    @Resource(name = "_r3")
    private MyResource r3;

    @Test
    public void valid() {
        assertEquals("r1", r1.value);
        assertEquals("2", r2.value);
        assertEquals("3", r3.value);
    }

    @Module
    public WebApp bean() {
        return new WebApp();
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put(PropertiesResourceProvider.class.getName(), GlobalPropertiesProvider.class.getName());

        p.put("r1", "new://Resource?" +
                "class-name=org.apache.openejb.resource.DefaultPropertiesProviderTest$MyResource");
        p.put("r1.value", "_____________________");

        p.put("_r2", "new://Resource?" +
                        "class-name=org.apache.openejb.resource.DefaultPropertiesProviderTest$MyResource"
                        + "&properties-provider=org.apache.openejb.resource.DefaultPropertiesProviderTest$SpecificPropertiesProvider");
        p.put("_r2.value", "____________________");

        p.put("_r3", "new://Resource?" +
                        "class-name=org.apache.openejb.resource.DefaultPropertiesProviderTest$MyResource");
        p.put("_r3.value", "3");

        return p;
    }

    public static class MyResource {
        private String value;
    }

    public static class GlobalPropertiesProvider {
        private Properties p;
        private String serviceId;

        public void setProperties(final Properties p) {
            this.p = p;
        }

        public Properties provides() {
            if (serviceId.startsWith("_r")) {
                return new Properties();
            }
            return new Properties() {{
                putAll(p);
                setProperty("value", serviceId);
            }};
        }
    }

    public static class SpecificPropertiesProvider {
        public Properties provides() {
            return new Properties() {{
                setProperty("value", "2"); // override
            }};
        }
    }
}
