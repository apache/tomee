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

import org.junit.Assert;

public final class TypeInfoTestUtil {
    private TypeInfoTestUtil() {
    }

    public static void assertEqual(final InfoObject expected, final InfoObject actual) throws Exception {
        assertEqual(expected, actual, false);
    }

    public static void assertEqual(final InfoObject expected, final InfoObject actual, final boolean printExpected) throws Exception {
        final List<String> messages = new ArrayList<String>();
        diff(null, expected, actual, messages);
        if (!messages.isEmpty()) {
            if (printExpected) {
                System.out.println("************ Actual " + expected.getClass().getSimpleName() + " ************");
                dump("expected", actual);
                System.out.println("**********************************************");
                System.out.println();
            }

            final StringBuilder msg = new StringBuilder();
            for (final String message : messages) {
                if (msg.length() != 0) msg.append("\n");
                msg.append(message);
            }
            msg.insert(0, expected.getClass().getSimpleName() + " is different:\n");
            Assert.fail(msg.toString());
        }
    }

    public static void diff(final String name, final Object expected, final Object actual, final List<String> messages) throws Exception {
        for (final Field field : expected.getClass().getFields()) {
            final String fieldName = name == null ? field.getName() : name + "." + field.getName();
            final Object expectedValue = field.get(expected);
            final Object actualValue = field.get(actual);
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

    private static void diffMap(final String name, final Map<Object, Object> expected, final Map<Object, Object> actual, final List<String> message) throws Exception {
        // Added
        Set<Object> keys = new HashSet<Object>(actual.keySet());
        keys.removeAll(expected.keySet());
        for (final Object key : keys) {
            message.add("A " + name + "[" + key + "]");
        }

        // Removed
        keys = new HashSet<Object>(expected.keySet());
        keys.removeAll(actual.keySet());
        for (final Object key : keys) {
            message.add("R " + name + "[" + key + "]");
        }

        // Changed
        for (final Object key : expected.keySet()) {
            final Object expectValue = expected.get(key);
            final Object actualValue = actual.get(key);
            if (actualValue != null) {
                diff(name + "[" + key + "]", expectValue, actualValue, message);
            }
        }
    }

    private static void diffSimple(final String name, final Object expected, final Object actual, final List<String> messages) {
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

    public static void dump(final String name, final Object value) throws Exception {
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
            for (final Field field : value.getClass().getFields()) {
                final String fieldName = name == null ? field.getName() : name + "." + field.getName();
                final Object fieldValue = field.get(value);
                if (fieldValue instanceof Map) {
                    //noinspection unchecked
                    dumpMap(fieldName, (Map) fieldValue);
                } else {
                    dump(fieldName, fieldValue);
                }
            }
        }
    }

    private static void dumpMap(final String name, final Map<Object, Object> map) throws Exception {
        for (final Map.Entry<Object, Object> entry : map.entrySet()) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();

            if (isSimpleValue(key) && isSimpleValue(value)) {
                System.out.println(name + ".put(" + getSimpleValue(key) + ", " + getSimpleValue(value) + ");");
            } else {
                final String indent = name.substring(0, getIndentSize(name));

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

    private static boolean isSimpleValue(final Object value) {
        return value == null || value instanceof Boolean || value instanceof Number || value instanceof String || value instanceof QName;
    }

    private static String getSimpleValue(final Object value) {
        if (!isSimpleValue(value))
            throw new IllegalArgumentException("Value is not a simple type " + value.getClass().getName());

        final String stringValue;
        if (value == null) {
            stringValue = "null";
        } else if (value instanceof Boolean) {
            stringValue = value.toString();
        } else if (value instanceof Number) {
            stringValue = value.toString();
        } else if (value instanceof QName) {
            final QName qname = (QName) value;
            stringValue = "new QName(\"" + qname.getNamespaceURI() + "\", \"" + qname.getLocalPart() + "\")";
        } else {
            stringValue = "\"" + value + "\"";
        }
        return stringValue;
    }

    private static String getTypeDecl(final Class type, final String name) {
        final int indentSize = getIndentSize(name);
        return name.substring(0, indentSize) + type.getSimpleName() + " " + name.substring(indentSize);
    }

    private static int getIndentSize(final String name) {
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) != ' ') {
                return i;
            }
        }
        return 0;
    }
}
