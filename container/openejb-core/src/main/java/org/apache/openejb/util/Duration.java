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
package org.apache.openejb.util;

import java.beans.PropertyEditorManager;
import java.util.concurrent.TimeUnit;

public class Duration {

    private long time;
    private TimeUnit unit;

    public Duration() {
    }

    public Duration(long time, TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    public Duration(String string) {
        String[] strings = string.split(",| and ");

        Duration total = new Duration();

        for (String s : strings) {
            Duration part = new Duration();
            s = s.trim();

            StringBuilder t = new StringBuilder();
            StringBuilder u = new StringBuilder();

            int i = 0;

            // get the number
            for (; i < s.length(); i++) {
                char c = s.charAt(i);
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
                char c = s.charAt(i);
                if (Character.isWhitespace(c)) {
                } else {
                    break;
                }
            }

            // get time unit text part
            for (; i < s.length(); i++) {
                char c = s.charAt(i);
                if (Character.isLetter(c)) {
                    u.append(c);
                } else {
                    invalidFormat(s);
                }
            }

            part.time = Integer.parseInt(t.toString());

            Unit unit1 = parseUnit(u.toString());

            if (unit1 != null) switch (unit1) {
                case days: {
                    part.time *= 60 * 60 * 24;
                    part.unit = TimeUnit.SECONDS;
                }
                ;
                break;
                case hours: {
                    part.time *= 60 * 60;
                    part.unit = TimeUnit.SECONDS;
                }
                ;
                break;
                case minutes: {
                    part.time *= 60;
                    part.unit = TimeUnit.SECONDS;
                }
                ;
                break;
                case seconds: {
                    part.unit = TimeUnit.SECONDS;
                }
                ;
                break;
                case milliseconds: {
                    part.unit = TimeUnit.MILLISECONDS;
                }
                ;
                break;
                case microseconds: {
                    part.unit = TimeUnit.MICROSECONDS;
                }
                ;
                break;
                case nanoseconds: {
                    part.unit = TimeUnit.NANOSECONDS;
                }
                ;
                break;
            }
            total = total.add(part);
        }

        this.time = total.time;
        this.unit = total.unit;
    }

    public long getTime() {
        return time;
    }

    public long getTime(TimeUnit unit) {
        return unit.convert(this.time, this.unit);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        final Duration duration = (Duration) o;
//
//        if (time != duration.time) return false;
//        if (unit != duration.unit) return false;
//
//        return true;
//    }

    //
    private static class Normalize {
        private long a;
        private long b;
        private TimeUnit base;

        private Normalize(Duration a, Duration b) {
            this.base = lowest(a, b);
            this.a = a.unit == null ? a.time : base.convert(a.time, a.unit);
            this.b = b.unit == null ? b.time : base.convert(b.time, b.unit);
        }

        private static TimeUnit lowest(Duration a, Duration b) {
            if (a.unit == null) return b.unit;
            if (b.unit == null) return a.unit;
            if (a.time == 0) return b.unit;
            if (b.time == 0) return a.unit;
            return TimeUnit.values()[Math.min(a.unit.ordinal(), b.unit.ordinal())];
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Duration that = (Duration) o;

        Normalize n = new Normalize(this, that);
        return n.a == n.b;
    }

    public Duration add(Duration that) {
        Normalize n = new Normalize(this, that);
        return new Duration(n.a + n.b, n.base);
    }

    public Duration subtract(Duration that) {
        Normalize n = new Normalize(this, that);
        return new Duration(n.a - n.b, n.base);
    }

    public static Duration parse(String text) {
        return new Duration(text);
    }

    private static void invalidFormat(String text) {
        throw new IllegalArgumentException("Illegal duration format: '" + text + "'.  Valid examples are '10s' or '10 seconds'.");
    }

    private static Unit parseUnit(String u) {
        if (u.length() == 0) return null;

        if (u.equalsIgnoreCase("NANOSECONDS")) return Unit.nanoseconds;
        if (u.equalsIgnoreCase("NANOSECOND")) return Unit.nanoseconds;
        if (u.equalsIgnoreCase("NANOS")) return Unit.nanoseconds;
        if (u.equalsIgnoreCase("NANO")) return Unit.nanoseconds;
        if (u.equalsIgnoreCase("NS")) return Unit.nanoseconds;

        if (u.equalsIgnoreCase("MICROSECONDS")) return Unit.microseconds;
        if (u.equalsIgnoreCase("MICROSECOND")) return Unit.microseconds;
        if (u.equalsIgnoreCase("MICROS")) return Unit.microseconds;
        if (u.equalsIgnoreCase("MICRO")) return Unit.microseconds;

        if (u.equalsIgnoreCase("MILLISECONDS")) return Unit.milliseconds;
        if (u.equalsIgnoreCase("MILLISECOND")) return Unit.milliseconds;
        if (u.equalsIgnoreCase("MILLIS")) return Unit.milliseconds;
        if (u.equalsIgnoreCase("MILLI")) return Unit.milliseconds;
        if (u.equalsIgnoreCase("MS")) return Unit.milliseconds;

        if (u.equalsIgnoreCase("SECONDS")) return Unit.seconds;
        if (u.equalsIgnoreCase("SECOND")) return Unit.seconds;
        if (u.equalsIgnoreCase("SEC")) return Unit.seconds;
        if (u.equalsIgnoreCase("S")) return Unit.seconds;

        if (u.equalsIgnoreCase("MINUTES")) return Unit.minutes;
        if (u.equalsIgnoreCase("MINUTE")) return Unit.minutes;
        if (u.equalsIgnoreCase("MIN")) return Unit.minutes;
        if (u.equalsIgnoreCase("M")) return Unit.minutes;

        if (u.equalsIgnoreCase("HOURS")) return Unit.hours;
        if (u.equalsIgnoreCase("HOUR")) return Unit.hours;
        if (u.equalsIgnoreCase("HRS")) return Unit.hours;
        if (u.equalsIgnoreCase("HR")) return Unit.hours;
        if (u.equalsIgnoreCase("H")) return Unit.hours;

        if (u.equalsIgnoreCase("DAYS")) return Unit.days;
        if (u.equalsIgnoreCase("DAY")) return Unit.days;
        if (u.equalsIgnoreCase("D")) return Unit.days;

        throw new IllegalArgumentException("Unknown time unit '" + u + "'.  Supported units " + Join.join(", ", Unit.values()));
    }


    static {
        PropertyEditorManager.registerEditor(Duration.class, DurationEditor.class);
    }

    public static enum Unit {
        // All lowercase so they look good displayed in help
        nanoseconds, microseconds, milliseconds, seconds, minutes, hours, days;
    }
}
