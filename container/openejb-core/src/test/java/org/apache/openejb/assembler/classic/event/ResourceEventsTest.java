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
package org.apache.openejb.assembler.classic.event;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Classes
@SimpleLog
@ContainerProperties({
        @ContainerProperties.Property(name = "test", value = "new://Service?class-name=org.apache.openejb.assembler.classic.event.ResourceEventsTest$Listener"),
        @ContainerProperties.Property(name = "base", value = "new://Resource?class-name=org.apache.openejb.assembler.classic.event.ResourceEventsTest$Base"),
        @ContainerProperties.Property(name = "base2", value = "new://Resource?class-name=org.apache.openejb.assembler.classic.event.ResourceEventsTest$Base"),
})
public class ResourceEventsTest {
    @Resource(name = "base")
    private Base base;

    @Resource(name = "base2")
    private Base base2;

    @Test
    public void run() throws Exception {
        Listener.EVENTS.clear();
        new ApplicationComposers(this).evaluate(this, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                assertEquals(2, Listener.EVENTS.size());
                for (final ResourceEvent re : Listener.EVENTS) {
                    assertTrue(ResourceCreated.class.isInstance(re));
                    assertNotNull(re.getResource());
                    assertTrue("base".equals(re.getName()) || "base2".equals(re.getName()));
                }
                Listener.EVENTS.clear();
                assertTrue(Child.class.isInstance(base));
                assertFalse(Child.class.isInstance(base2));
                assertNotNull(Child.class.cast(base).parent);
                return null;
            }
        });
        assertEquals(2, Listener.EVENTS.size());
        for (final ResourceEvent re : Listener.EVENTS) {
            assertTrue(ResourceBeforeDestroyed.class.isInstance(re));

            Object resource = re.getResource();
            if (Assembler.ResourceInstance.class.isInstance(resource)) {
                resource = Assembler.ResourceInstance.class.cast(resource).getObject();
            }

            assertNotNull(resource);
            assertTrue("base".equals(re.getName()) || "base2".equals(re.getName()));
            if ("base".equals(re.getName())) {
                assertTrue(Child.class.isInstance(resource));
            } else {
                assertFalse(Child.class.isInstance(resource));
                assertTrue(Base.class.isInstance(resource));
            }
        }
        Listener.EVENTS.clear();
        assertTrue(base2.stopped);
        assertTrue(base.stopped);
        assertTrue(Base.class.cast(Child.class.cast(base).parent).stopped);
    }

    public static class Base {
        private boolean stopped = false;

        @PreDestroy
        public void stop() {
            stopped = true;
        }
    }

    public static class Child extends Base {
        private final Object parent;

        Child(final Object resource) {
            parent = resource;
        }
    }

    public static class Listener {
        private static final List<ResourceEvent> EVENTS = new ArrayList<>();

        public void onCreated(@Observes final ResourceCreated created) {
            EVENTS.add(created);
            if (created.is(Base.class) && "base".equals(created.getName())) {
                created.replaceBy(new Child(created.getResource()));
            }
        }

        public void onDestroyed(@Observes final ResourceBeforeDestroyed destroyed) {
            EVENTS.add(destroyed);
            if (destroyed.is(Base.class) && destroyed.is(Child.class) && "base".equals(destroyed.getName())) {
                final Object parent = Child.class.cast(destroyed.getResource()).parent;
                try {
                    destroyed.replaceBy(new Assembler.ResourceInstance("base", parent, singleton(Base.class.getMethod("stop")), null));
                } catch (final NoSuchMethodException e) {
                    fail(e.getMessage());
                }
            }
        }
    }
}
