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

package org.apache.openejb.log;

import org.apache.openejb.loader.SystemInstance;
import org.fusesource.jansi.Ansi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ColorFormatter extends SingleLineFormatter {
    public static final String OPENEJB_LOG_COLOR_PREFIX = "openejb.log.color.";

    private final Map<Level, Ansi.Color> colors = new HashMap<>();

    public ColorFormatter() {
        // until info level  information should be not so import so keep default color
        colors.put(Level.FINEST, color("finest", Ansi.Color.DEFAULT.name()));
        colors.put(Level.FINER, color("finer", Ansi.Color.DEFAULT.name()));
        colors.put(Level.FINE, color("fine", Ansi.Color.DEFAULT.name()));

        // info is the main one so white? (default is often a bit gray so less visible)
        colors.put(Level.INFO, color("info", Ansi.Color.WHITE.name()));

        // warn and more are important information so specify a visible color
        colors.put(Level.WARNING, color("warning", Ansi.Color.YELLOW.name()));
        colors.put(Level.SEVERE, color("severe", Ansi.Color.RED.name()));
    }

    @Override
    public synchronized String format(final LogRecord record) {
        final boolean exception = record.getThrown() != null;
        final Ansi sbuf = prefix(record);
        sbuf.a(record.getLevel().getLocalizedName());
        sbuf.a(" - ");
        sbuf.a(formatMessage(record));
        if (!exception) {
            suffix(sbuf, record);
        }
        sbuf.newline();
        if (exception) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sbuf.a(sw.toString());
            } catch (final Exception ex) {
                // no-op
            } finally {
                suffix(sbuf, record);
            }
        }
        return sbuf.toString();
    }

    private Ansi.Color color(final String lvl, final String aDefault) {
        try {
            return Ansi.Color.valueOf(SystemInstance.get().getProperty(OPENEJB_LOG_COLOR_PREFIX + lvl, aDefault));
        } catch (final IllegalArgumentException iae) {
            return Ansi.Color.valueOf(aDefault);
        }
    }

    private Ansi color(final Level key) {
        if (colors.containsKey(key)) {
            return Ansi.ansi().fg(colors.get(key));
        }
        return Ansi.ansi().fg(Ansi.Color.DEFAULT);
    }

    protected Ansi prefix(final LogRecord record) {
        return color(record.getLevel());
    }

    protected Ansi suffix(final Ansi ansi, final LogRecord record) {
        return ansi.reset();
    }
}
