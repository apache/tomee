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
        parse(string, this);
    }

    public long getTime() {
        return time;
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Duration duration = (Duration) o;

        if (time != duration.time) return false;
        if (unit != duration.unit) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) (time ^ (time >>> 32));
        result = 29 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }

    public String toString() {
        if (unit == null){
            return time + "";
        } else {
            return time + " " + unit;
        }
    }

    public static Duration parse(String text) {
        Duration d = new Duration();
        parse(text, d);
        return d;
    }

    private static void parse(String text, Duration d) {
        text = text.trim();

        StringBuilder t = new StringBuilder();
        StringBuilder u = new StringBuilder();

        int i = 0;

        // get the number
        for (; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c) || i == 0 && c == '-') {
                t.append(c);
            } else {
                break;
            }
        }

        if (t.length() == 0){
            invalidFormat(text);
        }

        // skip whitespace
        for (; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
            } else {
                break;
            }
        }

        // get time unit text part
        for (; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                u.append(c);
            } else {
                invalidFormat(text);
            }
        }

        d.time = Integer.parseInt(t.toString());

        Unit unit = parseUnit(u.toString());

        if (unit != null) switch (unit) {
            case days: {
                d.time *= 60 * 60 * 24;
                d.unit = TimeUnit.SECONDS;
            }
            ;
            break;
            case hours: {
                d.time *= 60 * 60;
                d.unit = TimeUnit.SECONDS;
            }
            ;
            break;
            case minutes: {
                d.time *= 60;
                d.unit = TimeUnit.SECONDS;
            }
            ;
            break;
            case seconds: {
                d.unit = TimeUnit.SECONDS;
            }
            ;
            break;
            case milliseconds: {
                d.unit = TimeUnit.MILLISECONDS;
            }
            ;
            break;
            case microseconds: {
                d.unit = TimeUnit.MICROSECONDS;
            }
            ;
            break;
            case nanoseconds: {
                d.unit = TimeUnit.NANOSECONDS;
            }
            ;
            break;
        }
    }

    private static void invalidFormat(String text) {
        throw new IllegalArgumentException("Illegal duration format: '"+text+"'.  Valid examples are '10s' or '10 seconds'.");
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
