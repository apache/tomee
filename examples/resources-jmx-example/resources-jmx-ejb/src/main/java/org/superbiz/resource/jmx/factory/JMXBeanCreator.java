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

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.Logger;

public class JMXBeanCreator {

    private static Logger LOGGER = Logger.getLogger(JMXBeanCreator.class.getName());
    private Properties properties;

    public Object create() throws MBeanRegistrationException {
        // instantiate the bean

        final String code = properties.getProperty("code");
        final String name = properties.getProperty("name");

        requireNotNull(code);
        requireNotNull(name);

        try {
            final Class<?> cls = Class.forName(code, true, Thread.currentThread().getContextClassLoader());
            final Object instance = cls.newInstance();

            final Field[] fields = cls.getDeclaredFields();
            for (final Field field : fields) {

                final String property = properties.getProperty(field.getName());
                if (property == null) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    field.set(instance, Converter.convert(property, field.getType(), field.getName()));
                } catch (Exception e) {
                    LOGGER.info(String.format("Unable to set value %s on field %s", property, field.getName()));
                }
            }

            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final ObjectName objectName = new ObjectName(name);
            mbs.registerMBean(instance, objectName);

            return instance;

        } catch (final ClassNotFoundException e) {
            LOGGER.severe("Unable to find class " + code);
            throw new MBeanRegistrationException(e);
        } catch (final InstantiationException e) {
            LOGGER.severe("Unable to create instance of class " + code);
            throw new MBeanRegistrationException(e);
        } catch (final IllegalAccessException e) {
            LOGGER.severe("Illegal access: " + code);
            throw new MBeanRegistrationException(e);
        } catch (final MalformedObjectNameException e) {
            LOGGER.severe("Malformed MBean name: " + name);
            throw new MBeanRegistrationException(e);
        } catch (final InstanceAlreadyExistsException e) {
            LOGGER.severe("Instance already exists: " + name);
            throw new MBeanRegistrationException(e);
        } catch (final NotCompliantMBeanException e) {
            LOGGER.severe("Class is not a valid MBean: " + code);
            throw new MBeanRegistrationException(e);
        } catch (final javax.management.MBeanRegistrationException e) {
            LOGGER.severe("Error registering " + name + ", " + code);
            throw new MBeanRegistrationException(e);
        }
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
