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
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.LinkedList;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Classes
@SimpleLog
@ContainerProperties({
        @ContainerProperties.Property(name = "postConstructAndPreDestroy", value = "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithBoth"),
        @ContainerProperties.Property(name = "postConstructAndPreDestroy.myAttr", value = "done"),
        @ContainerProperties.Property(name = "postConstructOnly", value = "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithPostConstruct"),
        @ContainerProperties.Property(name = "postConstructOnly.myAttr", value = "done"),
        @ContainerProperties.Property(name = "postConstructAnnotationAndConfigOnly", value = "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithPostConstruct&post-construct=init2"),
        @ContainerProperties.Property(name = "postConstructAnnotationAndConfigOnly.myAttr", value = "done"),
        @ContainerProperties.Property(name = "preDestroyOnly", value = "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithPreDestroy"),
        @ContainerProperties.Property(name = "preDestroyOnly.myAttr", value = "done"),
        @ContainerProperties.Property(name = "preDestroyAnnotationAndConfigOnly", value = "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithPreDestroy&pre-destroy=init2"),
        @ContainerProperties.Property(name = "preDestroyAnnotationAndConfigOnly.myAttr", value = "done"),
})
@RunWith(ApplicationComposer.class)
public class ResourceLifecycleTest {
    @Resource(name = "postConstructAndPreDestroy")
    private WithBoth both;

    @Resource(name = "postConstructOnly")
    private WithPostConstruct postConstruct;

    @Resource(name = "postConstructAnnotationAndConfigOnly")
    private WithPostConstruct postConstruct2;

    @Resource(name = "preDestroyOnly")
    private WithPreDestroy preDestroy;

    @Resource(name = "preDestroyAnnotationAndConfigOnly")
    private WithPreDestroy preDestroy2;

    private static final Collection<Runnable> POST_CONTAINER_VALIDATIONS = new LinkedList<>();

    @AfterClass
    public static void lastValidations() { // late to make the test failing (ie junit report will be broken) but better than destroying eagerly the resource
        for (final Runnable runnable : POST_CONTAINER_VALIDATIONS) {
            runnable.run();
        }
        POST_CONTAINER_VALIDATIONS.clear();
    }

    @Test
    public void postConstructOnly() {
        assertNotNull(postConstruct);
        assertTrue(postConstruct.isInit());
        assertFalse(postConstruct.isInit2());
        assertEquals("done", postConstruct.myAttr);
    }

    @Test
    public void postConstructAnnotationAndConfigOnly() {
        assertNotNull(postConstruct2);
        assertTrue(postConstruct2.isInit());
        assertTrue(postConstruct2.isInit2());
        assertEquals("done", postConstruct2.myAttr);
    }

    @Test
    public void preDestroyOnly() {
        assertNotNull(preDestroy);
        assertFalse(preDestroy.isDestroy());
        assertFalse(preDestroy.isDestroy2());
        assertEquals("done", preDestroy.myAttr);
        POST_CONTAINER_VALIDATIONS.add(new Runnable() {
            @Override
            public void run() {
                assertTrue(preDestroy.isDestroy());
            }
        });
    }

    @Test
    public void preDestroyAnnotationAndConfigOnly() {
        assertNotNull(preDestroy2);
        assertFalse(preDestroy2.isDestroy());
        assertFalse(preDestroy2.isDestroy2());
        assertEquals("done", preDestroy2.myAttr);
        POST_CONTAINER_VALIDATIONS.add(new Runnable() {
            @Override
            public void run() {
                assertTrue(preDestroy2.isDestroy());
                assertTrue(preDestroy2.isDestroy2());
            }
        });
    }

    @Test
    public void postConstructAndPreDestroy() {
        assertNotNull(both);
        assertTrue(both.isInit());
        assertFalse(both.isDestroy());
        assertEquals("done", both.myAttr);
        POST_CONTAINER_VALIDATIONS.add(new Runnable() {
            @Override
            public void run() {
                assertTrue(both.isDestroy());
            }
        });
    }

    public static class TrivialConfigToCheckWarnings {
        protected String myAttr;
    }

    public static class WithBoth extends TrivialConfigToCheckWarnings {
        private boolean init;
        private boolean destroy;

        @PostConstruct
        private void init() {
            init = true;
        }

        @PreDestroy
        private void init2() {
            destroy = true;
        }

        public boolean isInit() {
            return init;
        }

        public boolean isDestroy() {
            return destroy;
        }
    }

    public static class WithPostConstruct extends TrivialConfigToCheckWarnings {
        private boolean init;
        private boolean init2;

        @PostConstruct
        private void init() {
            init = true;
        }

        private void init2() {
            init2 = true;
        }

        public boolean isInit() {
            return init;
        }

        public boolean isInit2() {
            return init2;
        }
    }

    public static class WithPreDestroy extends TrivialConfigToCheckWarnings {
        private boolean destroy;
        private boolean destroy2;

        @PreDestroy
        private void init() {
            destroy = true;
        }

        private void init2() {
            destroy2 = true;
        }

        public boolean isDestroy() {
            return destroy;
        }

        public boolean isDestroy2() {
            return destroy2;
        }
    }
}
