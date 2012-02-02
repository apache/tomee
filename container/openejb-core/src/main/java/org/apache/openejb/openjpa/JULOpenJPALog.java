package org.apache.openejb.openjpa;

import org.apache.openejb.util.JuliLogStream;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openjpa.lib.log.Log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JULOpenJPALog  implements Log {
    private final Logger logger;

    public JULOpenJPALog(final String channel) {
        logger = Logger.getLogger(channel);
        if (logger.getHandlers().length == 0) {
            logger.addHandler(new JuliLogStreamFactory.OpenEJBSimpleLayoutHandler());
            logger.setUseParentHandlers(false);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public void trace(Object o) {
        logger.log(record(o, Level.FINEST));
    }

    @Override
    public void trace(Object o, Throwable t) {
        logger.log(record(o, t, Level.FINEST));
    }

    @Override
    public void info(Object o) {
        logger.log(record(o, Level.INFO));
    }

    @Override
    public void info(Object o, Throwable t) {
        logger.log(record(o, t, Level.INFO));
    }

    @Override
    public void warn(Object o) {
        logger.log(record(o, Level.WARNING));
    }

    @Override
    public void warn(Object o, Throwable t) {
        logger.log(record(o, t, Level.WARNING));
    }

    @Override
    public void error(Object o) {
        logger.log(record(o.toString(), Level.SEVERE));
    }

    @Override
    public void error(Object o, Throwable t) {
        logger.log(record(o, t, Level.SEVERE));
    }

    @Override
    public void fatal(Object o) {
        logger.log(record(o, Level.SEVERE));
    }

    @Override
    public void fatal(Object o, Throwable t) {
        logger.log(record(o, t, Level.SEVERE));
    }

    private LogRecord record(final Object o, final Throwable t, final Level level) {
        final LogRecord record = record(o, level);
        record.setThrown(t);
        return record;
    }

    private LogRecord record(final Object o,  final Level level) {
        final LogRecord record = new JuliLogStream.OpenEJBLogRecord(level, o.toString());
        record.setSourceMethodName(logger.getName());
        return record;
    }
}
