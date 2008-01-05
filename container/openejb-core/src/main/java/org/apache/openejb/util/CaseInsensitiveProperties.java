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

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class CaseInsensitiveProperties extends Properties {
    public CaseInsensitiveProperties() {
    }

    public CaseInsensitiveProperties(Properties defaults) {
        if (!(defaults instanceof CaseInsensitiveProperties)) {
            super.defaults = new CaseInsensitiveProperties();
            super.defaults.putAll(defaults);
        }
    }

    public boolean containsKey(Object key) {
        return getKey((String) key) != null;
    }

    public Object get(Object key) {
        return super.get(normalize(key));
    }

    public Object put(Object key, Object value) {
        return super.put(normalize(key), value);
    }

    public String getProperty(String key) {
        Object property = get(key);
        if (property != null && property instanceof String){
            return (String) property;
        }
        if (defaults != null) {
            return defaults.getProperty(key);
        }
        return null;
    }

    private Object normalize(Object key){
        if (key instanceof String) {
            String normalized = getKey((String)key);
            key = (normalized != null) ? normalized : key;
        }
        return key;
    }

    private String getKey(String property){
        if (super.containsKey(property)){
            return property;
        }

        for (Object o : keySet()) {
            String key = (String) o;
            if (key.equalsIgnoreCase(property)) return key;
        }

        if (defaults != null) {
            CaseInsensitiveProperties defaults = (CaseInsensitiveProperties) this.defaults;
            return defaults.getKey(property);
        }
        return null;
    }
}
