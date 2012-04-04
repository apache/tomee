package org.apache.openejb.maven.util;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.LogStream;
import org.apache.openejb.util.LogStreamFactory;

public class MavenLogStreamFactory implements LogStreamFactory {
    private static Log logger;

    @Override
    public LogStream createLogStream(final LogCategory logCategory) {
        return new MavenLogStream(logger);
    }

    public static void setLogger(Log logger) {
        MavenLogStreamFactory.logger = logger;
    }

    private static class MavenLogStream implements LogStream {
        private final Log log;

        public MavenLogStream(Log logger) {
            log = logger;
        }

        @Override
        public boolean isFatalEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public void fatal(String message) {
            log.error(message);
        }

        @Override
        public void fatal(String message, Throwable t) {
            log.error(message, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public void error(String message) {
            log.error(message);
        }

        @Override
        public void error(String message, Throwable t) {
            log.error(message, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return log.isWarnEnabled();
        }

        @Override
        public void warn(String message) {
            log.warn(message);
        }

        @Override
        public void warn(String message, Throwable t) {
            log.warn(message, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return log.isInfoEnabled();
        }

        @Override
        public void info(String message) {
            log.info(message);
        }

        @Override
        public void info(String message, Throwable t) {
            log.info(message, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }

        @Override
        public void debug(String message) {
            log.debug(message);
        }

        @Override
        public void debug(String message, Throwable t) {
            log.debug(message, t);
        }
    }
}
