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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EventTest {
    @Before
    public void reset() {
        SystemInstance.reset();
        final SystemInstance s = SystemInstance.get();
        s.addObserver(new AfterSimpleObserver());
        s.addObserver(new SimpleObserver());
        s.addObserver(new BlueObserver());
    }

    @Test
    public void simple() {
        final SystemInstance s = SystemInstance.get();
        final OrangeEvent orangeEvent = s.fireEvent(new OrangeEvent());

        assertEvent(orangeEvent.observed, SimpleObserver.orange);
    }

    @Test
    public void afterEvent() {
        final SystemInstance s = SystemInstance.get();

        assertEvent(s.fireEvent(new GreenEvent()).observed,
                SimpleObserver.green,
                AfterSimpleObserver.afterGreen
        );

        assertEvent(s.fireEvent(new SquareEvent()).observed,
                AfterSimpleObserver.afterSquare
        );
    }

    @Test
    public void observeBoth() {
        final SystemInstance s = SystemInstance.get();

        assertEvent(s.fireEvent(new BlueEvent()).observed,
                BlueObserver.blue,
                BlueObserver.afterBlue
        );

    }

    public static class ColorEvent {
        final List<String> observed = new ArrayList<String>();
    }

    public static class OrangeEvent extends ColorEvent {
    }

    public static class GreenEvent extends ColorEvent {
    }

    public static class BlueEvent extends ColorEvent {
    }

    public static class SquareEvent {
        final List<String> observed = new ArrayList<String>();
    }


    public static class ColorObserver {
        private static final String afterColor = "ColorObserver.afterColor";
        private static final String color = "ColorObserver.color";

        public void observe(@Observes ColorEvent event) {
            event.observed.add(color);
        }

        public void observe(@Observes AfterEvent<ColorEvent> event) {
            assertThat(event.getEvent(), instanceOf(ColorEvent.class));
            event.getEvent().observed.add(color);
        }
    }

    public static class AfterSimpleObserver {

        private static final String afterGreen = "AfterSimpleObserver.afterGreen";
        private static final String afterSquare = "AfterSimpleObserver.afterSquare";

        public void afterGreen(final @Observes AfterEvent<GreenEvent> event) {
            assertThat(event.getEvent(), instanceOf(GreenEvent.class));
            event.getEvent().observed.add(afterGreen);
        }

        public void afterSquare(final @Observes AfterEvent<SquareEvent> event) {
            assertThat(event.getEvent(), instanceOf(SquareEvent.class));
            event.getEvent().observed.add(afterSquare);
        }
    }

    public static class SimpleObserver {

        private static final String orange = "SimpleObserver.orange";
        private static final String green = "SimpleObserver.green";

        public void observe(final @Observes OrangeEvent event) {
            event.observed.add(orange);
        }

        public void observe(final @Observes GreenEvent event) {
            event.observed.add(green);
        }
    }

    public static class BlueObserver {

        private static final String blue = "BlueObserver.blue";
        private static final String afterBlue = "BlueObserver.afterBlue";

        public void observe(final @Observes BlueEvent event) {
            event.observed.add(blue);
        }

        public void observeAfter(final @Observes AfterEvent<BlueEvent> event) {
            event.getEvent().observed.add(afterBlue);
        }
    }


    private static void assertEvent(List<String> observed, String... expected) {
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