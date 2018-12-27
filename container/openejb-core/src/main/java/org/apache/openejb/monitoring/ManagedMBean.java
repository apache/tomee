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

import org.apache.openejb.util.Classes;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.propertyeditor.PropertyEditors;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class ManagedMBean implements DynamicMBean {

    private static final MBeanNotificationInfo[] EMPTY_NOTIFICATIONS = new MBeanNotificationInfo[0];

    private final List<MBeanAttributeInfo> attributes = new ArrayList<>();
    private final List<MBeanOperationInfo> operations = new ArrayList<>();
    private final Map<String, Member> attributesMap = new HashMap<>();
    private final Map<String, MethodMember> operationsMap = new HashMap<>();
    private final List<Member> dynamic = new ArrayList<>();

    private Pattern includes = Pattern.compile("");
    private Pattern excludes = Pattern.compile("");
    private boolean filterAttributes;
    private MBeanParameterInfo excludeInfo;
    private MBeanParameterInfo includeInfo;

    public ManagedMBean(final Object managed) {
        this(managed, "");

        try {
            final Method method = this.getClass().getMethod("setAttributesFilter", String.class, String.class);

            final String description = "Filters the attributes that show up in the MBeanInfo." +
                "  The exclude is applied first, then any attributes that match the " +
                "include are re-added.  It may be required to disconnect and reconnect " +
                "the JMX console to force a refresh of the MBeanInfo";

            excludeInfo = new MBeanParameterInfo("excludeRegex", "java.lang.String", "\"" + excludes.pattern() + "\"");
            includeInfo = new MBeanParameterInfo("includeRegex", "java.lang.String", "\"" + includes.pattern() + "\"");
            final MBeanOperationInfo filterOperation = new MBeanOperationInfo("FilterAttributes", description, new MBeanParameterInfo[]{
                excludeInfo,
                includeInfo,
            }, "void", 3);
            operations.add(filterOperation);
            operationsMap.put(filterOperation.getName(), new MethodMember(method, this, ""));

            filterAttributes = true;
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    ManagedMBean(final Object managed, final String prefix) {
        scan(managed, prefix);

        for (final Member member : attributesMap.values()) {
            attributes.add(new MBeanAttributeInfo(member.getName(), member.getType().getName(), "", true, false, false));
        }

        for (final Member member : operationsMap.values()) {
            final MBeanOperationInfo op = new MBeanOperationInfo("", ((MethodMember) member).getter);
            operations.add(new MBeanOperationInfo(member.getName(), "", op.getSignature(), op.getReturnType(), op.getImpact()));
        }
        filterAttributes = true;
        excludeInfo = new MBeanParameterInfo("excludeRegex", "java.lang.String", "\"" + excludes.pattern() + "\"");
        includeInfo = new MBeanParameterInfo("includeRegex", "java.lang.String", "\"" + includes.pattern() + "\"");
    }

    private void scan(final Object target, final String prefix) {
        final ClassFinder finder = new ClassFinder(Classes.ancestors(target.getClass()));

        final List<Field> fields = finder.findAnnotatedFields(Managed.class);
        for (final Field field : fields) {
            attribute(new FieldMember(field, target, prefix));
        }

        final List<Method> managed = finder.findAnnotatedMethods(Managed.class);
        for (final Method method : managed) {
            final MethodMember member = new MethodMember(method, target, prefix);
            if (!method.getName().matches("(get|is)([A-Z_].*|)")) {
                operationsMap.put(member.getName(), member);
            } else {
                attribute(new MethodMember(method, target, prefix));
            }
        }

        final List<Method> collections = finder.findAnnotatedMethods(ManagedCollection.class);
        for (final Method method : collections) {
            dynamic.add(new MethodMember(method, target, prefix));
        }
    }

    private void attribute(final Member member) {
        final Class<?> type = member.getType();

        final Managed managed = type.getAnnotation(Managed.class);
        if (managed != null) {
            try {
                String s = "";
                if (managed.append()) {
                    s = member.getName();
                }
                scan(member.get(), s);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            attributesMap.put(member.getName(), member);
        }
    }

    public Object getAttribute(final String s) throws AttributeNotFoundException, MBeanException, ReflectionException {
        try {
            final Member member = attributesMap.get(s);

            if (member == null) {
                throw new AttributeNotFoundException(s);
            }

            return member.get();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ReflectionException(e);
        }
    }

    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    }

    public AttributeList getAttributes(final String[] strings) {
        final AttributeList list = new AttributeList(strings.length);
        for (final String attribute : strings) {
            try {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public AttributeList setAttributes(final AttributeList attributeList) {
        return new AttributeList();
    }

    public Object invoke(final String operation, final Object[] args, final String[] types) throws MBeanException, ReflectionException {
        final MethodMember member = operationsMap.get(operation);
        final Method method = member.getter;

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Object value = args[i];
            final Class<?> expectedType = method.getParameterTypes()[i];
            if (value instanceof String && expectedType != Object.class) {
                final String stringValue = (String) value;
                value = PropertyEditors.getValue(expectedType, stringValue);
            }
            args[i] = value;
        }

        try {
            return method.invoke(member.target, args);
        } catch (final InvocationTargetException e) {
            throw new ReflectionException((Exception) e.getCause());
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
    }

    public MBeanInfo getMBeanInfo() {

        final List<MBeanAttributeInfo> attributes = new ArrayList<>(this.attributes);
        final List<MBeanOperationInfo> operations = new ArrayList<>(this.operations);

        for (final Member member : dynamic) {
            try {
                final ManagedCollection managedCollection = member.getAnnotation(ManagedCollection.class);
                final Collection collection = (Collection) member.get();
                for (final Object o : collection) {
                    try {
                        final Field field = o.getClass().getDeclaredField(managedCollection.key());
                        field.setAccessible(true);
                        final Object key = field.get(o);
                        final ManagedMBean bean = new ManagedMBean(o, key.toString());
                        Collections.addAll(attributes, bean.getMBeanInfo().getAttributes());
                        Collections.addAll(operations, bean.getMBeanInfo().getOperations());
                        attributesMap.putAll(bean.attributesMap);
                        operationsMap.putAll(bean.operationsMap);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        operations.sort(MBeanFeatureInfoComparator.INSTANCE);
        attributes.sort(MBeanFeatureInfoComparator.INSTANCE);

        if (filterAttributes) {
            final Iterator<MBeanAttributeInfo> iterator = attributes.iterator();
            while (iterator.hasNext()) {
                final MBeanAttributeInfo info = iterator.next();
                if (includes.matcher(info.getName()).matches()) {
                    continue;
                }
                if (excludes.matcher(info.getName()).matches()) {
                    iterator.remove();
                }
            }
        }

        return new MBeanInfo(this.getClass().getName(), "", attributes.toArray(new MBeanAttributeInfo[attributes.size()]), new MBeanConstructorInfo[0], operations.toArray(new MBeanOperationInfo[operations.size()]), EMPTY_NOTIFICATIONS);
    }

    public void setAttributesFilter(String exclude, String include) {
        if (include == null) {
            include = "";
        }
        if (exclude == null) {
            exclude = "";
        }
        includes = Pattern.compile(include);
        excludes = Pattern.compile(exclude);

        try {
            // Set the current value as the description
            final Field field = MBeanFeatureInfo.class.getDeclaredField("description");
            field.setAccessible(true);
            field.set(includeInfo, "\"" + includes.pattern() + "\"");
            field.set(excludeInfo, "\"" + excludes.pattern() + "\"");
        } catch (final Exception e) {
            // Oh well, we tried
        }
    }

    /**
     * Small utility interface used to allow polymorphing
     * of java.lang.reflect.Method and java.lang.reflect.Field
     * so that each can be treated as injection targets using
     * the same code.
     */
    public interface Member {
        Object get() throws IllegalAccessException, InvocationTargetException;

        String getName();

        Class getType();

        <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    }

    /**
     * Implementation of Member for java.lang.reflect.Method
     * Used for injection targets that are annotated methods
     */
    public static class MethodMember implements Member {
        private final Method getter;
        private final Object target;
        private final String prefix;

        public MethodMember(final Method getter, final Object target, final String prefix) {
            getter.setAccessible(true);
            this.getter = getter;
            this.target = target;
            this.prefix = prefix;
        }

        public Class getType() {
            return getter.getReturnType();
        }

        public Class getDeclaringClass() {
            return getter.getDeclaringClass();
        }

        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return getter.getAnnotation(annotationClass);
        }

        /**
         * The method name needs to be changed from "getFoo" to "foo"
         *
         * @return attribute name
         */
        public String getName() {
            final String method = getter.getName();

            final StringBuilder name = new StringBuilder(method);

            // remove 'get'
            if (method.matches("get([A-Z].*|)")) {
                name.delete(0, 3);
            }
            if (method.matches("is([A-Z].*|)")) {
                name.delete(0, 2);
            }

            if (!"".equals(prefix)) {
                if (!"".equals(name.toString())) {
                    name.insert(0, ".");
                }
                name.insert(0, prefix);
            }

            return name.toString();
        }

        public String toString() {
            return getter.toString();
        }

        public Object get() throws IllegalAccessException, InvocationTargetException {
            return getter.invoke(target);
        }
    }

    /**
     * Implementation of Member for java.lang.reflect.Field
     * Used for injection targets that are annotated fields
     */
    public static class FieldMember implements Member {
        private final Field field;
        private final Object target;
        private final String prefix;

        public FieldMember(final Field field, final Object target, final String prefix) {
            field.setAccessible(true);
            this.field = field;
            this.target = target;
            this.prefix = prefix;
        }

        public Class getType() {
            return unwrap(field.getType());
        }

        public String toString() {
            return field.toString();
        }

        public Class getDeclaringClass() {
            return field.getDeclaringClass();
        }

        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return field.getAnnotation(annotationClass);
        }

        public String getName() {
            final StringBuilder name = new StringBuilder(field.getName());

            name.setCharAt(0, Character.toUpperCase(name.charAt(0)));

            if (!"".equals(prefix)) {
                if (!"".equals(name.toString())) {
                    name.insert(0, ".");
                }
                name.insert(0, prefix);
            }

            return name.toString();
        }

        public Object get() throws IllegalAccessException {
            return unwrap(field.get(target));
        }

        public Class<?> unwrap(final Class<?> clazz) {
            if (clazz == AtomicInteger.class) {
                return int.class;
            } else if (clazz == AtomicBoolean.class) {
                return boolean.class;
            } else if (clazz == AtomicLong.class) {
                return long.class;
            } else if (clazz == AtomicReference.class) {
                return Object.class;
            } else {
                return clazz;
            }
        }

        public Object unwrap(final Object clazz) {
            if (clazz instanceof AtomicInteger) {
                return ((AtomicInteger) clazz).get();
            } else if (clazz instanceof AtomicBoolean) {
                return ((AtomicBoolean) clazz).get();
            } else if (clazz instanceof AtomicLong) {
                return ((AtomicLong) clazz).get();
            } else if (clazz instanceof AtomicReference) {
                return ((AtomicReference) clazz).get();
            } else {
                return clazz;
            }
        }
    }

    private static class MBeanFeatureInfoComparator implements Comparator<MBeanFeatureInfo> {
        private static final MBeanFeatureInfoComparator INSTANCE = new MBeanFeatureInfoComparator();

        @Override
        public int compare(final MBeanFeatureInfo o1, final MBeanFeatureInfo o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
