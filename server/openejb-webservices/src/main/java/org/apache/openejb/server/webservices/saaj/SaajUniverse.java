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

import java.util.LinkedList;

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

public class SaajUniverse {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_WS, SaajUniverse.class);
    
    static {
        setProperty("javax.xml.soap.MessageFactory", "org.apache.openejb.server.webservices.saaj.MessageFactoryImpl");
        setProperty("javax.xml.soap.SOAPFactory", "org.apache.openejb.server.webservices.saaj.SoapFactoryImpl");
        setProperty("javax.xml.soap.SOAPConnectionFactory", "org.apache.openejb.server.webservices.saaj.SoapConnectionFactoryImpl");
        setProperty("javax.xml.soap.MetaFactory", "org.apache.openejb.server.webservices.saaj.MetaFactoryImpl");
    }

    private static void setProperty(String name, String value) {
        if (System.getProperty(name) == null) {
            System.setProperty(name, value);
        }
    }

    enum Type { DEFAULT, AXIS1, AXIS2, SUN }
    
    public static final Type DEFAULT = Type.DEFAULT;
    public static final Type SUN = Type.SUN;
    public static final Type AXIS1 = Type.AXIS1;
    public static final Type AXIS2 = Type.AXIS2;
    
    private static final ThreadLocal<LinkedList<Type>> currentUniverse = 
        new InheritableThreadLocal<LinkedList<Type>>();
        
    public void set(Type newUniverse) {
        LinkedList<Type> universeList = currentUniverse.get();
        if (universeList == null) {
            universeList = new LinkedList<Type>();
            currentUniverse.set(universeList);
        }
        universeList.add(newUniverse);
        if (logger.isDebugEnabled()) {
            logger.debug("Set universe: " + Thread.currentThread() + " " + newUniverse);
        }
    }
    
    public void unset() {
        LinkedList<Type> universeList = currentUniverse.get();
        if (universeList != null && !universeList.isEmpty()) {
            universeList.removeLast();
            if (logger.isDebugEnabled()) {
                logger.debug("Restored universe: " + Thread.currentThread());
            }
        }
    }
    
    static Type getCurrentUniverse() {
        LinkedList<Type> universeList = currentUniverse.get();
        if (universeList != null && !universeList.isEmpty()) {
            return universeList.getLast();
        } else {
            return null;
        }                
    }
       
}
