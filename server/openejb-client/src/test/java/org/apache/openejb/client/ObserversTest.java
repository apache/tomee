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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import org.apache.openejb.client.event.Observes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ObserversTest extends Assert {

    private final Observers observers = new Observers();
    private final List<String> observed = new ArrayList<String>();

    @Before
    public void setup() {
        observers.addObserver(new BasicObserver());
    }

    @Test
    public void canObserveObject() throws Exception {
        observers.fireEvent("");
        assertEvent(BasicObserver.object);
    }

    @Test
    public void objectNotInvoked() throws Exception {
        observers.fireEvent(new Color());
        assertEvent(BasicObserver.color);
    }

    @Test
    public void colorStillInvoked() throws Exception {
        observers.fireEvent(new Green());
        assertEvent(BasicObserver.color);
    }

    @Test
    public void observeEmerald() throws Exception {
        observers.fireEvent(new Emerald());
        assertEvent(BasicObserver.emerald);
    }


    public static class Color {
    }

    public static class Green extends Color {
    }

    public static class Emerald extends Green {
    }


    public class BasicObserver {

        private static final String color = "color";
        private static final String object = "object";
        private static final String emerald = "emerald";

        public void observe(@Observes Object event) {
            observed.add(object);
        }

        public void observe(@Observes Color event) {
            observed.add(color);
        }

        public void observe(@Observes Emerald event) {
            observed.add(emerald);
        }
    }


    private void assertEvent(String... expected) {
        assertEquals(join(expected), join(observed));
    }

    public static String join(final Object... collection) {
        return join(Arrays.asList(collection));
    }

    public static String join(final Collection<?> collection) {
        final String delimiter = "\n";
        if (collection.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

}
