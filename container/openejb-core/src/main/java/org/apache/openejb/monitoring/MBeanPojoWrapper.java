package org.apache.openejb.monitoring;

import org.apache.openejb.util.Duration;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * This class attempts to wrap a given class by using Bean introspection to get a list of attributes.
 * The intention of the MBean generated is to provide a read-only view of resources available in the server.
 * At the present time, this wrapper does not provide write support for attributes, and does not support
 * method invocation.
 */
public class MBeanPojoWrapper implements DynamicMBean {

    private final Object delegate;
    private final String name;
    private MBeanInfo info;
    private final Map<String, PropertyDescriptor> attributeMap = new HashMap<>();

    private static final Set<Class<?>> SUPPORTED_PROPERTY_TYPES = new HashSet<Class<?>>() {
        {
            add(Integer.class);
            add(Boolean.class);
            add(Byte.class);
            add(Short.class);
            add(Float.class);
            add(Long.class);
            add(Double.class);
            add(Integer.TYPE);
            add(Boolean.TYPE);
            add(Byte.TYPE);
            add(Short.TYPE);
            add(Float.TYPE);
            add(Long.TYPE);
            add(Double.TYPE);
            add(String.class);
            add(Duration.class);
        }
    };

    public MBeanPojoWrapper(final String name, final Object delegate) {
        this.name = name;
        if (delegate == null) {
            throw new NullPointerException("Delegate cannot be null");
        }

        this.delegate = delegate;
        scan(delegate.getClass());
    }

    private void scan(Class clazz) {
        final List<MBeanAttributeInfo> mBeanAttributeInfoList = new ArrayList<>();

        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

            final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                final String propertyName = propertyDescriptor.getName();
                Class<?> propertyType = propertyDescriptor.getPropertyType();

                if (! isSupported(propertyType)) {
                    continue;
                }

                attributeMap.put(propertyName, propertyDescriptor);
                try {
                    mBeanAttributeInfoList.add(new MBeanAttributeInfo(propertyName, "", propertyDescriptor.getReadMethod(), null));
                } catch (IntrospectionException e) {
                    // no-op
                }
            }
        } catch (java.beans.IntrospectionException e) {
            // no-op
        }

        // default constructor is mandatory
        info = new MBeanInfo(name,
                "Auto-created by OpenEJB",
                mBeanAttributeInfoList.toArray(new MBeanAttributeInfo[mBeanAttributeInfoList.size()]),
                null, // default constructor is mandatory
                new MBeanOperationInfo[0],
                new MBeanNotificationInfo[0]);
    }

    private static boolean isSupported(Class<?> type) {
        return SUPPORTED_PROPERTY_TYPES.contains(type);
    }


    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (! attributeMap.containsKey(attribute)) {
            throw new AttributeNotFoundException();
        }

        try {
            return attributeMap.get(attribute).getReadMethod().invoke(delegate);
        } catch (IllegalAccessException e) {
            throw new MBeanException(e);
        } catch (InvocationTargetException e) {
            throw new MBeanException(e);
        }
    }

    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        // no-op
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        final AttributeList list = new AttributeList();
        for (final String attribute : attributes) {
            try {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            } catch (final Exception ignore) {
                // no-op
            }
        }
        return list;
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        // no-op - not supported
        return null;
    }

    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        // no-op - not supported
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return info;
    }
}
