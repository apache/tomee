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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Description;
import javax.management.DynamicMBean;
import javax.management.ImmutableDescriptor;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ManagedAttribute;
import javax.management.ManagedOperation;
import javax.management.NotificationInfo;
import javax.management.NotificationInfos;
import javax.management.ReflectionException;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class DynamicMBeanWrapper implements DynamicMBean {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_DEPLOY, DynamicMBeanWrapper.class);

    private final MBeanInfo info;
    private final Map<String, Method> getters = new HashMap<String, Method>();
    private final Map<String, Method> setters = new HashMap<String, Method>();
    private final Map<String, Method> operations = new HashMap<String, Method>();
    private final Object instance;

    public DynamicMBeanWrapper(Class<?> annotatedMBean) {
        String description;
        List<MBeanAttributeInfo> attributeInfos = new ArrayList<MBeanAttributeInfo>();
        List<MBeanOperationInfo> operationInfos = new ArrayList<MBeanOperationInfo>();
        List<MBeanNotificationInfo> notificationInfos = new ArrayList<MBeanNotificationInfo>();

        // instantiation
        try {
            instance = annotatedMBean.newInstance();
        } catch (InstantiationException ie) {
            logger.error("can't instantiate " + annotatedMBean.getName(), ie);
            throw new IllegalArgumentException(annotatedMBean.getName());
        } catch (IllegalAccessException iae) {
            logger.error("can't instantiate " + annotatedMBean.getName(), iae);
            throw new IllegalArgumentException(annotatedMBean.getName());
        }

        // class
        Description classDescription = annotatedMBean.getAnnotation(Description.class);
        description = getDescription(classDescription, "a MBean built by OpenEJB");

        NotificationInfo notification = annotatedMBean.getAnnotation(NotificationInfo.class);
        if (notification != null) {
            MBeanNotificationInfo notificationInfo = getNotificationInfo(notification);
            notificationInfos.add(notificationInfo);
        }

        NotificationInfos notifications = annotatedMBean.getAnnotation(NotificationInfos.class);
        if (notifications != null && notifications.value() != null) {
            for (NotificationInfo n : notifications.value()) {
                MBeanNotificationInfo notificationInfo = getNotificationInfo(n);
                notificationInfos.add(notificationInfo);
            }
        }


        // methods
        for (Method m : annotatedMBean.getMethods()) {
            int modifiers = m.getModifiers();
            if (m.getDeclaringClass().equals(Object.class)
                || !Modifier.isPublic(modifiers)
                || Modifier.isAbstract(modifiers)) {
                continue;
            }

            if (m.getAnnotation(ManagedAttribute.class) != null) {
                String methodName = m.getName();
                String attrName = methodName;
                if (((attrName.startsWith("get") && m.getParameterTypes().length == 0)
                    || (attrName.startsWith("set") && m.getParameterTypes().length == 1))
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
            } else if (m.getAnnotation(ManagedOperation.class) != null) {
                operations.put(m.getName(), m);

                String operationDescr = "";
                Description descr = m.getAnnotation(Description.class);
                if (descr != null) {
                    operationDescr = getDescription(descr, "-");
                }

                operationInfos.add(new MBeanOperationInfo(operationDescr, m));
            }
        }

        for (Map.Entry<String, Method> e : getters.entrySet()) {
            String key = e.getKey();
            Method mtd = e.getValue();

            String attrDescr = "";
            Description descr = mtd.getAnnotation(Description.class);
            if (descr != null) {
                attrDescr = getDescription(descr, "-");
            }

            try {
                attributeInfos.add(new MBeanAttributeInfo(key, attrDescr, mtd, setters.get(key)));
            } catch (IntrospectionException ex) {
                logger.warning("can't manage " + key + " for " + mtd.getName(), ex);
            }
        }

        info = new MBeanInfo(annotatedMBean.getName(),
            description,
            attributeInfos.toArray(new MBeanAttributeInfo[attributeInfos.size()]),
            null, // default constructor is mandatory
            operationInfos.toArray(new MBeanOperationInfo[operationInfos.size()]),
            notificationInfos.toArray(new MBeanNotificationInfo[notificationInfos.size()]));
    }

    private MBeanNotificationInfo getNotificationInfo(NotificationInfo n) {
        String description = getDescription(n.description(), "-");
        MBeanNotificationInfo ni = new MBeanNotificationInfo(n.types(),
            n.notificationClass().getName(), description,
            new ImmutableDescriptor(n.descriptorFields()));
        return ni;
    }

    private String getDescription(Description d, String defaultValue) {
        if (d != null) {
            if (d.bundleBaseName() != null && d.key() != null) {
                try {
                    return ResourceBundle
                        .getBundle(d.bundleBaseName())
                        .getString(d.key());
                } catch (RuntimeException re) {
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
    public Object getAttribute(String attribute)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException {
        if (getters.containsKey(attribute)) {
            try {
                return getters.get(attribute).invoke(instance);
            } catch (IllegalArgumentException e) {
                logger.error("can't get " + attribute + " value", e);
            } catch (IllegalAccessException e) {
                logger.error("can't get " + attribute + " value", e);
            } catch (InvocationTargetException e) {
                logger.error("can't get " + attribute + " value", e);
            }
        }
        throw new AttributeNotFoundException();
    }

    @Override
    public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, InvalidAttributeValueException,
        MBeanException, ReflectionException {
        if (setters.containsKey(attribute.getName())) {
            try {
                setters.get(attribute.getName()).invoke(instance, attribute.getValue());
            } catch (IllegalArgumentException e) {
                logger.error("can't set " + attribute + " value", e);
            } catch (IllegalAccessException e) {
                logger.error("can't set " + attribute + " value", e);
            } catch (InvocationTargetException e) {
                logger.error("can't set " + attribute + " value", e);
            }
        } else {
            throw new AttributeNotFoundException();
        }
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (String n : attributes) {
            try {
                list.add(new Attribute(n, getAttribute(n)));
            } catch (Exception ignore) {
                // no-op
            }
        }
        return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList list = new AttributeList();
        Iterator<Object> it = attributes.iterator();
        ;
        while (it.hasNext()) {
            Attribute attr = (Attribute) it.next();
            try {
                setAttribute(attr);
                list.add(attr);
            } catch (Exception ignore) {
                // no-op
            }
        }
        return list;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
        throws MBeanException, ReflectionException {
        if (operations.containsKey(actionName)) {
            try {
                return operations.get(actionName).invoke(instance, params);
            } catch (IllegalArgumentException e) {
                logger.error(actionName + "can't be invoked", e);
            } catch (IllegalAccessException e) {
                logger.error(actionName + "can't be invoked", e);
            } catch (InvocationTargetException e) {
                logger.error(actionName + "can't be invoked", e);
            }
        }
        throw new MBeanException(new IllegalArgumentException(), actionName + " doesn't exist");
    }
}
