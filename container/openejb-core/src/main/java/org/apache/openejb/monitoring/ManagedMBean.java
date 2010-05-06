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

import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.Attribute;
import javax.management.InvalidAttributeValueException;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @version $Rev$ $Date$
*/
class ManagedMBean implements DynamicMBean {

    private final List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
    private final Map<String, Member> map = new HashMap<String, Member>();

    ManagedMBean(Object managed) {
        scan(managed, "");

        for (Member member : map.values()) {
            attributes.add(new MBeanAttributeInfo(member.getName(), member.getType().getName(), "", true, false, false));
        }

        Collections.sort(attributes, new Comparator<MBeanAttributeInfo>(){
            public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private void scan(Object managed, String prefix) {
        ClassFinder finder = new ClassFinder(managed.getClass());

        List<Field> fields = finder.findAnnotatedFields(Managed.class);
        for (Field field : fields) {
            scan(new FieldMember(field, managed, prefix));
        }

        List<Method> methods = finder.findAnnotatedMethods(Managed.class);
        for (Method method : methods) {
            scan(new MethodMember(method, managed, prefix));
        }
    }

    private void scan(Member member) {
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
            map.put(member.getName(), member);
        }
    }

    public Object getAttribute(String s) throws AttributeNotFoundException, MBeanException, ReflectionException {
        try {
            Member member = map.get(s);

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

    public Object invoke(String s, Object[] objects, String[] strings) throws MBeanException, ReflectionException {
        return null;
    }

    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(this.getClass().getName(), "The description", attributes.toArray(new MBeanAttributeInfo[0]), new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
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

        /**
         * The method name needs to be changed from "getFoo" to "foo"
         *
         * @return
         */
        public String getName() {
            StringBuilder name = new StringBuilder(getter.getName());

            // remove 'get'
            name.delete(0, 3);

            if (!"".equals(prefix)){
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

        public String getName() {
            StringBuilder name = new StringBuilder(field.getName());

            name.setCharAt(0, Character.toUpperCase(name.charAt(0)));

            if (!"".equals(prefix)){
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
