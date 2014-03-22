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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.loader;

import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.AfterEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class EventTest {
    @After
    @Before
    public void reset() {
        SystemInstance.reset();
        SimpleObserver.id = -1;
    }

    @Test
    public void simple() {
        final SystemInstance s = SystemInstance.get();
        assertEquals(-1, SimpleObserver.id);
        s.addObserver(new SimpleObserver());
        s.fireEvent(new SimpleEvent());
        assertEquals(1, SimpleObserver.id);
    }

    @Test
    public void afterEvent() {
        final SystemInstance s = SystemInstance.get();
        assertEquals(-1, SimpleObserver.id);
        s.addObserver(new SimpleObserver());
        s.addObserver(new AfterSimpleObserver());
        final SimpleEvent event = new SimpleEvent();
        s.fireEvent(event);
        assertEquals(1, SimpleObserver.id);
        assertNotNull(AfterSimpleObserver.event);
        assertEquals(event, AfterSimpleObserver.event.getEvent());
    }

    public static class SimpleEvent {}

    public static class AfterSimpleObserver {
        private static AfterEvent<SimpleEvent> event;

        public void observe(final @Observes AfterEvent<SimpleEvent> event) {
            AfterSimpleObserver.event = event;
            assertThat(event.getEvent(), instanceOf(SimpleEvent.class));
        }
    }

    public static class SimpleObserver {
        private static int id = -1;

        public void observe(final @Observes SimpleEvent event) {
            id = 1;
        }
    }
}
