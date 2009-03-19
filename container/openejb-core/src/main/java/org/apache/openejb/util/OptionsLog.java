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
package org.apache.openejb.util;

import org.apache.openejb.loader.Options;

/**
 * @version $Rev$ $Date$
 */
public class OptionsLog implements Options.Log {

    private final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("options"), OptionsLog.class);

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarningEnabled() {
        return logger.isWarningEnabled();
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public void debug(String message, Throwable t, Object... args) {
        logger.debug(message, t, args);
    }

    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void info(String message, Throwable t, Object... args) {
        logger.info(message, t, args);
    }

    public void warning(String message, Object... args) {
        logger.warning(message, args);
    }

    public void warning(String message, Throwable t, Object... args) {
        logger.warning(message, t, args);
    }
}
