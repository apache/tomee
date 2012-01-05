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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.monitoring;

import javax.management.*;
import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class LocalMBeanServer implements MBeanServer {
    public static final String OPENEJB_JMX_ACTIVE = "openejb.jmx.active";

    private static final LocalMBeanServer INSTANCE = new LocalMBeanServer();

    private LocalMBeanServer() {
        // no-op
    }

    public static MBeanServer get() {
        return INSTANCE;
    }

    private static boolean isJMXActive() {
        return "true".equalsIgnoreCase(System.getProperty(OPENEJB_JMX_ACTIVE, "true"));
    }

    private static MBeanServer s() {
        if (isJMXActive()) {
            return ManagementFactory.getPlatformMBeanServer();
        }
        return NoOpMBeanServer.INSTANCE;
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return s().createMBean(className, name);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return s().createMBean(className, name, loaderName);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return s().createMBean(className, name, params, signature);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return s().createMBean(className, name, loaderName, params, signature);
    }

    @Override
    public ObjectInstance registerMBean(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        return s().registerMBean(object, name);
    }

    @Override
    public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException {
        s().unregisterMBean(name);
    }

    @Override
    public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException {
        return s().getObjectInstance(name);
    }

    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
        return queryMBeans(name, query);
    }

    @Override
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
        return s().queryNames(name, query);
    }

    @Override
    public boolean isRegistered(ObjectName name) {
        return s().isRegistered(name);
    }

    @Override
    public Integer getMBeanCount() {
        return s().getMBeanCount();
    }

    @Override
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return s().getAttribute(name, attribute);
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
        return s().getAttributes(name, attributes);
    }

    @Override
    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        s().setAttribute(name, attribute);
    }

    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
        return s().setAttributes(name, attributes);
    }

    @Override
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return s().invoke(name, operationName, params, signature);
    }

    @Override
    public String getDefaultDomain() {
        return s().getDefaultDomain();
    }

    @Override
    public String[] getDomains() {
        return s().getDomains();
    }

    @Override
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
        s().addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
        s().addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException {
        s().removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
        s().removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException {
        s().removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
        s().removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        return s().getMBeanInfo(name);
    }

    @Override
    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException {
        return s().isInstanceOf(name, className);
    }

    @Override
    public Object instantiate(String className) throws ReflectionException, MBeanException {
        return s().instantiate(className);
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return s().instantiate(className, loaderName);
    }

    @Override
    public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException {
        return s().instantiate(className, params, signature);
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return s().instantiate(className, loaderName, params, signature);
    }

    @Override
    public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException {
        return s().deserialize(name, data);
    }

    @Override
    public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException {
        return s().deserialize(className, data);
    }

    @Override
    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException {
        return s().deserialize(className, loaderName, data);
    }

    @Override
    public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException {
        return s().getClassLoaderFor(mbeanName);
    }

    @Override
    public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException {
        return s().getClassLoader(loaderName);
    }

    @Override
    public ClassLoaderRepository getClassLoaderRepository() {
        return s().getClassLoaderRepository();
    }

    private static class NoOpMBeanServer implements MBeanServer{
        public static final MBeanServer INSTANCE = new NoOpMBeanServer();
        private static final String[] DEFAULT_DOMAINS = new String[]{ "default-domain" };

        @Override
        public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
            return null;
        }

        @Override
        public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
            return null;
        }

        @Override
        public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
            return null;
        }

        @Override
        public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
            return null;
        }

        @Override
        public ObjectInstance registerMBean(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
            return null;
        }

        @Override
        public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException {
            // no-op
        }

        @Override
        public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException {
            return null;
        }

        @Override
        public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
            return null;
        }

        @Override
        public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
            return null;
        }

        @Override
        public boolean isRegistered(ObjectName name) {
            return false;
        }

        @Override
        public Integer getMBeanCount() {
            return 0;
        }

        @Override
        public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
            return null;
        }

        @Override
        public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
            return new AttributeList();
        }

        @Override
        public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            // no-op
        }

        @Override
        public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
            return new AttributeList();
        }

        @Override
        public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
            return null;
        }

        @Override
        public String getDefaultDomain() {
            return DEFAULT_DOMAINS[0];
        }

        @Override
        public String[] getDomains() {
            return DEFAULT_DOMAINS;
        }

        @Override
        public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
            // no-op
        }

        @Override
        public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
            // no-op
        }

        @Override
        public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException {
            // no-op
        }

        @Override
        public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
            // no-op
        }

        @Override
        public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException {
            // no-op
        }

        @Override
        public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
            // no-op
        }

        @Override
        public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
            return null;
        }

        @Override
        public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException {
            return false;
        }

        @Override
        public Object instantiate(String className) throws ReflectionException, MBeanException {
            return null;
        }

        @Override
        public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException {
            return null;
        }

        @Override
        public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException {
            return null;
        }

        @Override
        public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException {
            return null;
        }

        @Override
        public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException {
            return null;
        }

        @Override
        public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException {
            return null;
        }

        @Override
        public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException {
            return null;
        }

        @Override
        public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException {
            return null;
        }

        @Override
        public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException {
            return null;
        }

        @Override
        public ClassLoaderRepository getClassLoaderRepository() {
            return null;
        }
    }
}
