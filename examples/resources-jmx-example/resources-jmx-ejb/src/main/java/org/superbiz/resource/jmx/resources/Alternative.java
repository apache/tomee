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

import org.superbiz.resource.jmx.factory.MBeanRegistrationException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.logging.Logger;

public class Alternative implements AlternativeMBean {

    private static Logger LOGGER = Logger.getLogger(Alternative.class.getName());
    private Properties properties;

    @PostConstruct
    public void postConstruct() throws MBeanRegistrationException {
        // initialize the bean

        final String code = properties.getProperty("code");
        final String name = properties.getProperty("name");

        requireNotNull(code);
        requireNotNull(name);

        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final ObjectName objectName = new ObjectName(name);
            mbs.registerMBean(this, objectName);
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
