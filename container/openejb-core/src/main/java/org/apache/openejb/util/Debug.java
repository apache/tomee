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
package org.apache.openejb.util;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

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
        Map<String, Object> map = new TreeMap<String, Object>();
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
        for (Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Object> entry = iterator.next();
            out.println(entry.getKey() + "=" + entry.getValue().getClass().getName());
        }
        return map;
    }
}
