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

package org.superbiz.resource.jmx.factory;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class JMXBeanCreator {

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

    private static Logger LOGGER = Logger.getLogger(JMXBeanCreator.class.getName());
    private Properties properties;

    public <T> Object create() throws MBeanRegistrationException {
        final String code = (String) properties.remove("code");
        final String name = (String) properties.remove("name");
        final String iface = (String) properties.remove("interface");
        final String prefix = (String) properties.remove("prefix");

        requireNotNull(code);
        requireNotNull(name);
        requireNotNull(iface);

        try {
            final Class<? extends T> cls = (Class<? extends T>) Class.forName(code, true, Thread.currentThread().getContextClassLoader());
            final Class<T> ifaceCls = (Class<T>) Class.forName(iface, true, Thread.currentThread().getContextClassLoader());
            final T instance = (T) cls.newInstance();
            final StandardMBean mBean = new StandardMBean(instance, ifaceCls);

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
            mbs.registerMBean(instance, objectName);

            return instance;

        } catch (final Exception e) {
            e.printStackTrace();
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
}
