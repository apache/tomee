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
package org.apache.openejb.log;

import org.fusesource.jansi.Ansi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SingleLineFormatter extends Formatter {
    @Override
    public synchronized String format(LogRecord record) {
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
            } catch (Exception ex) {
                // no-op
            } finally {
                suffix(sbuf, record);
            }
        }
        return sbuf.toString();
    }

    protected Ansi prefix(final LogRecord record) {
        return Ansi.ansi();
    }

    protected Ansi suffix(final Ansi ansi, final LogRecord record) {
        return ansi;
    }
}
