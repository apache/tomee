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

import java.util.HashMap;
import java.lang.reflect.Array;

/**
 * @version $Rev$ $Date$
 */
public class Classes {

    private static final HashMap<String, Class> primitives = new HashMap();
    static {
        Classes.primitives.put("boolean", boolean.class);
        Classes.primitives.put("byte", byte.class);
        Classes.primitives.put("char", char.class);
        Classes.primitives.put("short", short.class);
        Classes.primitives.put("int", int.class);
        Classes.primitives.put("long", long.class);
        Classes.primitives.put("float", float.class);
        Classes.primitives.put("double", double.class);
    }

    public static Class forName(String string, ClassLoader classLoader) throws ClassNotFoundException {
        int arrayDimentions = 0;
        while (string.endsWith("[]")){
            string = string.substring(0, string.length() - 2);
            arrayDimentions++;
        }

        Class clazz = primitives.get(string);

        if (clazz == null) clazz = Class.forName(string, true, classLoader);

        if (arrayDimentions == 0){
            return clazz;
        }
        return Array.newInstance(clazz, new int[arrayDimentions]).getClass();
    }

    public static String packageName(Class clazz){
        return packageName(clazz.getName());
    }

    public static String packageName(String clazzName){
        int i = clazzName.lastIndexOf('.');
        if (i > 0){
            return clazzName.substring(0, i);
        } else {
            return "";
        }
    }
}
