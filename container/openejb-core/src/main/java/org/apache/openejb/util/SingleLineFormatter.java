package org.apache.openejb.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SingleLineFormatter extends Formatter {
    private String lineSeparator = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator"));

    @Override public synchronized String format(LogRecord record) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append(record.getLevel().getLocalizedName());
        sbuf.append(" - ");
        sbuf.append(formatMessage(record));
        sbuf.append(lineSeparator);
        if (record.getThrown() != null) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sbuf.append(sw.toString());
            } catch (Exception ex) {
                 // no-op
            }
        }
        return sbuf.toString();
    }
}
