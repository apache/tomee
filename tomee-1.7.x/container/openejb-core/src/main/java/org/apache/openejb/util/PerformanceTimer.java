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

package org.apache.openejb.util;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class PerformanceTimer {

    protected Start start;
    private Event event;

    public PerformanceTimer() {
        event = start = new Start();
    }

    public void event(final String event) {
        this.event = new Event(this.event, event);
    }

    public void stop(final PrintStream out) {
        final Event event = new Event(this.event, "stop");
        this.event.stop(event, out);
        start.stop(event, out);
    }

    private class Event {

        protected final long start = System.nanoTime();
        private final Event previous;
        private final String description;

        Event(final Event previous, final String description) {
            this.previous = previous;
            this.description = description;
        }

        public void stop(final Event next, final PrintStream out) {
            if (previous != PerformanceTimer.this.start) {
                previous.stop(this, out);
            }
            out.printf("%s  %s", TimeUnit.NANOSECONDS.toMillis(next.start - this.start), this.description);
            out.println();
        }
    }

    private final class Start extends Event {

        private Start() {
            super(null, "start");
        }

        @Override
        public void stop(final Event next, final PrintStream out) {
            out.printf("%s  %s", TimeUnit.NANOSECONDS.toMillis(next.start - this.start), "total");
            out.println();
        }
    }
}
