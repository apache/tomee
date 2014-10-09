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
package org.apache.openejb.maven.util;

import org.apache.juli.logging.Log;

public class TomEEMavenLog implements Log {
    public TomEEMavenLog(final String ignored ) {
        // no-op but needed by TomEELog (reloadable feature)
    }

    @Override
    public boolean isDebugEnabled() {
        return MavenLogStreamFactory.currentLogger().isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return MavenLogStreamFactory.currentLogger().isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return MavenLogStreamFactory.currentLogger().isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return MavenLogStreamFactory.currentLogger().isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return MavenLogStreamFactory.currentLogger().isDebugEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return MavenLogStreamFactory.currentLogger().isWarnEnabled();
    }

    @Override
    public void trace(final Object message) {
        MavenLogStreamFactory.currentLogger().debug(String.valueOf(message));
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        MavenLogStreamFactory.currentLogger().debug(String.valueOf(message), t);
    }

    @Override
    public void debug(final Object message) {
        MavenLogStreamFactory.currentLogger().debug(String.valueOf(message));
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        MavenLogStreamFactory.currentLogger().debug(String.valueOf(message), t);
    }

    @Override
    public void info(final Object message) {
        MavenLogStreamFactory.currentLogger().info(String.valueOf(message));
    }

    @Override
    public void info(final Object message, final Throwable t) {
        MavenLogStreamFactory.currentLogger().info(String.valueOf(message), t);
    }

    @Override
    public void warn(final Object message) {
        MavenLogStreamFactory.currentLogger().warn(String.valueOf(message));
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        MavenLogStreamFactory.currentLogger().warn(String.valueOf(message), t);
    }

    @Override
    public void error(final Object message) {
        MavenLogStreamFactory.currentLogger().error(String.valueOf(message));
    }

    @Override
    public void error(final Object message, final Throwable t) {
        MavenLogStreamFactory.currentLogger().error(String.valueOf(message), t);
    }

    @Override
    public void fatal(final Object message) {
        MavenLogStreamFactory.currentLogger().error(String.valueOf(message));
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        MavenLogStreamFactory.currentLogger().error(String.valueOf(message), t);
    }
}
