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
package org.apache.openejb.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.lang.reflect.Field;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * @version $Rev$ $Date$
 */
public class Debug {

    public static String printStackTrace(Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(baos));
        return new String(baos.toByteArray());
    }

    public static Map<String,Object> contextToMap(Context context) throws NamingException {
        Map<String, Object> map = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        contextToMap(context, "", map);
        return map;
    }

    public static void contextToMap(Context context, String baseName, Map<String,Object> results) throws NamingException {
        NamingEnumeration<Binding> namingEnumeration = context.listBindings("");
        while (namingEnumeration.hasMoreElements()) {
            Binding binding = namingEnumeration.nextElement();
            String name = binding.getName();
            String fullName = baseName + name;
            Object object = binding.getObject();
            results.put(fullName, object);
            if (object instanceof Context) {
                contextToMap((Context) object, fullName + "/", results);
            }
        }
    }

    public static Map<String,Object> printContext(Context context) throws NamingException {
        return printContext(context, System.out);
    }

    public static Map<String,Object> printContext(Context context, PrintStream out) throws NamingException {
        Map<String, Object> map = contextToMap(context);
        for (Entry<String, Object> entry : map.entrySet()) {
            out.println(entry.getKey() + "=" + entry.getValue().getClass().getName());
        }
        return map;
    }

    public static Map<String,Object> printContextValues(Context context) throws NamingException {
        return printContextValues(context, System.out);
    }

    public static Map<String,Object> printContextValues(Context context, PrintStream out) throws NamingException {
        Map<String, Object> map = contextToMap(context);
        for (Entry<String, Object> entry : map.entrySet()) {
            out.println(entry.getKey() + "=" + entry.getValue());
        }
        return map;
    }

    public static List<Field> getFields(Class clazz){
        if (clazz == null) return Collections.EMPTY_LIST;

        List<Field> fields = new ArrayList<Field>();

        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        fields.addAll(getFields(clazz.getSuperclass()));

        return fields;
    }
}
