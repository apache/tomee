/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jul.handler.rotating;

import java.util.concurrent.TimeUnit;

class Duration {
    private long time;
    private TimeUnit unit = TimeUnit.MILLISECONDS;

    private Duration() {
        // no-op
    }

    private Duration(final long time, final TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    Duration(final String string) {
        this(string, null);
    }

    private Duration(final String string, final TimeUnit defaultUnit) {
        final String[] strings = string.split(",| and ");

        Duration total = new Duration();

        for (final String value : strings) {
            final Duration part = new Duration();
            final String s = value.trim();

            final StringBuilder t = new StringBuilder();
            final StringBuilder u = new StringBuilder();

            int i = 0;

            // get the number
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (Character.isDigit(c) || i == 0 && c == '-') {
                    t.append(c);
                } else {
                    break;
                }
            }

            if (t.length() == 0) {
                invalidFormat(s);
            }

            // skip whitespace
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (!Character.isWhitespace(c)) {
                    break;
                }
            }

            // get time unit text part
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (Character.isLetter(c)) {
                    u.append(c);
                } else {
                    invalidFormat(s);
                }
            }

            part.time = Long.parseLong(t.toString());

            part.unit = parseUnit(u.toString());

            if (part.unit == null) {
                part.unit = defaultUnit;
            }

            total = total.add(part);
        }

        this.time = total.time;
        this.unit = total.unit;
    }

    public long asMillis() {
        return unit.toMillis(time);
    }

    private static final class Normalize {
        private final long a;
        private final long b;
        private final TimeUnit base;

        private Normalize(final Duration a, final Duration b) {
            this.base = lowest(a, b);
            this.a = a.unit == null ? a.time : base.convert(a.time, a.unit);
            this.b = b.unit == null ? b.time : base.convert(b.time, b.unit);
        }

        private static TimeUnit lowest(final Duration a, final Duration b) {
            if (a.unit == null) {
                return b.unit;
            }
            if (b.unit == null) {
                return a.unit;
            }
            if (a.time == 0) {
                return b.unit;
            }
            if (b.time == 0) {
                return a.unit;
            }
            return TimeUnit.values()[Math.min(a.unit.ordinal(), b.unit.ordinal())];
        }
    }

    public Duration add(final Duration that) {
        final Normalize n = new Normalize(this, that);
        return new Duration(n.a + n.b, n.base);
    }

    private static void invalidFormat(final String text) {
        throw new IllegalArgumentException("Illegal duration format: '" + text +
                "'.  Valid examples are '10s' or '10 seconds'.");
    }

    private static TimeUnit parseUnit(final String u) {
        if (u.length() == 0) {
            return null;
        }

        if ("NANOSECONDS".equalsIgnoreCase(u)) {
            return TimeUnit.NANOSECONDS;
        }
        if ("NANOSECOND".equalsIgnoreCase(u)) {
            return TimeUnit.NANOSECONDS;
        }
        if ("NANOS".equalsIgnoreCase(u)) {
            return TimeUnit.NANOSECONDS;
        }
        if ("NANO".equalsIgnoreCase(u)) {
            return TimeUnit.NANOSECONDS;
        }
        if ("NS".equalsIgnoreCase(u)) {
            return TimeUnit.NANOSECONDS;
        }

        if ("MICROSECONDS".equalsIgnoreCase(u)) {
            return TimeUnit.MICROSECONDS;
        }
        if ("MICROSECOND".equalsIgnoreCase(u)) {
            return TimeUnit.MICROSECONDS;
        }
        if ("MICROS".equalsIgnoreCase(u)) {
            return TimeUnit.MICROSECONDS;
        }
        if ("MICRO".equalsIgnoreCase(u)) {
            return TimeUnit.MICROSECONDS;
        }

        if ("MILLISECONDS".equalsIgnoreCase(u)) {
            return TimeUnit.MILLISECONDS;
        }
        if ("MILLISECOND".equalsIgnoreCase(u)) {
            return TimeUnit.MILLISECONDS;
        }
        if ("MILLIS".equalsIgnoreCase(u)) {
            return TimeUnit.MILLISECONDS;
        }
        if ("MILLI".equalsIgnoreCase(u)) {
            return TimeUnit.MILLISECONDS;
        }
        if ("MS".equalsIgnoreCase(u)) {
            return TimeUnit.MILLISECONDS;
        }

        if ("SECONDS".equalsIgnoreCase(u)) {
            return TimeUnit.SECONDS;
        }
        if ("SECOND".equalsIgnoreCase(u)) {
            return TimeUnit.SECONDS;
        }
        if ("SEC".equalsIgnoreCase(u)) {
            return TimeUnit.SECONDS;
        }
        if ("S".equalsIgnoreCase(u)) {
            return TimeUnit.SECONDS;
        }

        if ("MINUTES".equalsIgnoreCase(u)) {
            return TimeUnit.MINUTES;
        }
        if ("MINUTE".equalsIgnoreCase(u)) {
            return TimeUnit.MINUTES;
        }
        if ("MIN".equalsIgnoreCase(u)) {
            return TimeUnit.MINUTES;
        }
        if ("M".equalsIgnoreCase(u)) {
            return TimeUnit.MINUTES;
        }

        if ("HOURS".equalsIgnoreCase(u)) {
            return TimeUnit.HOURS;
        }
        if ("HOUR".equalsIgnoreCase(u)) {
            return TimeUnit.HOURS;
        }
        if ("HRS".equalsIgnoreCase(u)) {
            return TimeUnit.HOURS;
        }
        if ("HR".equalsIgnoreCase(u)) {
            return TimeUnit.HOURS;
        }
        if ("H".equalsIgnoreCase(u)) {
            return TimeUnit.HOURS;
        }

        if ("DAYS".equalsIgnoreCase(u)) {
            return TimeUnit.DAYS;
        }
        if ("DAY".equalsIgnoreCase(u)) {
            return TimeUnit.DAYS;
        }
        if ("D".equalsIgnoreCase(u)) {
            return TimeUnit.DAYS;
        }

        throw new IllegalArgumentException("Unknown time unit '" + u + "'");
    }
}

