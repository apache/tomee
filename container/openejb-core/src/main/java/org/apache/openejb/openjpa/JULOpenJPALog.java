package org.apache.openejb.openjpa;

import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openjpa.lib.log.Log;

import java.util.logging.Level;
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
        logger.finest(o.toString());
    }

    @Override
    public void trace(Object o, Throwable t) {
        logger.log(Level.FINEST, o.toString(), t);
    }

    @Override
    public void info(Object o) {
        logger.info(o.toString());
    }

    @Override
    public void info(Object o, Throwable t) {
        logger.log(Level.INFO, o.toString(), t);
    }

    @Override
    public void warn(Object o) {
        logger.warning(o.toString());
    }

    @Override
    public void warn(Object o, Throwable t) {
        logger.log(Level.WARNING, o.toString(), t);
    }

    @Override
    public void error(Object o) {
        logger.severe(o.toString());
    }

    @Override
    public void error(Object o, Throwable t) {
        logger.log(Level.SEVERE, o.toString(), t);
    }

    @Override
    public void fatal(Object o) {
        logger.severe(o.toString());
    }

    @Override
    public void fatal(Object o, Throwable t) {
        logger.log(Level.SEVERE, o.toString(), t);
    }
}
