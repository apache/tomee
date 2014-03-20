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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class EventTest {
    @After
    @Before
    public void reset() {
        SystemInstance.reset();
    }

    private static final AtomicInteger ID = new AtomicInteger(0);

    @Test
    public void order() {
        final SystemInstance s = SystemInstance.get();
        assertEquals(-1, SimpleObserver.id);
        assertEquals(-1, OrderedSimpleObserver.id);
        s.addObserver(new OrderedSimpleObserver());
        s.addObserver(new SimpleObserver());
        s.fireEvent(new SimpleEvent());
        assertEquals(1, SimpleObserver.id);
        assertEquals(2, OrderedSimpleObserver.id);
    }

    public static class SimpleEvent {}

    public static class SimpleObserver {
        private static int id = -1;

        public void observe(final @Observes SimpleEvent event) {
            id = ID.incrementAndGet();
        }
    }

    public static class OrderedSimpleObserver {
        private static int id = -1;

        public void observe(final @Observes(after = SimpleObserver.class) SimpleEvent event) {
            id = ID.incrementAndGet();
        }
    }
}
