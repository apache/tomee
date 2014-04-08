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

import java.beans.PropertyEditorManager;
import java.util.concurrent.TimeUnit;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

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

            part.unit = parseUnit(u.toString());

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(time);
        if (unit != null) {
            sb.append(" ");
            sb.append(unit);
        }
        return sb.toString();
    }

    private static TimeUnit parseUnit(String u) {
        if (u.length() == 0) return null;

        if (u.equalsIgnoreCase("NANOSECONDS")) return TimeUnit.NANOSECONDS;
        if (u.equalsIgnoreCase("NANOSECOND")) return TimeUnit.NANOSECONDS;
        if (u.equalsIgnoreCase("NANOS")) return TimeUnit.NANOSECONDS;
        if (u.equalsIgnoreCase("NANO")) return TimeUnit.NANOSECONDS;
        if (u.equalsIgnoreCase("NS")) return TimeUnit.NANOSECONDS;

        if (u.equalsIgnoreCase("MICROSECONDS")) return TimeUnit.MICROSECONDS;
        if (u.equalsIgnoreCase("MICROSECOND")) return TimeUnit.MICROSECONDS;
        if (u.equalsIgnoreCase("MICROS")) return TimeUnit.MICROSECONDS;
        if (u.equalsIgnoreCase("MICRO")) return TimeUnit.MICROSECONDS;

        if (u.equalsIgnoreCase("MILLISECONDS")) return TimeUnit.MILLISECONDS;
        if (u.equalsIgnoreCase("MILLISECOND")) return TimeUnit.MILLISECONDS;
        if (u.equalsIgnoreCase("MILLIS")) return TimeUnit.MILLISECONDS;
        if (u.equalsIgnoreCase("MILLI")) return TimeUnit.MILLISECONDS;
        if (u.equalsIgnoreCase("MS")) return TimeUnit.MILLISECONDS;

        if (u.equalsIgnoreCase("SECONDS")) return TimeUnit.SECONDS;
        if (u.equalsIgnoreCase("SECOND")) return TimeUnit.SECONDS;
        if (u.equalsIgnoreCase("SEC")) return TimeUnit.SECONDS;
        if (u.equalsIgnoreCase("S")) return TimeUnit.SECONDS;

        if (u.equalsIgnoreCase("MINUTES")) return TimeUnit.MINUTES;
        if (u.equalsIgnoreCase("MINUTE")) return TimeUnit.MINUTES;
        if (u.equalsIgnoreCase("MIN")) return TimeUnit.MINUTES;
        if (u.equalsIgnoreCase("M")) return TimeUnit.MINUTES;

        if (u.equalsIgnoreCase("HOURS")) return TimeUnit.HOURS;
        if (u.equalsIgnoreCase("HOUR")) return TimeUnit.HOURS;
        if (u.equalsIgnoreCase("HRS")) return TimeUnit.HOURS;
        if (u.equalsIgnoreCase("HR")) return TimeUnit.HOURS;
        if (u.equalsIgnoreCase("H")) return TimeUnit.HOURS;

        if (u.equalsIgnoreCase("DAYS")) return TimeUnit.DAYS;
        if (u.equalsIgnoreCase("DAY")) return TimeUnit.DAYS;
        if (u.equalsIgnoreCase("D")) return TimeUnit.DAYS;

        throw new IllegalArgumentException("Unknown time unit '" + u + "'.  Supported units " + Join.join(", ", lowercase(TimeUnit.values())));
    }

    private static List<String> lowercase(Enum... units) {
        List<String> list = new ArrayList<String>();
        for (Enum unit : units) {
            list.add(unit.name().toLowerCase());
        }
        return list;
    }

    static {
        PropertyEditorManager.registerEditor(Duration.class, DurationEditor.class);
    }

}
