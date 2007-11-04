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
package org.apache.openejb.server.webservices.saaj;

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import javax.xml.soap.SOAPException;
import java.util.HashMap;
import java.util.Map;

class SaajFactoryFinder {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_WS, SaajFactoryFinder.class);
    private static final String SAAJ_PROVIDER_PROPERTY = "org.apache.openejb.server.webservices.saaj.provider";

    private static SaajUniverse.Type DEFAULT_SAAJ_UNIVERSE = null;

    private static final Map<String, Map<String, String>> SAAJ_FACTORIES = new HashMap<String, Map<String, String>>();
    static {
        SAAJ_FACTORIES.put(SaajUniverse.Type.AXIS1.toString(), createSAAJInfo(
                "org.apache.axis.soap.MessageFactoryImpl",
                "org.apache.axis.soap.SOAPFactoryImpl",
                "org.apache.axis.soap.SOAPConnectionFactoryImpl",
                "org.apache.axis.soap.SAAJMetaFactoryImpl"));
        SAAJ_FACTORIES.put(SaajUniverse.Type.AXIS2.toString(), createSAAJInfo(
                "org.apache.axis2.saaj.MessageFactoryImpl",
                "org.apache.axis2.saaj.SOAPFactoryImpl",
                "org.apache.axis2.saaj.SOAPConnectionFactoryImpl",
                "org.apache.axis2.saaj.SAAJMetaFactoryImpl"));
        SAAJ_FACTORIES.put(SaajUniverse.Type.SUN.toString(), createSAAJInfo(
                "com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl",
                "com.sun.xml.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl",
                "com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory",
                "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl"));

        initDefaultSAAJProvider();
    }

    private static void initDefaultSAAJProvider() {
        String provider = System.getProperty(SAAJ_PROVIDER_PROPERTY);
        if (provider != null) {
            if (provider.equalsIgnoreCase("axis2")) {
                DEFAULT_SAAJ_UNIVERSE = SaajUniverse.Type.AXIS2;
            } else if (provider.equalsIgnoreCase("sun")) {
                DEFAULT_SAAJ_UNIVERSE = SaajUniverse.Type.SUN;
            } else {
                throw new RuntimeException("Invalid SAAJ universe specified: " + provider);
            }

            logger.info("Default SAAJ universe: " + DEFAULT_SAAJ_UNIVERSE);
        } else {
            logger.info("Default SAAJ universe not set");
        }
    }

    private static Map<String, String> createSAAJInfo(String messageFactory, String soapFactory, String soapConnectionFactory, String metaFactory) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("javax.xml.soap.MessageFactory", messageFactory);
        map.put("javax.xml.soap.SOAPFactory", soapFactory);
        map.put("javax.xml.soap.SOAPConnectionFactory", soapConnectionFactory);
        map.put("javax.xml.soap.MetaFactory", metaFactory);
        return map;
    }

    static Object find(String factoryPropertyName) throws SOAPException {
        String factoryClassName = getFactoryClass(factoryPropertyName);
        if (factoryClassName == null) {
            throw new SOAPException("Provider for " + factoryPropertyName + " cannot be found", null);
        } else {
            return newInstance(factoryClassName);
        }
    }

    private static String getFactoryClass(String factoryName) {
        SaajUniverse.Type universe = SaajUniverse.getCurrentUniverse();
        if (universe == null || universe == SaajUniverse.Type.DEFAULT) {
            if (DEFAULT_SAAJ_UNIVERSE == null) {
                // Default SAAJ universe not set. 
                // Prefer Axis2 SAAJ if it is in class loader, otherwise use Sun's
                if (isAxis2InClassLoader()) {
                    universe = SaajUniverse.Type.AXIS2;
                } else {
                    universe = SaajUniverse.Type.SUN;
                }
            } else {
                // Use default SAAJ universe
                universe = DEFAULT_SAAJ_UNIVERSE;
            }
        }

        return SAAJ_FACTORIES.get(universe.toString()).get(factoryName);
    }

    private static boolean isAxis2InClassLoader() {
        try {
            loadClass("org.apache.axis2.saaj.MessageFactoryImpl");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Class loadClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            return Class.forName(className);
        } else {
            return classLoader.loadClass(className);
        }
    }

    private static Object newInstance(String factoryClassName) throws SOAPException {
        try {
            Class factory = null;
            try {
                factory = loadClass(factoryClassName);
            } catch (ClassNotFoundException cnfe) {
                factory = SaajFactoryFinder.class.getClassLoader().loadClass(factoryClassName);
            }
            return factory.newInstance();
        } catch (ClassNotFoundException e) {
            throw new SOAPException("Provider " + factoryClassName + " not found", e);
        } catch (Exception e) {
            throw new SOAPException("Provider " + factoryClassName + " could not be instantiated: " + e.getMessage(), e);
        }
    }
}
