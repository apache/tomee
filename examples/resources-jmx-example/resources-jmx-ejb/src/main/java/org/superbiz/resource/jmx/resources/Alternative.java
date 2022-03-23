/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.superbiz.resource.jmx.resources;

import org.superbiz.resource.jmx.factory.Converter;
import org.superbiz.resource.jmx.factory.MBeanRegistrationException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class Alternative implements AlternativeMBean {

    private final Map<String, Class<?>> primitives = new HashMap<String, Class<?>>() {
        {
            put("boolean", Boolean.TYPE);
            put("byte", Byte.TYPE);
            put("char", Character.TYPE);
            put("long", Long.TYPE);
            put("float", Float.TYPE);
            put("int", Integer.TYPE);
            put("double", Double.TYPE);
            put("short", Short.TYPE);
        }
    };

    private static Logger LOGGER = Logger.getLogger(Alternative.class.getName());
    private Properties properties;

    @PreDestroy
    public void preDestroy() throws MBeanRegistrationException {
        final String name = properties.getProperty("name");
        requireNotNull(name);

        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final ObjectName objectName = new ObjectName(name);
            mbs.unregisterMBean(objectName);
        } catch (final MalformedObjectNameException e) {
            LOGGER.severe("Malformed MBean name: " + name);
            throw new MBeanRegistrationException(e);
        } catch (final javax.management.MBeanRegistrationException e) {
            LOGGER.severe("Error unregistering " + name);
            throw new MBeanRegistrationException(e);
        } catch (InstanceNotFoundException e) {
            LOGGER.severe("Error unregistering " + name);
            throw new MBeanRegistrationException(e);
        }
    }

    @PostConstruct
    public <T> void postConstruct() throws MBeanRegistrationException {

        final String name = properties.remove("name").toString();
        final String iface = properties.remove("interface").toString();
        final String prefix = properties.remove("prefix").toString();

        requireNotNull(name);
        requireNotNull(iface);

        try {
            final Class<T> ifaceCls = (Class<T>) Class.forName(iface, true, Thread.currentThread().getContextClassLoader());
            final StandardMBean mBean = new StandardMBean((T) this, ifaceCls);

            for (String attributeName : properties.stringPropertyNames()) {
                final Object value = properties.remove(attributeName);

                if (prefix != null) {
                    if (!attributeName.startsWith(prefix + ".")) {
                        continue;
                    } else {
                        attributeName = attributeName.substring(prefix.length() + 1);
                    }
                }

                final Class<?> targetType = findAttributeType(mBean.getMBeanInfo(), attributeName);
                final Object targetValue = Converter.convert(value, targetType, null);

                final Attribute attribute = new Attribute(attributeName, targetValue);
                mBean.setAttribute(attribute);
            }

            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final ObjectName objectName = new ObjectName(name);
            mbs.registerMBean(this, objectName);

        } catch (final Exception e) {
            LOGGER.severe("Unable to register mbean " + e.getMessage());
            throw new MBeanRegistrationException(e);
        }
    }

    private Class<?> findAttributeType(MBeanInfo mBeanInfo, String attributeName) {
        try {
            for (final MBeanAttributeInfo attribute : mBeanInfo.getAttributes()) {
                if (attribute.getName().equals(attributeName)) {
                    return convertPrimitive(attribute.getType());
                }
            }

            return null;
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    private Class<?> convertPrimitive(final String type) throws ClassNotFoundException {
        if (primitives.containsKey(type)) {
            return primitives.get(type);
        }

        return Class.forName(type, true, Thread.currentThread().getContextClassLoader());
    }

    private void requireNotNull(final String object) throws MBeanRegistrationException {
        if (object == null) {
            throw new MBeanRegistrationException("code property not specified, stopping");
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    private int count = 0;

    @Override
    public String greet(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        return "Hello, " + name;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int value) {
        count = value;
    }

    @Override
    public void increment() {
        count++;
    }
}
