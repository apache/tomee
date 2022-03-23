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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class PropertiesInjectionTest {
    @Configuration
    public Properties p() {
        return new PropertiesBuilder()
            .p("p", "new://Resource?class-name=" + PropertiesFactory.class.getName() + "&factory-name=create")
            .p("p.attr1", "v1")
            .p("p.attr2", "v2")
            .build();
    }

    @Module
    public EjbJar jar() {
        return new EjbJar();
    }

    @Resource(name = "p")
    private Properties p;

    @Test
    public void test() {
        assertEquals("v1", p.getProperty("attr1"));
        assertEquals("v2", p.getProperty("attr2"));
        assertEquals(2, p.size());
    }
}
