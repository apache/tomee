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
package org.apache.openejb.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * default conf = jre conf
 * user conf used transparently
 */
public class JuliLogStreamFactory implements LogStreamFactory {
    public LogStream createLogStream(LogCategory logCategory) {
        return new JuliLogStream(logCategory);
    }

    static {
        final Class<LogCategory> clazz = LogCategory.class;
        for (Field constant : clazz.getFields()) {
            int modifiers = constant.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
                final String name;
                try {
                    name = ((LogCategory) constant.get(null)).getName();
                } catch (IllegalAccessException e) {
                    continue;
                }

                final Enumeration<String> names = LogManager.getLogManager().getLoggerNames();
                boolean init = true;
                while (names.hasMoreElements()) {
                    if (names.nextElement().startsWith(name)) {
                        init = false;
                        break;
                    }
                }
                if (init) { // no conf
                    final Logger logger = java.util.logging.Logger.getLogger(name);
                    logger.setUseParentHandlers(false);
                    LogManager.getLogManager().addLogger(logger);
                    if (logger.getHandlers().length == 0) {
                        logger.addHandler(new ConsoleHandler());
                    }
                    for (Handler h : logger.getHandlers()) {
                        h.setFormatter(new SingleLineFormatter());
                    }
                }
            }
        }
    }
}
