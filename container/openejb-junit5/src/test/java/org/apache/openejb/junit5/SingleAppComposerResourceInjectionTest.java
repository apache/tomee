/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.junit5;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.openejb.junit5.app.MyResourceApp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// -Dtomee.application-composer.application=org.apache.openejb.junit5.app.MyResourceApp
@RunWithApplicationComposer(mode = ExtensionMode.PER_JVM)
public class SingleAppComposerResourceInjectionTest {

    @Resource
    private MyResourceApp.MyResource myResource;

    @Resource(name = "myResource")
    private MyResourceApp.MyResource myResource2;

    @Resource(name = "java:comp/env/myResource")
    private MyResourceApp.MyResource myResource3;

    @Inject
    private MyResourceApp.MyService service;

    @Inject
    private MyResourceApp app;

    @Test
    public void testInject() {
        // In App..
        assertNotNull(app, "app must not be null");
        assertNotNull(app.getResource(), "app#myResource must not be null");
        assertEquals("value1", app.getResource().getAttr1(), "app#attr1 should equal 'value1'");
        assertEquals("value2", app.getResource().getAttr2(), "app#attr2 should equal 'value2'");

        // In Service.
        assertNotNull(service, "service must not be null");
        assertNotNull(service.getResource(), "service#myResource must not be null");
        assertEquals("value1", service.getResource().getAttr1(), "service#resource#attr1 should equal 'value1'");
        assertEquals("value2", service.getResource().getAttr2(), "service#resource#attr2 should equal 'value2'");

        // In Test -> TOMEE-4342
        assertNotNull(myResource, "'myResource' in test must not be null");
        assertEquals("value1", myResource.getAttr1(), "myResource#attr1 should equal 'value1'");
        assertEquals("value2", myResource.getAttr2(), "myResource#attr2 should equal 'value2'");

        assertNotNull(myResource2, "'myResource3' in test must not be null");
        assertEquals("value1", myResource3.getAttr1(), "myResource2#attr1 should equal 'value1'");
        assertEquals("value2", myResource3.getAttr2(), "myResource2#attr2 should equal 'value2'");

        assertNotNull(myResource3, "'myResource3' in test must not be null");
        assertEquals("value1", myResource3.getAttr1(), "myResource#attr1 should equal 'value1'");
        assertEquals("value2", myResource3.getAttr2(), "myResource#attr2 should equal 'value2'");
    }
}
