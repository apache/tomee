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
package org.apache.tomee.jul.formatter;

import org.apache.juli.AsyncFileHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class AsyncConsoleHandler extends AsyncFileHandler {
    private final ConsoleHandler delegate = new ConsoleHandler() {{
        setFormatter(new SingleLineFormatter()); // console -> dev. File uses plain old format
    }};

    protected void publishInternal(final LogRecord record) {
        delegate.publish(record);
    }

    // copy cause of classloading
    private static class SingleLineFormatter extends Formatter {
        private static final String SEP = System.getProperty("line.separator", "\n");

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        @Override
        public synchronized String format(final LogRecord record) {
            final boolean exception = record.getThrown() != null;
            final StringBuilder sbuf = new StringBuilder();
            sbuf.append(record.getLevel().getLocalizedName());
            sbuf.append(" - ");
            sbuf.append(this.formatMessage(record));
            sbuf.append(SEP);
            if (exception) {
                try {
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sbuf.append(sw.toString());
                } catch (final Exception ex) {
                    // no-op
                }
            }
            return sbuf.toString();
        }
    }
}
