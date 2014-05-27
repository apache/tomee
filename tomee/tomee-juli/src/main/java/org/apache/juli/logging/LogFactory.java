/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.juli.logging;


import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.LogManager;


// tomcat doesn't have any spi mecanism so forking this class
public /* abstract */ class LogFactory {
    public static final String FACTORY_PROPERTY =
            "org.apache.commons.logging.LogFactory";

    public static final String FACTORY_DEFAULT =
            "org.apache.commons.logging.impl.LogFactoryImpl";

    public static final String FACTORY_PROPERTIES =
            "commons-logging.properties";

    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY =
            "org.apache.commons.logging.LogFactory.HashtableImpl";

    private static LogFactory singleton=new LogFactory();
    private final Collection<String> names = new HashSet<String>();

    Properties logConfig;

    protected LogFactory() {
        logConfig=new Properties();
    }

    protected static void setSingleton(final LogFactory singleton) {
        if (singleton == null) {
            return;
        }
        LogFactory.singleton = singleton;
    }

    void setLogConfig( final Properties p ) {
        this.logConfig=p;
    }

    public Collection<String> getNames() {
        return names;
    }

    public Log getInstance(final String name)
            throws LogConfigurationException {
        synchronized (names) {
            names.add(name);
        }
        return DirectJDKLog.getInstance(name);
    }

    public void release() {
        DirectJDKLog.release();
    }

    public Object getAttribute(final String name) {
        return logConfig.get(name);
    }

    public String[] getAttributeNames() {
        final String[] result = new String[logConfig.size()];
        return logConfig.keySet().toArray(result);
    }

    public void removeAttribute(final String name) {
        logConfig.remove(name);
    }

    public void setAttribute(final String name, final Object value) {
        logConfig.put(name, value);
    }

    public Log getInstance(final Class<?> clazz)
            throws LogConfigurationException {
        return getInstance( clazz.getName());
    }

    public static LogFactory getFactory() throws LogConfigurationException {
        return singleton;
    }

    public static Log getLog(final Class<?> clazz)
            throws LogConfigurationException {
        return getFactory().getInstance(clazz);

    }

    public static Log getLog(final String name)
            throws LogConfigurationException {
        return getFactory().getInstance(name);

    }

    public static void release(final ClassLoader classLoader) {
        // JULI's log manager looks at the current classLoader so there is no
        // need to use the passed in classLoader, the default implementation
        // does not so calling reset in that case will break things
        if (!LogManager.getLogManager().getClass().getName().equals(
                "java.util.logging.LogManager")) {
            LogManager.getLogManager().reset();
        }
    }

    public static void releaseAll() {
        singleton.release();
    }

    public static String objectId(final Object o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + "@" + System.identityHashCode(o);
        }
    }
}
