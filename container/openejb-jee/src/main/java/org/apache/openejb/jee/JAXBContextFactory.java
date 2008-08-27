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

import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public final class JAXBContextFactory {
//    private static boolean useSXC = false;

    public static JAXBContext newInstance(String s) throws JAXBException {
//        if (useSXC) {
//            try {
//                Sxc.newInstance(s);
//            } catch (NoClassDefFoundError e) {
//            }
//        }

        return JAXBContext.newInstance(s);
    }

    public static JAXBContext newInstance(String s, ClassLoader classLoader) throws JAXBException {
//        if (useSXC) {
//            try {
//                return Sxc.newInstance(s, classLoader);
//            } catch (NoClassDefFoundError e) {
//            }
//        }

        return JAXBContext.newInstance(s, classLoader);
    }

    public static JAXBContext newInstance(String s, ClassLoader classLoader, Map<String, ?> properties) throws JAXBException {
//        if (useSXC) {
//            try {
//                return Sxc.newInstance(s, classLoader, properties);
//            } catch (NoClassDefFoundError e) {
//            }
//        }

        return JAXBContext.newInstance(s, classLoader, properties);
    }

    public static JAXBContext newInstance(Class... classes) throws JAXBException {
//        if (useSXC) {
//            try {
//                return Sxc.newInstance(classes);
//            } catch (NoClassDefFoundError e) {
//            }
//        }

        return JAXBContext.newInstance(classes);
    }

    public static JAXBContext newInstance(Class[] classes, Map<String, ?> properties) throws JAXBException {
//        if (useSXC) {
//            try {
//                return Sxc.newInstance(classes, properties);
//            } catch (NoClassDefFoundError e) {
//            }
//        }

        return JAXBContext.newInstance(classes, properties);
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

}
