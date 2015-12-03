/*
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

package org.apache.openejb.monitoring;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.Set;

public final class LocalMBeanServer implements MBeanServer {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, LocalMBeanServer.class);

    public static final String OPENEJB_JMX_ACTIVE = "openejb.jmx.active";

    private static final LocalMBeanServer INSTANCE = new LocalMBeanServer();
    private static boolean active = SystemInstance.get().getOptions().get(OPENEJB_JMX_ACTIVE, true);

    private LocalMBeanServer() {
        // no-op
    }

    public static void reset() {
        active = SystemInstance.get().getOptions().get(OPENEJB_JMX_ACTIVE, true);
    }

    public static MBeanServer get() {
        return INSTANCE;
    }

    public static boolean isJMXActive() {
        return active;
    }

    public static ObjectInstance registerSilently(final Object mbean, final ObjectName name) {
        try {
            if (get().isRegistered(name)) {
                get().unregisterMBean(name);
            }

            return get().registerMBean(mbean, name);

        } catch (final Exception e) {
            LOGGER.error("Cannot register MBean " + name); // silently so no stack
        }
        return null;
    }

    public static ObjectInstance registerDynamicWrapperSilently(final Object object, final ObjectName name) {
        return registerSilently(new DynamicMBeanWrapper(object), name);
    }

    public static void unregisterSilently(final ObjectName name) {
        try {
            get().unregisterMBean(name);

        } catch (final Exception e) {
            LOGGER.error("Cannot unregister MBean " + name); // silently so no stack
        }
    }

    public static TabularData tabularData(final String typeName, final String typeDescription, final String[] names, final Object[] values) {
        if (names.length == 0) {
            return null;
        }

        final OpenType<?>[] types = new OpenType<?>[names.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = SimpleType.STRING;
        }

        try {
            final CompositeType ct = new CompositeType(typeName, typeDescription, names, names, types);
            final TabularType type = new TabularType(typeName, typeDescription, ct, names);
            final TabularDataSupport data = new TabularDataSupport(type);

            final CompositeData line = new CompositeDataSupport(ct, names, values);
            data.put(line);

            return data;
        } catch (final OpenDataException e) {
            return null;
        }
    }

    public static TabularData tabularData(final String typeName, final String typeDescription, final String description, final Properties properties) {
        final String[] names = properties.keySet().toArray(new String[properties.size()]);
        final Object[] values = new Object[names.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = properties.get(names[i]).toString(); // hibernate put objects in properties for instance
        }
        return tabularData(typeName, typeDescription, names, values);
    }

    private static MBeanServer s() {
        if (isJMXActive()) {
            return ManagementFactory.getPlatformMBeanServer();
        }
        return NoOpMBeanServer.INSTANCE;
    }

    @Override
    public ObjectInstance createMBean(final String className, final ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return s().createMBean(className, name);
    }

    @Override
    public ObjectInstance createMBean(final String className, final ObjectName name, final ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return s().createMBean(className, name, loaderName);
    }

    @Override
    public ObjectInstance createMBean(final String className, final ObjectName name, final Object[] params, final String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return s().createMBean(className, name, params, signature);
    }

    @Override
    public ObjectInstance createMBean(final String className, final ObjectName name, final ObjectName loaderName, final Object[] params, final String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return s().createMBean(className, name, loaderName, params, signature);
    }

    @Override
    public ObjectInstance registerMBean(final Object object, final ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        return s().registerMBean(object, name);
    }

    @Override
    public void unregisterMBean(final ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException {
        s().unregisterMBean(name);
    }

    @Override
    public ObjectInstance getObjectInstance(final ObjectName name) throws InstanceNotFoundException {
        return s().getObjectInstance(name);
    }

    @Override
    public Set<ObjectInstance> queryMBeans(final ObjectName name, final QueryExp query) {
        return s().queryMBeans(name, query);
    }

    @Override
    public Set<ObjectName> queryNames(final ObjectName name, final QueryExp query) {
        return s().queryNames(name, query);
    }

    @Override
    public boolean isRegistered(final ObjectName name) {
        return s().isRegistered(name);
    }

    @Override
    public Integer getMBeanCount() {
        return s().getMBeanCount();
    }

    @Override
    public Object getAttribute(final ObjectName name, final String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return s().getAttribute(name, attribute);
    }

    @Override
    public AttributeList getAttributes(final ObjectName name, final String[] attributes) throws InstanceNotFoundException, ReflectionException {
        return s().getAttributes(name, attributes);
    }

    @Override
    public void setAttribute(final ObjectName name, final Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        s().setAttribute(name, attribute);
    }

    @Override
    public AttributeList setAttributes(final ObjectName name, final AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
        return s().setAttributes(name, attributes);
    }

    @Override
    public Object invoke(final ObjectName name, final String operationName, final Object[] params, final String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
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
    public void addNotificationListener(final ObjectName name, final NotificationListener listener, final NotificationFilter filter, final Object handback) throws InstanceNotFoundException {
        s().addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void addNotificationListener(final ObjectName name, final ObjectName listener, final NotificationFilter filter, final Object handback) throws InstanceNotFoundException {
        s().addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(final ObjectName name, final ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException {
        s().removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(final ObjectName name, final ObjectName listener, final NotificationFilter filter, final Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
        s().removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(final ObjectName name, final NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException {
        s().removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(final ObjectName name, final NotificationListener listener, final NotificationFilter filter, final Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
        s().removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public MBeanInfo getMBeanInfo(final ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        return s().getMBeanInfo(name);
    }

    @Override
    public boolean isInstanceOf(final ObjectName name, final String className) throws InstanceNotFoundException {
        return s().isInstanceOf(name, className);
    }

    @Override
    public Object instantiate(final String className) throws ReflectionException, MBeanException {
        return s().instantiate(className);
    }

    @Override
    public Object instantiate(final String className, final ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return s().instantiate(className, loaderName);
    }

    @Override
    public Object instantiate(final String className, final Object[] params, final String[] signature) throws ReflectionException, MBeanException {
        return s().instantiate(className, params, signature);
    }

    @Override
    public Object instantiate(final String className, final ObjectName loaderName, final Object[] params, final String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return s().instantiate(className, loaderName, params, signature);
    }

    @Override
    public ObjectInputStream deserialize(final ObjectName name, final byte[] data) throws InstanceNotFoundException, OperationsException {
        return s().deserialize(name, data);
    }

    @Override
    public ObjectInputStream deserialize(final String className, final byte[] data) throws OperationsException, ReflectionException {
        return s().deserialize(className, data);
    }

    @Override
    public ObjectInputStream deserialize(final String className, final ObjectName loaderName, final byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException {
        return s().deserialize(className, loaderName, data);
    }

    @Override
    public ClassLoader getClassLoaderFor(final ObjectName mbeanName) throws InstanceNotFoundException {
        return s().getClassLoaderFor(mbeanName);
    }

    @Override
    public ClassLoader getClassLoader(final ObjectName loaderName) throws InstanceNotFoundException {
        return s().getClassLoader(loaderName);
    }

    @Override
    public ClassLoaderRepository getClassLoaderRepository() {
        return s().getClassLoaderRepository();
    }

    private static class NoOpMBeanServer implements MBeanServer {
        public static final MBeanServer INSTANCE = new NoOpMBeanServer();
        private static final String[] DEFAULT_DOMAINS = new String[]{"default-domain"};

        @Override
        public ObjectInstance createMBean(final String className, final ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
            return null;
        }

        @Override
        public ObjectInstance createMBean(final String className, final ObjectName name, final ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
            return null;
        }

        @Override
        public ObjectInstance createMBean(final String className, final ObjectName name, final Object[] params, final String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
            return null;
        }

        @Override
        public ObjectInstance createMBean(final String className, final ObjectName name, final ObjectName loaderName, final Object[] params, final String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
            return null;
        }

        @Override
        public ObjectInstance registerMBean(final Object object, final ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
            return null;
        }

        @Override
        public void unregisterMBean(final ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException {
            // no-op
        }

        @Override
        public ObjectInstance getObjectInstance(final ObjectName name) throws InstanceNotFoundException {
            return null;
        }

        @Override
        public Set<ObjectInstance> queryMBeans(final ObjectName name, final QueryExp query) {
            return null;
        }

        @Override
        public Set<ObjectName> queryNames(final ObjectName name, final QueryExp query) {
            return null;
        }

        @Override
        public boolean isRegistered(final ObjectName name) {
            return false;
        }

        @Override
        public Integer getMBeanCount() {
            return 0;
        }

        @Override
        public Object getAttribute(final ObjectName name, final String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
            return null;
        }

        @Override
        public AttributeList getAttributes(final ObjectName name, final String[] attributes) throws InstanceNotFoundException, ReflectionException {
            return new AttributeList();
        }

        @Override
        public void setAttribute(final ObjectName name, final Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            // no-op
        }

        @Override
        public AttributeList setAttributes(final ObjectName name, final AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
            return new AttributeList();
        }

        @Override
        public Object invoke(final ObjectName name, final String operationName, final Object[] params, final String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
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
        public void addNotificationListener(final ObjectName name, final NotificationListener listener, final NotificationFilter filter, final Object handback) throws InstanceNotFoundException {
            // no-op
        }

        @Override
        public void addNotificationListener(final ObjectName name, final ObjectName listener, final NotificationFilter filter, final Object handback) throws InstanceNotFoundException {
            // no-op
        }

        @Override
        public void removeNotificationListener(final ObjectName name, final ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException {
            // no-op
        }

        @Override
        public void removeNotificationListener(final ObjectName name, final ObjectName listener, final NotificationFilter filter, final Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
            // no-op
        }

        @Override
        public void removeNotificationListener(final ObjectName name, final NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException {
            // no-op
        }

        @Override
        public void removeNotificationListener(final ObjectName name, final NotificationListener listener, final NotificationFilter filter, final Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
            // no-op
        }

        @Override
        public MBeanInfo getMBeanInfo(final ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
            return null;
        }

        @Override
        public boolean isInstanceOf(final ObjectName name, final String className) throws InstanceNotFoundException {
            return false;
        }

        @Override
        public Object instantiate(final String className) throws ReflectionException, MBeanException {
            return null;
        }

        @Override
        public Object instantiate(final String className, final ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException {
            return null;
        }

        @Override
        public Object instantiate(final String className, final Object[] params, final String[] signature) throws ReflectionException, MBeanException {
            return null;
        }

        @Override
        public Object instantiate(final String className, final ObjectName loaderName, final Object[] params, final String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException {
            return null;
        }

        @Override
        public ObjectInputStream deserialize(final ObjectName name, final byte[] data) throws InstanceNotFoundException, OperationsException {
            return null;
        }

        @Override
        public ObjectInputStream deserialize(final String className, final byte[] data) throws OperationsException, ReflectionException {
            return null;
        }

        @Override
        public ObjectInputStream deserialize(final String className, final ObjectName loaderName, final byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException {
            return null;
        }

        @Override
        public ClassLoader getClassLoaderFor(final ObjectName mbeanName) throws InstanceNotFoundException {
            return null;
        }

        @Override
        public ClassLoader getClassLoader(final ObjectName loaderName) throws InstanceNotFoundException {
            return null;
        }

        @Override
        public ClassLoaderRepository getClassLoaderRepository() {
            return null;
        }
    }
}
