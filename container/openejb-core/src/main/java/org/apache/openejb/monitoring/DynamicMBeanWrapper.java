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

import org.apache.openejb.api.internal.Internal;
import org.apache.openejb.api.jmx.Description;
import org.apache.openejb.api.jmx.MBean;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.api.jmx.ManagedOperation;
import org.apache.openejb.api.jmx.NotificationInfo;
import org.apache.openejb.api.jmx.NotificationInfos;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.ImmutableDescriptor;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class DynamicMBeanWrapper implements DynamicMBean, MBeanRegistration {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_DEPLOY, DynamicMBeanWrapper.class);

    private static final Map<Class<?>, CacheInfo> CACHE = new HashMap<Class<?>, CacheInfo>();

    private static final Map<Class<?>, Class<? extends Annotation>> OPENEJB_API_TO_JAVAX = new HashMap<>();

    static {
        final ClassLoader loader = DynamicMBeanWrapper.class.getClassLoader();
        try { // all these dont work on java 9 cause of java.management module
            OPENEJB_API_TO_JAVAX.put(MBean.class, (Class<? extends Annotation>) loader.loadClass("javax.management.MBean"));
            OPENEJB_API_TO_JAVAX.put(Description.class, (Class<? extends Annotation>) loader.loadClass("javax.management.Description"));
            OPENEJB_API_TO_JAVAX.put(ManagedOperation.class, (Class<? extends Annotation>) loader.loadClass("javax.management.ManagedOperation"));
            OPENEJB_API_TO_JAVAX.put(ManagedAttribute.class, (Class<? extends Annotation>) loader.loadClass("javax.management.ManagedAttribute"));
            OPENEJB_API_TO_JAVAX.put(NotificationInfo.class, (Class<? extends Annotation>) loader.loadClass("javax.management.NotificationInfo"));
            OPENEJB_API_TO_JAVAX.put(NotificationInfos.class, (Class<? extends Annotation>) loader.loadClass("javax.management.NotificationInfos"));
        } catch (final ClassNotFoundException | NoClassDefFoundError cnfe) {
            // ignored
        }
    }

    private final MBeanInfo info;
    private final Map<String, Method> getters = new HashMap<>();
    private final Map<String, Method> setters = new HashMap<>();
    private final Map<String, Method> operations = new HashMap<>();
    private final Object instance;
    private final ClassLoader classloader;

    public DynamicMBeanWrapper(final Object givenInstance) {
        this(null, givenInstance);
    }

    public DynamicMBeanWrapper(final WebBeansContext wc, final Object givenInstance) {
        Class<?> annotatedMBean = givenInstance.getClass();

        // javaassist looses annotation so simply unwrap it
        if (wc != null) {
            if (givenInstance.getClass().getName().contains("$Owb")) { // isProxy
                annotatedMBean = annotatedMBean.getSuperclass();
            }
        }

        classloader = annotatedMBean.getClassLoader();
        instance = givenInstance;

        final CacheInfo cache = CACHE.get(annotatedMBean);
        if (cache == null) {
            final String description;
            final List<MBeanAttributeInfo> attributeInfos = new ArrayList<>();
            final List<MBeanOperationInfo> operationInfos = new ArrayList<>();
            final List<MBeanNotificationInfo> notificationInfos = new ArrayList<>();

            // class
            final Description classDescription = findAnnotation(annotatedMBean, Description.class);
            description = getDescription(classDescription, "a MBean built by OpenEJB");

            final NotificationInfo notification = findAnnotation(annotatedMBean, NotificationInfo.class);
            if (notification != null) {
                final MBeanNotificationInfo notificationInfo = getNotificationInfo(notification);
                notificationInfos.add(notificationInfo);
            }

            final NotificationInfos notifications = findAnnotation(annotatedMBean, NotificationInfos.class);
            if (notifications != null && notifications.value() != null) {
                for (final NotificationInfo n : notifications.value()) {
                    final MBeanNotificationInfo notificationInfo = getNotificationInfo(n);
                    notificationInfos.add(notificationInfo);
                }
            }


            // methods
            for (final Method m : annotatedMBean.getMethods()) {
                final int modifiers = m.getModifiers();
                if (m.getDeclaringClass().equals(Object.class)
                    || !Modifier.isPublic(modifiers)
                    || Modifier.isAbstract(modifiers)) {
                    continue;
                }

                if (findAnnotation(m, ManagedAttribute.class) != null) {
                    final String methodName = m.getName();
                    String attrName = methodName;
                    if ((attrName.startsWith("get") && m.getParameterTypes().length == 0
                        || attrName.startsWith("set") && m.getParameterTypes().length == 1)
                        && attrName.length() > 3) {
                        attrName = attrName.substring(3);
                        if (attrName.length() > 1) {
                            attrName = Character.toLowerCase(attrName.charAt(0)) + attrName.substring(1);
                        } else {
                            attrName = attrName.toLowerCase();
                        }
                    } else {
                        logger.warning("ignoring attribute " + m.getName() + " for " + annotatedMBean.getName());
                    }

                    if (methodName.startsWith("get")) {
                        getters.put(attrName, m);
                    } else if (methodName.startsWith("set")) {
                        setters.put(attrName, m);
                    }
                } else if (findAnnotation(m, ManagedOperation.class) != null) {
                    operations.put(m.getName(), m);

                    String operationDescr = "";
                    final Description descr = findAnnotation(m, Description.class);
                    if (descr != null) {
                        operationDescr = getDescription(descr, "-");
                    }

                    operationInfos.add(newMethodDescriptor(operationDescr, m));
                }
            }

            for (final Map.Entry<String, Method> e : getters.entrySet()) {
                final String key = e.getKey();
                final Method mtd = e.getValue();

                String attrDescr = "";
                final Description descr = findAnnotation(mtd, Description.class);
                if (descr != null) {
                    attrDescr = getDescription(descr, "-");
                }

                try {
                    attributeInfos.add(new MBeanAttributeInfo(key, attrDescr, mtd, setters.get(key)));
                } catch (final IntrospectionException ex) {
                    logger.warning("can't manage " + key + " for " + mtd.getName(), ex);
                }
            }

            // for updatable but not readable attributes
            for (final Map.Entry<String, Method> e : setters.entrySet()) {
                final String key = e.getKey();
                if (getters.get(key) != null) {
                    continue; //already done
                }

                final Method mtd = e.getValue();

                String attrDescr = "";
                final Description descr = findAnnotation(mtd, Description.class);
                if (descr != null) {
                    attrDescr = getDescription(descr, "-");
                }

                try {
                    attributeInfos.add(new MBeanAttributeInfo(key, attrDescr, null, setters.get(key)));
                } catch (final IntrospectionException ex) {
                    logger.warning("can't manage " + key + " for " + mtd.getName(), ex);
                }
            }

            info = new MBeanInfo(annotatedMBean.getName(),
                description,
                attributeInfos.toArray(new MBeanAttributeInfo[attributeInfos.size()]),
                null, // default constructor is mandatory
                operationInfos.toArray(new MBeanOperationInfo[operationInfos.size()]),
                notificationInfos.toArray(new MBeanNotificationInfo[notificationInfos.size()]));

            if (annotatedMBean.getAnnotation(Internal.class) != null) {
                CACHE.put(annotatedMBean, new CacheInfo(info, getters, setters, operations));
            }
        } else {
            info = cache.mBeanInfo;
            getters.putAll(cache.getters);
            setters.putAll(cache.setters);
            operations.putAll(cache.operations);
        }
    }

    private MBeanOperationInfo newMethodDescriptor(final String operationDescr, final Method m) {
        final MBeanOperationInfo jvmInfo = new MBeanOperationInfo(operationDescr, m);
        return new MBeanOperationInfo(
            m.getName(),
            operationDescr,
            methodSignature(jvmInfo, m),
            m.getReturnType().getName(),
            MBeanOperationInfo.UNKNOWN,
            jvmInfo.getDescriptor()); // avoid to copy the logic
    }

    private static MBeanParameterInfo[] methodSignature(final MBeanOperationInfo jvmInfo, final Method method) {
        final Class<?>[] classes = method.getParameterTypes();
        final Annotation[][] annots = method.getParameterAnnotations();
        return parameters(jvmInfo, classes, annots, method.getParameters());
    }

    static MBeanParameterInfo[] parameters(final MBeanOperationInfo jvmInfo,
                                           final Class<?>[] classes,
                                           final Annotation[][] annots, Parameter[] parameters) {
        final MBeanParameterInfo[] params =
            new MBeanParameterInfo[classes.length];
        assert classes.length == annots.length;

        String desc = "";
        for (int i = 0; i < classes.length; i++) {
            final Descriptor d = jvmInfo.getSignature()[i].getDescriptor();
            final String pn =  parameters[i].getName();
            for (final Annotation a : annots[i]) {
                final Class<? extends Annotation> type = a.annotationType();
                if (type.equals(Description.class) || type.equals(OPENEJB_API_TO_JAVAX.get(Description.class))) {
                    desc = getDescription(annotationProxy(a, Description.class), desc);
                    break;
                }
            }
            params[i] = new MBeanParameterInfo(pn, classes[i].getName(), desc, d);
        }

        return params;
    }

    private <T extends Annotation> T findAnnotation(final Method method, final Class<T> searchedAnnotation) {
        final T annotation = method.getAnnotation(searchedAnnotation);
        if (annotation != null) {
            return annotation;
        }

        if (OPENEJB_API_TO_JAVAX.containsKey(searchedAnnotation)) {
            final Class<? extends Annotation> clazz = OPENEJB_API_TO_JAVAX.get(searchedAnnotation);
            final Object javaxAnnotation = method.getAnnotation(clazz);
            if (javaxAnnotation != null) {
                return annotationProxy(javaxAnnotation, searchedAnnotation);
            }
        }
        return null;
    }

    private <T extends Annotation> T findAnnotation(final Class<?> annotatedMBean, final Class<T> searchedAnnotation) {
        final T annotation = annotatedMBean.getAnnotation(searchedAnnotation);
        if (annotation != null) {
            return annotation;
        }

        if (OPENEJB_API_TO_JAVAX.containsKey(searchedAnnotation)) {
            final Class<? extends Annotation> clazz = OPENEJB_API_TO_JAVAX.get(searchedAnnotation);
            final Object javaxAnnotation = annotatedMBean.getAnnotation(clazz);
            if (javaxAnnotation != null) {
                return annotationProxy(javaxAnnotation, searchedAnnotation);
            }
        }
        return null;
    }

    private static <T extends Annotation> T annotationProxy(final Object javaxAnnotation, final Class<T> clazz) {
        return (T) Proxy.newProxyInstance(DynamicMBeanWrapper.class.getClassLoader(), new Class<?>[]{clazz}, new AnnotationHandler(javaxAnnotation));
    }

    private static MBeanNotificationInfo getNotificationInfo(final NotificationInfo n) {
        final String description = getDescription(n.description(), "-");
        return new MBeanNotificationInfo(n.types(),
            n.notificationClass().getName(), description,
            new ImmutableDescriptor(n.descriptorFields()));
    }

    private static String getDescription(final Description d, final String defaultValue) {
        if (d != null) {
            if (d.bundleBaseName() != null && d.key() != null) {
                try {
                    return ResourceBundle
                        .getBundle(d.bundleBaseName())
                        .getString(d.key());
                } catch (final RuntimeException re) {
                    return d.value();
                }
            } else {
                return d.value();
            }
        }
        return defaultValue;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return info;
    }

    @Override
    public Object getAttribute(final String attribute)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException {
        if (getters.containsKey(attribute)) {
            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classloader);
            try {
                return getters.get(attribute).invoke(instance);
            } catch (final IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                logger.error("can't get " + attribute + " value", e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
        throw new AttributeNotFoundException();
    }

    @Override
    public void setAttribute(final Attribute attribute)
        throws AttributeNotFoundException, InvalidAttributeValueException,
        MBeanException, ReflectionException {
        if (setters.containsKey(attribute.getName())) {
            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classloader);
            try {
                setters.get(attribute.getName()).invoke(instance, attribute.getValue());
            } catch (final IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                logger.error("can't set " + attribute + " value", e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        } else {
            throw new AttributeNotFoundException();
        }
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        final AttributeList list = new AttributeList();
        for (final String n : attributes) {
            try {
                list.add(new Attribute(n, getAttribute(n)));
            } catch (final Exception ignore) {
                // no-op
            }
        }
        return list;
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        final AttributeList list = new AttributeList();
        for (final Object o : attributes) {
            final Attribute attr = (Attribute) o;
            try {
                setAttribute(attr);
                list.add(attr);
            } catch (final Exception ignore) {
                // no-op
            }
        }
        return list;
    }

    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature)
        throws MBeanException, ReflectionException {
        if (operations.containsKey(actionName)) {
            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classloader);
            try {
                return operations.get(actionName).invoke(instance, params);
            } catch (final IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                logger.error(actionName + "can't be invoked", e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
        throw new MBeanException(new IllegalArgumentException(), actionName + " doesn't exist");
    }

    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {
        final Thread thread = Thread.currentThread();
        final ClassLoader oldCl = thread.getContextClassLoader();
        thread.setContextClassLoader(classloader);
        try {
            if (MBeanRegistration.class.isInstance(instance)) {
                return MBeanRegistration.class.cast(instance).preRegister(server, name);
            }
            return name;
        } finally {
            thread.setContextClassLoader(oldCl);
        }
    }

    @Override
    public void postRegister(final Boolean registrationDone) {
        final Thread thread = Thread.currentThread();
        final ClassLoader oldCl = thread.getContextClassLoader();
        thread.setContextClassLoader(classloader);
        try {
            if (MBeanRegistration.class.isInstance(instance)) {
                MBeanRegistration.class.cast(instance).postRegister(registrationDone);
            }
        } finally {
            thread.setContextClassLoader(oldCl);
        }
    }

    @Override
    public void preDeregister() throws Exception {
        final Thread thread = Thread.currentThread();
        final ClassLoader oldCl = thread.getContextClassLoader();
        thread.setContextClassLoader(classloader);
        try {
            if (MBeanRegistration.class.isInstance(instance)) {
                MBeanRegistration.class.cast(instance).preDeregister();
            }
        } finally {
            thread.setContextClassLoader(oldCl);
        }
    }

    @Override
    public void postDeregister() {
        final Thread thread = Thread.currentThread();
        final ClassLoader oldCl = thread.getContextClassLoader();
        thread.setContextClassLoader(classloader);
        try {
            if (MBeanRegistration.class.isInstance(instance)) {
                MBeanRegistration.class.cast(instance).postDeregister();
            }
        } finally {
            thread.setContextClassLoader(oldCl);
        }
    }

    private static class AnnotationHandler implements InvocationHandler {
        private final Object delegate;

        public AnnotationHandler(final Object javaxAnnotation) {
            delegate = javaxAnnotation;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            Object result = null;
            for (final Method mtd : delegate.getClass().getMethods()) { // simple heurisitc which should be enough
                if (mtd.getName().equals(method.getName())) {
                    result = mtd.invoke(delegate, args);
                    break;
                }
            }

            if (result == null) {
                return null;
            }

            if (result.getClass().isArray()) {
                final Object[] array = (Object[]) result;
                if (array.length == 0 || !OPENEJB_API_TO_JAVAX.containsValue(array[0].getClass())) {
                    return array;
                }

                final Object[] translated = new Object[array.length];
                for (int i = 0; i < translated.length; i++) {
                    translated[i] = annotationProxy(array[i], OPENEJB_API_TO_JAVAX.get(array[i].getClass()));
                }
            }

            return result;
        }
    }

    private static final class CacheInfo {
        public final MBeanInfo mBeanInfo;
        public final Map<String, Method> getters;
        public final Map<String, Method> setters;
        public final Map<String, Method> operations;

        private CacheInfo(final MBeanInfo mBeanInfo,
                          final Map<String, Method> getters, final Map<String, Method> setters,
                          final Map<String, Method> operations) {
            this.mBeanInfo = mBeanInfo;
            this.getters = getters;
            this.setters = setters;
            this.operations = operations;
        }
    }
}
