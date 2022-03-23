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
package org.apache.openejb.jee;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class JAXBContextFactory {
    private static final boolean USE_FAST_BOOT = "true".equals(System.getProperty("openejb.jaxb.fastBoot", "true"));
    private static final String FAST_BOOT = "com.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot";

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(JAXBContextFactory.class.getName());
//    private static boolean useSXC = false;

    private static String setFastBoot() {
        final String fastBoot = System.getProperty(FAST_BOOT);
        if (USE_FAST_BOOT) {
            System.setProperty(FAST_BOOT, Boolean.TRUE.toString());
        }
        return fastBoot;
    }

    private static void resetFastBoot(final String fastBoot) {
        if (USE_FAST_BOOT) {
            if (fastBoot == null) {
                System.clearProperty(FAST_BOOT);
            } else {
                System.setProperty(FAST_BOOT, fastBoot);
            }
        }
    }

    public static JAXBContext newInstance(final String s) throws JAXBException {
//        if (useSXC) {
//            try {
//                Sxc.newInstance(s);
//            } catch (NoClassDefFoundError e) {
//            }
//        }
        final Event event = Event.start(s);
        try {
            final String fastBoot = setFastBoot();
            try {
                return JAXBContext.newInstance(s);
            } finally {
                resetFastBoot(fastBoot);
            }
        } finally {
            event.stop();
        }
    }

    public static JAXBContext newInstance(final String s, final ClassLoader classLoader) throws JAXBException {
//        if (useSXC) {
//            try {
//                return Sxc.newInstance(s, classLoader);
//            } catch (NoClassDefFoundError e) {
//            }
//        }

        final Event event = Event.start(s);
        try {
            final String fastBoot = setFastBoot();
            try {
                return JAXBContext.newInstance(s, classLoader);
            } finally {
                resetFastBoot(fastBoot);
            }
        } finally {
            event.stop();
        }
    }

    public static JAXBContext newInstance(final String s, final ClassLoader classLoader, final Map<String, ?> properties) throws JAXBException {
//        if (useSXC) {
//            try {
//                return Sxc.newInstance(s, classLoader, properties);
//            } catch (NoClassDefFoundError e) {
//            }
//        }

        final Event event = Event.start(s);
        try {
            final String fastBoot = setFastBoot();
            try {
                return JAXBContext.newInstance(s, classLoader, properties);
            } finally {
                resetFastBoot(fastBoot);
            }
        } finally {
            event.stop();
        }
    }

    public static JAXBContext newInstance(final Class... classes) throws JAXBException {
//        if (useSXC) {
//            try {
//                return Sxc.newInstance(classes);
//            } catch (NoClassDefFoundError e) {
//            }
//        }
        final StringBuilder sb = new StringBuilder();
        for (final Class clazz : classes) {
            sb.append(clazz.getName());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        final Event event = Event.start(sb.toString());
        try {
            final String fastBoot = setFastBoot();
            try {
                return JAXBContext.newInstance(classes);
            } catch (final LinkageError ignore) {
                return JAXBContext.newInstance(classes);
            } finally {
                resetFastBoot(fastBoot);
            }
        } finally {
            event.stop();
        }
    }

    public static JAXBContext newInstance(final Class[] classes, final Map<String, ?> properties) throws JAXBException {
//        if (useSXC) {
//            try {
//                return Sxc.newInstance(classes, properties);
//            } catch (NoClassDefFoundError e) {
//            }
//        }

        final StringBuilder sb = new StringBuilder();
        for (final Class clazz : classes) {
            sb.append(clazz.getName());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        final Event event = Event.start(sb.toString());
        try {
            final String fastBoot = setFastBoot();
            try {
                return JAXBContext.newInstance(classes, properties);
            } finally {
                resetFastBoot(fastBoot);
            }
        } finally {
            event.stop();
        }
    }

//    public static class Sxc {
//        public static JAXBContext newInstance(String s) throws JAXBException {
//            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//            if (classLoader == null) classLoader = JAXBContextFactory.class.getClassLoader();
//            return com.envoisolutions.sxc.jaxb.JAXBContextImpl.newInstance(s, classLoader, Collections.singletonMap("com.envoisolutions.sxc.generate", "false"));
//        }
//
//        public static JAXBContext newInstance(String s, ClassLoader classLoader) throws JAXBException {
//            return com.envoisolutions.sxc.jaxb.JAXBContextImpl.newInstance(s, classLoader, Collections.singletonMap("com.envoisolutions.sxc.generate", "false"));
//        }
//
//        public static JAXBContext newInstance(String s, ClassLoader classLoader, Map<String, ?> properties) throws JAXBException {
//            if (properties == null) properties = new TreeMap<String, Object>();
//            // hack because intellij is being stupid
//            ((Map<String, Object>) properties).put("com.envoisolutions.sxc.generate", "false");
//            return com.envoisolutions.sxc.jaxb.JAXBContextImpl.newInstance(s, classLoader, properties);
//        }
//
//        public static JAXBContext newInstance(Class... classes) throws JAXBException {
//            JAXBContext jaxbContext = null;
//            jaxbContext = com.envoisolutions.sxc.jaxb.JAXBContextImpl.newInstance(classes, Collections.singletonMap("com.envoisolutions.sxc.generate", "false"));
//            return jaxbContext;
//        }
//
//        public static JAXBContext newInstance(Class[] classes, Map<String, ?> properties) throws JAXBException {
//            if (properties == null) properties = new TreeMap<String, Object>();
//            // hack because intellij is being stupid
//            ((Map<String, Object>) properties).put("com.envoisolutions.sxc.generate", "false");
//            return com.envoisolutions.sxc.jaxb.JAXBContextImpl.newInstance(classes, properties);
//        }
//    }


    private static class Event {
        protected final long start = System.nanoTime();
        private final String description;

        private Event(final String description) {
            this.description = description;
        }

        public static Event start(final String description) {
            return new Event(description);
        }

        public void stop() {
            log.log(Level.FINE, String.format("JAXBContext.newInstance %s  %s", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.start), this.description));
        }
    }
}
