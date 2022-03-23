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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.util.LinkedList;

public class SaajUniverse {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_WS, SaajUniverse.class);

    static {
        if (SystemInstance.get().getOptions().get("openejb.soap.override-factory", false)) { // default are far faster than our chain
            setProperty("jakarta.xml.soap.MessageFactory", "org.apache.openejb.server.webservices.saaj.MessageFactoryImpl");
            setProperty("jakarta.xml.soap.SOAPFactory", "org.apache.openejb.server.webservices.saaj.SoapFactoryImpl");
            setProperty("jakarta.xml.soap.SOAPConnectionFactory", "org.apache.openejb.server.webservices.saaj.SoapConnectionFactoryImpl");
            setProperty("jakarta.xml.soap.MetaFactory", "org.apache.openejb.server.webservices.saaj.SaajMetaFactoryImpl");
        }
    }

    private static void setProperty(String name, String value) {
        if (System.getProperty(name) == null) {
            System.setProperty(name, value);
        }
    }

    enum Type {DEFAULT, AXIS1, AXIS2, SUN}

    public static final Type DEFAULT = Type.DEFAULT;
    public static final Type SUN = Type.SUN;
    public static final Type AXIS1 = Type.AXIS1;
    public static final Type AXIS2 = Type.AXIS2;

    private static final ThreadLocal<LinkedList<Type>> CURRENT_UNIVERSE =
        new ThreadLocal<LinkedList<Type>>() {
            @Override
            protected LinkedList<Type> initialValue() {
                return new LinkedList<Type>();
            }
        };

    public void set(Type newUniverse) {
        final LinkedList<Type> universeList = CURRENT_UNIVERSE.get();
        universeList.add(newUniverse);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set universe: " + Thread.currentThread() + " " + newUniverse);
        }
    }

    public void unset() {
        final LinkedList<Type> universeList = CURRENT_UNIVERSE.get();
        if (universeList != null && !universeList.isEmpty()) {
            universeList.removeLast();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Restored universe: " + Thread.currentThread());
            }
        }
    }

    static Type getCurrentUniverse() {
        final LinkedList<Type> universeList = CURRENT_UNIVERSE.get();
        if (universeList != null && !universeList.isEmpty()) {
            return universeList.getLast();
        }
        return null;
    }

}
