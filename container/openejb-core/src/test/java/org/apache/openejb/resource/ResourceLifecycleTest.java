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

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SimpleLog
@RunWith(ApplicationComposer.class)
public class ResourceLifecycleTest {


    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder()
                .p("postConstructAndPreDestroy", "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithBoth")
                .p("postConstructAndPreDestroy.myAttr", "done")
                .p("postConstructOnly", "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithPostConstruct")
                .p("postConstructOnly.myAttr", "done")
                .p("postConstructAnnotationAndConfigOnly", "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithPostConstruct&post-construct=init2")
                .p("postConstructAnnotationAndConfigOnly.myAttr", "done")
                .p("preDestroyOnly", "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithPreDestroy")
                .p("preDestroyOnly.myAttr", "done")
                .p("preDestroyAnnotationAndConfigOnly", "new://Resource?class-name=org.apache.openejb.resource.ResourceLifecycleTest$WithPreDestroy&pre-destroy=init2")
                .p("preDestroyAnnotationAndConfigOnly.myAttr", "done")
                .build();
    }

    @Module
    public WebApp webApp() {
        return new WebApp();
    }

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

    private static final Collection<Runnable> POST_CONTAINER_VALIDATIONS = new LinkedList<Runnable>();

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
