/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.log.commonslogging;

import org.apache.commons.logging.Log;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class OpenEJBCommonsLog implements Log, Serializable {
    private transient Logger logger;
    private final String category;

    public OpenEJBCommonsLog(final String category) {
        this.category = category;
        logger = Logger.getInstance(LogCategory.OPENEJB, category);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarningEnabled();
    }

    @Override
    public void trace(final Object message) {
        logger.debug(String.valueOf(message));
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        logger.debug(String.valueOf(message), t);
    }

    @Override
    public void debug(final Object message) {
        logger.debug(String.valueOf(message));
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        logger.debug(String.valueOf(message), t);
    }

    @Override
    public void info(final Object message) {
        logger.info(String.valueOf(message));
    }

    @Override
    public void info(final Object message, final Throwable t) {
        logger.info(String.valueOf(message), t);
    }

    @Override
    public void warn(final Object message) {
        logger.warning(String.valueOf(message));
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        logger.warning(String.valueOf(message), t);
    }

    @Override
    public void error(final Object message) {
        logger.error(String.valueOf(message));
    }

    @Override
    public void error(final Object message, final Throwable t) {
        logger.error(String.valueOf(message), t);
    }

    @Override
    public void fatal(final Object message) {
        logger.fatal(String.valueOf(message));
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        logger.fatal(String.valueOf(message), t);
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeUTF(category);
    }

    private void readObject(final ObjectInputStream in) throws IOException {
        logger = Logger.getInstance(LogCategory.OPENEJB, in.readUTF());
    }
}
