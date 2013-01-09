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
package org.apache.openejb.openjpa;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactoryAdapter;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class JULOpenJPALogFactory extends LogFactoryAdapter {
    @Override
    protected Log newLogAdapter(final String channel) {
        return new JULOpenJPALog(new LoggerCreator(channel));
    }

    private static class LoggerCreator implements Callable<Logger> {
        private final String name;
        private Logger logger;

        public LoggerCreator(final String channel) {
            name = channel;
        }

        @Override
        public Logger call() throws Exception {
            if (logger == null) {
                synchronized (this) { // no need of lock for this part
                    if (logger == null) {
                        try {
                            logger = Logger.getLogger(name);
                        } catch (Exception e) {
                            logger = Logger.getLogger(name); // try again
                        }
                    }
                }
            }
            return logger;
        }
    }
}
