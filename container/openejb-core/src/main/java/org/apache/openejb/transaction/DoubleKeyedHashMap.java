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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.transaction;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * <b>Really</b> stupid implementation of a double keyed map.
 *
 * @version $Rev$ $Date$
 */
public final class DoubleKeyedHashMap {
    private final Map map = new HashMap();

    public Object put(Object key1, Object key2, Object value) {
        return map.put(new org.apache.openejb.transaction.DoubleKeyedHashMap.Key(key1, key2), value);
    }

    public Object get(Object key1, Object key2) {
        return map.get(new org.apache.openejb.transaction.DoubleKeyedHashMap.Key(key1, key2));
    }

    public Object remove(Object key1, Object key2) {
        return map.remove(new org.apache.openejb.transaction.DoubleKeyedHashMap.Key(key1, key2));
    }

    public Collection values() {
        return map.values();
    }

    public void clear() {
        map.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    private final static class Key {
        private final Object part1;
        private final Object part2;

        public Key(Object part1, Object part2) {
            this.part1 = part1;
            this.part2 = part2;
        }

        public int hashCode() {
            return part1.hashCode() ^ part2.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof org.apache.openejb.transaction.DoubleKeyedHashMap.Key) {
                org.apache.openejb.transaction.DoubleKeyedHashMap.Key other = (org.apache.openejb.transaction.DoubleKeyedHashMap.Key) obj;
                return this.part1.equals(other.part1) && this.part2.equals(other.part2);
            } else {
                return false;
            }
        }
    }
}
