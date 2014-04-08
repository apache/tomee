/**
 *
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
package org.apache.openejb.server.axis.assembler;

import org.apache.openejb.assembler.classic.InfoObject;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.lang.reflect.Field;

import junit.framework.Assert;

public final class TypeInfoTestUtil {
    private TypeInfoTestUtil() {
    }

    public static void assertEqual(InfoObject expected, InfoObject actual) throws Exception {
        assertEqual(expected, actual, false);
    }

    public static void assertEqual(InfoObject expected, InfoObject actual, boolean printExpected) throws Exception {
        List<String> messages = new ArrayList<String>();
        diff(null, expected, actual, messages);
        if (!messages.isEmpty()) {
            if (printExpected) {
                System.out.println("************ Actual " + expected.getClass().getSimpleName() + " ************");
                dump("expected", actual);
                System.out.println("**********************************************");
                System.out.println();
            }

            StringBuilder msg = new StringBuilder();
            for (String message : messages) {
                if (msg.length() != 0) msg.append("\n");
                msg.append(message);
            }
            msg.insert(0, expected.getClass().getSimpleName() + " is different:\n");
            Assert.fail(msg.toString());
        }
    }

    public static void diff(String name, Object expected, Object actual, List<String> messages) throws Exception {
        for (Field field : expected.getClass().getFields()) {
            String fieldName = name == null ? field.getName() : name + "." + field.getName();
            Object expectedValue = field.get(expected);
            Object actualValue = field.get(actual);
            if (expectedValue instanceof InfoObject) {
                diff(fieldName, expectedValue, actualValue, messages);
            } else if (expectedValue instanceof Map) {
                //noinspection unchecked
                diffMap(fieldName, (Map) expectedValue, (Map) actualValue, messages);
            } else {
                diffSimple(fieldName, expectedValue, actualValue, messages);
            }
        }
    }

    private static void diffMap(String name, Map<Object,Object> expected, Map<Object,Object> actual, List<String> message) throws Exception {
        // Added
        Set<Object> keys = new HashSet<Object>(actual.keySet());
        keys.removeAll(expected.keySet());
        for (Object key : keys) {
            message.add("A " + name + "[" + key + "]");
        }

        // Removed
        keys = new HashSet<Object>(expected.keySet());
        keys.removeAll(actual.keySet());
        for (Object key : keys) {
            message.add("R " + name + "[" + key + "]");
        }

        // Changed
        for (Object key : expected.keySet()) {
            Object expectValue = expected.get(key);
            Object actualValue = actual.get(key);
            if (actualValue != null) {
                diff(name + "[" + key + "]", expectValue, actualValue, message);
            }
        }
    }

    private static void diffSimple(String name, Object expected, Object actual, List<String> messages) {
        boolean changed = true;
        if (expected == null) {
            if (actual == null) changed = false;
        } else {
            if (expected.equals(actual)) changed = false;
        }

        if (changed) {
            messages.add("C " + name + ": " + expected + " ==> " + actual);
        }
    }

    public static void dump(String name, Object value) throws Exception {
        if (name == null) throw new NullPointerException("name is null");

        if (isSimpleValue(value)) {
            if (name.indexOf('.') > 0) {
                System.out.println(name + " = " + getSimpleValue(value) + ";");
            } else {
                if (value == null) throw new NullPointerException("value is null");
                System.out.println(getTypeDecl(value.getClass(), name) + " = " + getSimpleValue(value) + ";");
            }
        } else {
            System.out.println(getTypeDecl(value.getClass(), name) + " = new " + value.getClass().getSimpleName() + "();");
            for (Field field : value.getClass().getFields()) {
                String fieldName = name == null ? field.getName() : name + "." + field.getName();
                Object fieldValue = field.get(value);
                if (fieldValue instanceof Map) {
                    //noinspection unchecked
                    dumpMap(fieldName, (Map) fieldValue);
                } else {
                    dump(fieldName, fieldValue);
                }
            }
        }
    }

    private static void dumpMap(String name, Map<Object,Object> map) throws Exception {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (isSimpleValue(key) && isSimpleValue(value)) {
                System.out.println(name + ".put(" + getSimpleValue(key) + ", " + getSimpleValue(value) + ");");
            } else {
                String indent = name.substring(0, getIndentSize(name));

                String baseName;
                if (value instanceof InfoObject) {
                    baseName = value.getClass().getSimpleName();
                    if (baseName.endsWith("Info")) baseName = baseName.substring(0, baseName.length() - 4);
                    baseName = Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1);
                } else {
                    baseName = name.substring(indent.length()).replace('.', '_');
                    if (baseName.endsWith("Key")) baseName = baseName.substring(0, baseName.length() - 3);
                }

                System.out.println(indent + "{");
                dump(indent + "    " + baseName + "Key", key);
                System.out.println();
                dump(indent + "    " + baseName, value);
                System.out.println();
                System.out.println("    " + name + ".put(" + baseName + "Key, " + baseName + ");");
                System.out.println(indent + "}");
            }
        }
    }

    private static boolean isSimpleValue(Object value) {
        return value == null || value instanceof Boolean || value instanceof Number || value instanceof String|| value instanceof QName;
    }

    private static String getSimpleValue(Object value) {
        if (!isSimpleValue(value)) throw new IllegalArgumentException("Value is not a simple type " + value.getClass().getName());

        String stringValue;
        if (value == null) {
            stringValue = "null";
        } else if (value instanceof Boolean) {
            stringValue = value.toString();
        } else if (value instanceof Number) {
            stringValue = value.toString();
        } else if (value instanceof QName) {
            QName qname = (QName) value;
            stringValue = "new QName(\"" + qname.getNamespaceURI() + "\", \"" + qname.getLocalPart() + "\")";
        } else {
            stringValue = "\"" + value + "\"";
        }
        return stringValue;
    }

    private static String getTypeDecl(Class type, String name) {
        int indentSize = getIndentSize(name);
        return name.substring(0, indentSize) + type.getSimpleName() + " " + name.substring(indentSize);
    }

    private static int getIndentSize(String name) {
        for (int i = 0; i < name.length(); i++) {
             if (name.charAt(i) != ' ') {
                 return i;
             }
        }
        return 0;
    }
}
