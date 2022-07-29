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

import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.log.ConsoleColorHandler;
import org.apache.openejb.log.SingleLineFormatter;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * default conf = jre conf
 * user conf used transparently
 */
public class JuliLogStreamFactory implements LogStreamFactory {
    public static final String OPENEJB_LOG_COLOR_PROP = "openejb.log.color";

    private static String consoleHandlerClazz;
    private static boolean useOpenEJBHandler;

    public LogStream createLogStream(final LogCategory logCategory) {
        return new JuliLogStream(logCategory);
    }

    static {
        final boolean tomee = is("org.apache.tomee.catalina.TomcatLoader");
        final boolean embedded = is("org.apache.tomee.embedded.Container");

        // if embedded case enhance a bit logging if not set
        final Options options = SystemInstance.isInitialized() ? SystemInstance.get().getOptions() : new Options(JavaSecurityManagers.getSystemProperties());
        final boolean forceLogs = options.get("openejb.jul.forceReload", false);
        if ((!tomee || embedded || forceLogs) && JavaSecurityManagers.getSystemProperty("java.util.logging.manager") == null) {
            consoleHandlerClazz = options.get("openejb.jul.consoleHandlerClazz", (String) null);
            if (consoleHandlerClazz == null) {
                if (options.get(OPENEJB_LOG_COLOR_PROP, false) && isNotIDE()) {
                    consoleHandlerClazz = ConsoleColorHandler.class.getName();
                } else {
                    consoleHandlerClazz = OpenEJBSimpleLayoutHandler.class.getName();
                }
            }

            try { // check it will not fail later (case when a framework change the JVM classloading)
                ClassLoader.getSystemClassLoader().loadClass(consoleHandlerClazz);
            } catch (final ClassNotFoundException e) {
                consoleHandlerClazz = ConsoleHandler.class.getName();
            }

            if (forceLogs) {
                useOpenEJBHandler = options.get("openejb.jul.forceReload.use-openejb-handler", true);
                try {
                    final OpenEJBLogManager value = new OpenEJBLogManager();
                    Reflections.set(LogManager.class, null, "manager", value);
                    value.forceReset();

                    setRootLogger(value);

                    value.readConfiguration(); // re-read the config to ensure we have a parent logger
                } catch (final Exception e) {
                    // no-op
                }
            }
            // do it last since otherwise it can lock
            JavaSecurityManagers.setSystemProperty("java.util.logging.manager", OpenEJBLogManager.class.getName());
        }

        try {
            if (options.get("openjpa.Log", (String) null) == null) {
                JuliLogStreamFactory.class.getClassLoader().loadClass("org.apache.openjpa.lib.log.LogFactoryAdapter");
                JavaSecurityManagers.setSystemProperty("openjpa.Log", "org.apache.openejb.openjpa.JULOpenJPALogFactory");
            }
        } catch (final Exception ignored) {
            // no-op: openjpa is not at the classpath so don't trigger it loading with our logger
        }

        try {
            JavaSecurityManagers.setSystemProperty(WebBeansLoggerFacade.class.getName(), "org.apache.openejb.cdi.logging.ContainerJULLoggerFactory");
        } catch (final Throwable th) {
            // ignored, surely arquillian remote only so OWB is not here
        }
    }

    private static void setRootLogger(final OpenEJBLogManager value) {
        try { // if we don't do it - which is done in static part of the LogManager - we couldn't log user info when force-reload is to true
            final Class<?> rootLoggerClass = ClassLoader.getSystemClassLoader().loadClass("java.util.logging.LogManager$RootLogger");
            final Constructor<?> cons = rootLoggerClass.getDeclaredConstructor(LogManager.class);
            final boolean acc = cons.isAccessible();
            if (!acc) {
                cons.setAccessible(true);
            }
            final Logger rootLogger = Logger.class.cast(cons.newInstance(value));
            try {
                Reflections.set(value, "rootLogger", rootLogger);
            } finally {
                cons.setAccessible(acc);
            }
            value.addLogger(rootLogger);
            Reflections.invokeByReflection(Reflections.get(value, "systemContext"), "addLocalLogger", new Class<?>[]{Logger.class}, new Object[]{rootLogger});
            Reflections.invokeByReflection(Logger.getGlobal(), "setLogManager", new Class<?>[]{LogManager.class}, new Object[]{value});
            value.addLogger(Logger.getGlobal());
        } catch (final Throwable e) {
            // no-op
        }
    }

    public static boolean isNotIDE() {
        return !JavaSecurityManagers.getSystemProperty("java.class.path").contains("idea_rt"); // TODO: eclipse, netbeans
    }

    // TODO: mange conf by classloader? see tomcat log manager
    public static class OpenEJBLogManager extends LogManager {
        static {
            final LogManager mgr = LogManager.getLogManager();
            if (mgr instanceof OpenEJBLogManager) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        ((OpenEJBLogManager) mgr).forceReset();
                    }
                });
            }
        }

        public void forceReset() {
            super.reset();
        }

        @Override
        public void reset() throws SecurityException {
            // no-op
        }

        @Override
        public String getProperty(final String name) {
            final String parentValue = super.getProperty(name);

            if (SystemInstance.isInitialized()) {
                if (SystemInstance.get().getProperties().containsKey(name)) {
                    return SystemInstance.get().getProperty(name);
                }

                final String propertyKeyValue = "logging" + reverseProperty(name);
                if (SystemInstance.get().getProperties().containsKey(propertyKeyValue)) {
                    return SystemInstance.get().getProperty(propertyKeyValue);
                }
            }

            // if it is one of ours loggers and no value is defined let set our nice logging style
            if (OpenEJBLogManager.class.getName().equals(JavaSecurityManagers.getSystemProperty("java.util.logging.manager")) // custom logging
                && isOverridableLogger(name) // managed loggers
                && parentValue == null) { // not already defined
                if (name.endsWith(".handlers")) {
                    return consoleHandlerClazz;
                } else if (name.endsWith(".useParentHandlers")) {
                    return "false";
                }
            }
            return parentValue;
        }
    }

    private static String reverseProperty(final String name) {
        if (name.contains(".") && !name.endsWith(".")) {
            final int idx = name.lastIndexOf('.');
            return name.substring(idx) + "." + name.substring(0, idx);
        }
        return name;
    }

    private static boolean isOverridableLogger(final String name) {
        return useOpenEJBHandler
                || name.toLowerCase().contains("openejb")
                || name.toLowerCase().contains("transaction")
                || name.toLowerCase().contains("cxf")
                || name.toLowerCase().contains("timer")
                || (name.startsWith("org.apache.") && !name.startsWith("org.apache.geronimo.connector.work.WorkerContext."))
                || name.startsWith("openjpa.")
                || name.startsWith("net.sf.ehcache.")
                || name.startsWith("org.ehcache.")
                || name.startsWith("org.quartz.")
                || name.startsWith("org.hibernate.");
    }

    public static class OpenEJBSimpleLayoutHandler extends ConsoleHandler {
        public OpenEJBSimpleLayoutHandler() {
            setFormatter(new SingleLineFormatter());
        }

        @Override
        protected synchronized void setOutputStream(final OutputStream out) throws SecurityException {
            super.setOutputStream(new FilterOutputStream(System.out) { // don't close System.out to not loose important things like exceptions
                @Override
                public void write(final int b) throws IOException {
                    System.out.write(b);
                }

                @Override
                public void write(final byte[] b) throws IOException {
                    System.out.write(b);
                }

                @Override
                public void write(final byte[] b, final int off, final int len) throws IOException {
                    System.out.write(b, off, len);
                }

                @Override
                public void flush() throws IOException {
                    System.out.flush();
                }

                @Override
                public void close() throws IOException {
                    flush();
                }
            });
        }
    }

    private static boolean is(final String classname) {
        try {
            JuliLogStreamFactory.class.getClassLoader().loadClass(classname);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }
}
