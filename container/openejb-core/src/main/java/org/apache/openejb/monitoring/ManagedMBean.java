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
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.management.MBeanFeatureInfo;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class ManagedMBean implements DynamicMBean {

    private final List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
    private final List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
    private final Map<String, Member> attributesMap = new HashMap<String, Member>();
    private final Map<String, MethodMember> operationsMap = new HashMap<String, MethodMember>();
    private final List<Member> dynamic = new ArrayList<Member>();

    private Pattern includes = Pattern.compile("");
    private Pattern excludes = Pattern.compile("");
    private boolean filterAttributes;
    private MBeanParameterInfo excludeInfo;
    private MBeanParameterInfo includeInfo;

    public ManagedMBean(Object managed) {
        this(managed, "");

        try {
            Method method = this.getClass().getMethod("setAttributesFilter", String.class, String.class);

            String description = "Filters the attributes that show up in the MBeanInfo." +
                    "  The exclude is applied first, then any attributes that match the " +
                    "include are re-added.  It may be required to disconnect and reconnect " +
                    "the JMX console to force a refresh of the MBeanInfo";

            excludeInfo = new MBeanParameterInfo("excludeRegex", "java.lang.String", "\"" + excludes.pattern() + "\"");
            includeInfo = new MBeanParameterInfo("includeRegex", "java.lang.String", "\"" + includes.pattern() + "\"");
            MBeanOperationInfo filterOperation = new MBeanOperationInfo("FilterAttributes", description, new MBeanParameterInfo[]{
                    excludeInfo,
                    includeInfo,
            }, "void", 3);
            operations.add(filterOperation);
            operationsMap.put(filterOperation.getName(), new MethodMember(method, this, ""));

            filterAttributes = true;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    ManagedMBean(Object managed, String prefix) {
        scan(managed, prefix);

        for (Member member : attributesMap.values()) {
            attributes.add(new MBeanAttributeInfo(member.getName(), member.getType().getName(), "", true, false, false));
        }

        for (Member member : operationsMap.values()) {
            MBeanOperationInfo op = new MBeanOperationInfo("", ((MethodMember) member).getter);
            operations.add(new MBeanOperationInfo(member.getName(), "", op.getSignature(), op.getReturnType(), op.getImpact()));
        }
        filterAttributes = true;
        excludeInfo = new MBeanParameterInfo("excludeRegex", "java.lang.String", "\"" + excludes.pattern() + "\"");
        includeInfo = new MBeanParameterInfo("includeRegex", "java.lang.String", "\"" + includes.pattern() + "\"");
    }

    private void sortAttributes(List<MBeanAttributeInfo> attributes) {
        Collections.sort(attributes, new Comparator<MBeanAttributeInfo>() {
            public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private void sortOperations(List<MBeanOperationInfo> operations) {
        Collections.sort(operations, new Comparator<MBeanOperationInfo>() {
            public int compare(MBeanOperationInfo o1, MBeanOperationInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private void scan(Object target, String prefix) {
        ClassFinder finder = new ClassFinder(target.getClass());

        List<Field> fields = finder.findAnnotatedFields(Managed.class);
        for (Field field : fields) {
            attribute(new FieldMember(field, target, prefix));
        }

        List<Method> managed = finder.findAnnotatedMethods(Managed.class);
        for (Method method : managed) {
            MethodMember member = new MethodMember(method, target, prefix);
            if (!method.getName().matches("(get|is)([A-Z_].*|)")) {
                operationsMap.put(member.getName(), member);
            } else {
                attribute(new MethodMember(method, target, prefix));
            }
        }

        List<Method> collections = finder.findAnnotatedMethods(ManagedCollection.class);
        for (Method method : collections) {
            dynamic.add(new MethodMember(method, target, prefix));
        }
    }

    private void attribute(Member member) {
        Class<?> type = member.getType();

        Managed managed = type.getAnnotation(Managed.class);
        if (managed != null) {
            try {
                String s = "";
                if (managed.append()) s = member.getName();
                scan(member.get(), s);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            attributesMap.put(member.getName(), member);
        }
    }

    public Object getAttribute(String s) throws AttributeNotFoundException, MBeanException, ReflectionException {
        try {
            Member member = attributesMap.get(s);

            if (member == null) throw new AttributeNotFoundException(s);

            return member.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectionException(e);
        }
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    }

    public AttributeList getAttributes(String[] strings) {
        AttributeList list = new AttributeList(strings.length);
        for (String attribute : strings) {
            try {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public AttributeList setAttributes(AttributeList attributeList) {
        return new AttributeList();
    }

    public Object invoke(String operation, Object[] args, String[] types) throws MBeanException, ReflectionException {
        MethodMember member = operationsMap.get(operation);
        Method method = member.getter;

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Object value = args[i];
            Class<?> expectedType = method.getParameterTypes()[i];
            if (value instanceof String && (expectedType != Object.class)) {
                String stringValue = (String) value;
                value = PropertyEditors.getValue(expectedType, stringValue);
            }
            args[i] = value;
        }

        try {
            return method.invoke(member.target, args);
        } catch (InvocationTargetException e) {
            throw new ReflectionException((Exception)e.getCause());
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    public MBeanInfo getMBeanInfo() {

        List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>(this.attributes);
        List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>(this.operations);

        for (Member member : dynamic) {
            try {
                ManagedCollection managedCollection = member.getAnnotation(ManagedCollection.class);
                Collection collection = (Collection) member.get();
                for (Object o : collection) {
                    try {
                        Field field = o.getClass().getDeclaredField(managedCollection.key());
                        field.setAccessible(true);
                        Object key = field.get(o);
                        ManagedMBean bean = new ManagedMBean(o, key.toString());
                        for (MBeanAttributeInfo info : bean.getMBeanInfo().getAttributes()) {
                            attributes.add(info);
                        }
                        for (MBeanOperationInfo info : bean.getMBeanInfo().getOperations()) {
                            operations.add(info);
                        }
                        attributesMap.putAll(bean.attributesMap);
                        operationsMap.putAll(bean.operationsMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sortOperations(operations);
        sortAttributes(attributes);

        if (filterAttributes) {
            Iterator<MBeanAttributeInfo> iterator = attributes.iterator();
            while (iterator.hasNext()) {
                MBeanAttributeInfo info = iterator.next();
                if (includes.matcher(info.getName()).matches()) continue;
                if (excludes.matcher(info.getName()).matches()) iterator.remove();
            }
        }

        return new MBeanInfo(this.getClass().getName(), "", attributes.toArray(new MBeanAttributeInfo[0]), new MBeanConstructorInfo[0], operations.toArray(new MBeanOperationInfo[0]), new MBeanNotificationInfo[0]);
    }

    public void setAttributesFilter(String exclude, String include) {
        if (include == null) include = "";
        if (exclude == null) exclude = "";
        includes = Pattern.compile(include);
        excludes = Pattern.compile(exclude);

        try {
            // Set the current value as the description
            Field field = MBeanFeatureInfo.class.getDeclaredField("description");
            field.setAccessible(true);
            field.set(includeInfo, "\"" + includes.pattern() + "\"");
            field.set(excludeInfo, "\"" + excludes.pattern() + "\"");
        } catch (Exception e) {
            // Oh well, we tried
        }
    }

    /**
     * Small utility interface used to allow polymorphing
     * of java.lang.reflect.Method and java.lang.reflect.Field
     * so that each can be treated as injection targets using
     * the same code.
     */
    public static interface Member {
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

        public MethodMember(Method getter, Object target, String prefix) {
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

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return getter.getAnnotation(annotationClass);
        }

        /**
         * The method name needs to be changed from "getFoo" to "foo"
         *
         * @return
         */
        public String getName() {
            String method = getter.getName();

            StringBuilder name = new StringBuilder(method);
            
            // remove 'get'
            if (method.matches("get([A-Z].*|)")) name.delete(0, 3);
            if (method.matches("is([A-Z].*|)")) name.delete(0, 2);

            if (!"".equals(prefix)) {
                if (!"".equals(name.toString())) name.insert(0, ".");
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

        public FieldMember(Field field, Object target, String prefix) {
            field.setAccessible(true);
            this.field = field;
            this.target = target;
            this.prefix = prefix;
        }

        public Class getType() {
            return field.getType();
        }

        public String toString() {
            return field.toString();
        }

        public Class getDeclaringClass() {
            return field.getDeclaringClass();
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return field.getAnnotation(annotationClass);
        }

        public String getName() {
            StringBuilder name = new StringBuilder(field.getName());

            name.setCharAt(0, Character.toUpperCase(name.charAt(0)));

            if (!"".equals(prefix)) {
                if (!"".equals(name.toString())) name.insert(0, ".");
                name.insert(0, prefix);
            }

            return name.toString();
        }

        public Object get() throws IllegalAccessException {
            return field.get(target);
        }
    }

}
