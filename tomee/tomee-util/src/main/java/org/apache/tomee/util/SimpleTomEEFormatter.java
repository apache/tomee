/**
 *
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
package org.apache.tomee.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

// Note: don't rely on any dep since this class is often added to main cp
// even in tomee where only bootstrap and tomcat-juli are present
public class SimpleTomEEFormatter extends java.util.logging.Formatter {
    private static final String LN = System.getProperty("line.separator");

    @Override
    public synchronized String format(final LogRecord record) {
        final Throwable thrown = record.getThrown();
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append(record.getLevel().getLocalizedName());
        sbuf.append(" - ");
        sbuf.append(formatMessage(record));
        sbuf.append(LN);
        if (thrown != null) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                thrown.printStackTrace(pw);
                pw.close();
                sbuf.append(sw.toString());
            } catch (final Exception ex) {
                // no-op
            }
        }
        return sbuf.toString();
    }
}
