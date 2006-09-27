/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;

/**
 * @version $Revision$ $Date$
 */
public class KeyedList<K,V> {
    protected final Map<K, V> map = new HashMap<K, V>();
    private final Class type;
    private final java.lang.reflect.Method key;

    public KeyedList(Class type, String key) {
        String methodName = "get" + ucfirst(key);
        try {
            this.type = type;
            this.key = type.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Invalid object key method "+methodName, e);
        }
    }

    public V[] toArray() {
        V[] ts = (V[]) java.lang.reflect.Array.newInstance(type, 0);
        return map.values().toArray(ts);
    }

    public void set(V[] values) {
        map.clear();
        for (V v : values) {
            add(v);
        }
    }

    public void add(V v) {
        try {
            map.put((K)key.invoke(v), v);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Error invoking "+key.getName(), e.getTargetException());
        }
    }

    public Map<K,V> get() {
        return map;
    }

    public V get(String key){
        return map.get(key);
    }

    private String ucfirst(String key) {
        StringBuffer sb = new StringBuffer(key);
        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

}
