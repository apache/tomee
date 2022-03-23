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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Classes
@SimpleLog
@RunWith(ApplicationComposer.class)
@ContainerProperties({ // defined in reverse order to ensure we sort it
        @ContainerProperties.Property(name = "b", value = "new://Resource?class-name=org.apache.openejb.resource.DependsOnResourceTest$B&depends-on=a"),
        @ContainerProperties.Property(name = "b.a", value = "@a"),
        @ContainerProperties.Property(name = "a", value = "new://Resource?class-name=org.apache.openejb.resource.DependsOnResourceTest$A"),
        @ContainerProperties.Property(name = "a.testValue", value = "DependsOnResourceTest")
})
public class DependsOnResourceTest {
    @Resource
    private B b;

    @Test
    public void run() {
        assertNotNull(b);
        assertNotNull(b.a);
        assertNotNull(b.a.testValue);
        assertEquals("DependsOnResourceTest", b.a.testValue);
    }

    public static class A {
        private String testValue;
    }
    public static class B {
        private A a;
    }
}
