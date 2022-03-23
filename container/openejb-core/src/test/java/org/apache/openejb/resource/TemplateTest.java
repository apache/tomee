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

import org.apache.openejb.api.resource.Template;
import org.apache.openejb.config.sys.AbstractService;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@Classes
@RunWith(ApplicationComposer.class)
public class TemplateTest {
    @Resource(name = "r1")
    private MyResource r1;

    @Test
    public void validAliases() {
        assertEquals("r1", r1.value);
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("r1", "new://Resource?template=org.apache.openejb.resource.TemplateTest$MyTemplate");
        return p;
    }

    public static class MyResource {
        private String value;
    }

    public static class MyTemplate implements Template<AbstractService> {
        private String serviceId;

        @Override
        public void configure(final AbstractService resource) {
            resource.setClassName(MyResource.class.getName());
            resource.getProperties().put("value", serviceId);
        }
    }
}
