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

import org.apache.openejb.loader.SystemInstance;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * default conf = jre conf
 * user conf used transparently
 */
public class JuliLogStreamFactory implements LogStreamFactory {
    public LogStream createLogStream(LogCategory logCategory) {
        return new JuliLogStream(logCategory);
    }

    static {
        final boolean tomee = is("org.apache.tomee.catalina.TomcatLoader");
        final boolean embedded = is("org.apache.tomee.embedded.Container");

        // if embedded case enhance a bit logging if not set
        if ((!tomee || embedded) && System.getProperty("java.util.logging.manager") == null) {
            System.setProperty("java.util.logging.manager", OpenEJBLogManager.class.getName());
        }

        try {
            if (SystemInstance.get().getOptions().get("openjpa.Log", (String) null) == null) {
                JuliLogStreamFactory.class.getClassLoader().loadClass("org.apache.openjpa.lib.log.LogFactoryAdapter");
                System.setProperty("openjpa.Log", "org.apache.openejb.openjpa.JULOpenJPALogFactory");
            }
        } catch (Exception ignored) {
            // no-op: openjpa is not at the classpath so don't trigger it loading with our logger
        }
    }

    public static class OpenEJBLogManager extends LogManager {
        @Override
        public String getProperty(final String name) {
            final String parentValue = super.getProperty(name);
            // if it is one of ours loggers and no value is defined let set our nice logging style
            if (OpenEJBLogManager.class.getName().equals(System.getProperty("java.util.logging.manager")) // custom logging
                    && isOverridableLogger(name) // managed loggers
                    && parentValue == null) { // not already defined
                if (name.endsWith(".handlers")) {
                    return OpenEJBSimpleLayoutHandler.class.getName();
                } else if (name.endsWith(".useParentHandlers")) {
                    return "false";
                }
            }
            return super.getProperty(name);
        }

        private static boolean isOverridableLogger(String name) {
            return name.toLowerCase().contains("openejb")
                    || name.toLowerCase().contains("transaction")
                    || name.toLowerCase().contains("cxf")
                    || name.toLowerCase().contains("timer")
                    || name.startsWith("org.apache.")
                    || name.startsWith("net.sf.ehcache.")
                    || name.startsWith("org.quartz.")
                    || name.startsWith("org.hibernate.");
        }
    }

    public static class OpenEJBSimpleLayoutHandler extends ConsoleHandler {
        public OpenEJBSimpleLayoutHandler() {
            setFormatter(new SingleLineFormatter());
            setLevel(Level.INFO);
        }
    }

    private static boolean is(String classname) {
        try {
            JuliLogStreamFactory.class.getClassLoader().loadClass(classname);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
