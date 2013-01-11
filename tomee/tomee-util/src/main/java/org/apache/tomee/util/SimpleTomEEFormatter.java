package org.apache.tomee.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

public class SimpleTomEEFormatter extends java.util.logging.Formatter {
    private static final String LN = System.getProperty("line.separator");

    @Override
    public synchronized String format(LogRecord record) {
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
            } catch (Exception ex) {
                // no-op
            }
        }
        return sbuf.toString();
    }
}
