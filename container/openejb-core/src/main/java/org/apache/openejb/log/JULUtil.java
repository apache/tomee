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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.log;

import java.util.logging.Level;
import org.apache.openejb.loader.SystemInstance;

public final class JULUtil {
    public static final String DEFAULT_LOG_LEVEL = "INFO";
    public static final String OPENEJB_LOG_LEVEL = "openejb.log.level";

    private JULUtil() {
        // no-op
    }

    public static Level level() {
        final String propLevel = SystemInstance.get().getProperty(OPENEJB_LOG_LEVEL, DEFAULT_LOG_LEVEL).toUpperCase();
        try {
            return (Level) Level.class.getDeclaredField(propLevel).get(null);
        } catch (IllegalAccessException e) {
            return Level.INFO;
        } catch (NoSuchFieldException e) {
            return Level.INFO;
        }
    }
}
