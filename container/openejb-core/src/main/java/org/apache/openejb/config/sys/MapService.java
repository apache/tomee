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
package org.apache.openejb.config.sys;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Maps")
public class MapService extends AbstractService implements Map<Object, Object> {
    @Override
    public int size() {
        return getProperties().size();
    }

    @Override
    public boolean isEmpty() {
        return getProperties().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getProperties().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getProperties().containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return getProperties().get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return getProperties().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return getProperties().remove(key);
    }

    @Override
    public void putAll(Map<? extends Object, ? extends Object> m) {
        getProperties().putAll(m);
    }

    @Override
    public void clear() {
        getProperties().clear();
    }

    @Override
    public Set<Object> keySet() {
        return getProperties().keySet();
    }

    @Override
    public Collection<Object> values() {
        return getProperties().values();
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return getProperties().entrySet();
    }
}
