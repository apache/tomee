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
package org.apache.openejb.jee;

import java.util.Locale;
import java.util.Collection;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class LocalCollection<V> extends KeyedCollection<String,V> {
    public LocalCollection() {
    }

    public LocalCollection(KeyExtractor<String, ? super V> keyExtractor) {
        super(keyExtractor);
    }

    public LocalCollection(Collection<? extends V> c) {
        super(c);
    }

    public LocalCollection(int initialCapacity) {
        super(initialCapacity);
    }

    public V getLocal() {
        String lang = Locale.getDefault().getLanguage();
        Map<String,V> map = toMap();
        return (map.get(lang) != null ? map.get(lang) : map.get(null));
    }
}
