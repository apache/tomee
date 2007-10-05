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
package org.apache.openejb.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JuliLogStream implements LogStream {
    protected Logger logger;

    public JuliLogStream(LogCategory logCategory) {
        logger = Logger.getLogger(logCategory.getName());
    }

    public boolean isFatalEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    public void fatal(String message) {
        log(Level.SEVERE, message, null);
    }

    public void fatal(String message, Throwable t) {
        log(Level.SEVERE, message, t);
    }

    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    public void error(String message) {
        log(Level.SEVERE, message, null);
    }

    public void error(String message, Throwable t) {
        log(Level.SEVERE, message, t);
    }

    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    public void warn(String message) {
        log(Level.WARNING, message, null);
    }

    public void warn(String message, Throwable t) {
        log(Level.WARNING, message, t);
    }

    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    public void info(String message) {
        log(Level.INFO, message, null);
    }

    public void info(String message, Throwable t) {
        log(Level.INFO, message, t);
    }

    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    public void debug(String message) {
        log(Level.FINE, message, null);
    }

    public void debug(String message, Throwable t) {
        log(Level.FINE, message, t);
    }

    private void log(Level level, String message, Throwable t) {
        if (logger.isLoggable(level)) {
            LogRecord logRecord = new OpenEJBLogRecord(level, message);
            if (t != null) logRecord.setThrown(t);
            logger.log(logRecord);
        }
    }

    private static class OpenEJBLogRecord extends LogRecord {
        /**
         * The name of the class that issued the logging call.
         *
         * @serial
         */
        private String sourceClassName;

        /**
         * The name of the method that issued the logging call.
         *
         * @serial
         */
        private String sourceMethodName;
        
        // If the source method and source class has been inited
        private transient boolean sourceInited;

        public OpenEJBLogRecord(Level level, String message) {
            super(level, message);
            sourceInited = false;
        }

        /**
         * Gets the name of the class that issued the logging call.
         *
         * @return the name of the class that issued the logging call
         */
        public String getSourceClassName() {
            initSource();
            return sourceClassName;
        }

        /**
         * Sets the name of the class that issued the logging call.
         *
         * @param sourceClassName
         *            the name of the class that issued the logging call
         */
        public void setSourceClassName(String sourceClassName) {
            sourceInited = true;
            this.sourceClassName = sourceClassName;
        }

        /**
         * Gets the name of the method that issued the logging call.
         *
         * @return the name of the method that issued the logging call
         */
        public String getSourceMethodName() {
            initSource();
            return sourceMethodName;
        }

        /**
         * Sets the name of the method that issued the logging call.
         *
         * @param sourceMethodName the name of the method that issued the logging call
         */
        public void setSourceMethodName(String sourceMethodName) {
            sourceInited = true;
            this.sourceMethodName = sourceMethodName;
        }

        /**
         *  Init the sourceClass and sourceMethod fields.
         */
        private void initSource() {
            if (!sourceInited) {
                // search back up the stack for the first use of the OpenEJB Logger
                StackTraceElement[] elements = (new Throwable()).getStackTrace();
                int i = 0;
                String current = null;
                for (; i < elements.length; i++) {
                    current = elements[i].getClassName();
                    if (current.equals(org.apache.openejb.util.Logger.class.getName())) {
                        break;
                    }
                }

                // Skip any internal OpenEJB Logger call
                while (++i < elements.length && elements[i].getClassName().equals(current)) {
                    // do nothing
                }

                // If we didn't run out of elements, set the source
                if (i < elements.length) {
                    this.sourceClassName = elements[i].getClassName();
                    this.sourceMethodName = elements[i].getMethodName();
                }
                sourceInited = true;
            }
        }
    }
}
