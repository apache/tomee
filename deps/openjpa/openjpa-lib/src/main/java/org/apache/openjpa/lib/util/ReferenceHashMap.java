/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.Reference;

/**
 * Map in which the key, value, or both may be weak/soft references.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class ReferenceHashMap
    extends org.apache.commons.collections.map.ReferenceMap
    implements ReferenceMap, SizedMap {

    private int _maxSize = Integer.MAX_VALUE;

    public ReferenceHashMap(int keyType, int valueType) {
        super(toReferenceConstant(keyType), toReferenceConstant(valueType));
    }

    public ReferenceHashMap(int keyType, int valueType, int capacity,
        float loadFactor) {
        super(toReferenceConstant(keyType), toReferenceConstant(valueType),
            capacity, loadFactor);
    }

    /**
     * Concver our reference constants to Apache's.
     */
    private static int toReferenceConstant(int type) {
        switch (type) {
            case ReferenceMap.HARD:
                return org.apache.commons.collections.map.ReferenceMap. HARD;
            case ReferenceMap.SOFT:
                return org.apache.commons.collections.map.ReferenceMap. SOFT;
            default:
                return org.apache.commons.collections.map.ReferenceMap. WEAK;
        }
    }

    public int getMaxSize() {
        return _maxSize;
    }

    public void setMaxSize(int maxSize) {
        _maxSize = (maxSize < 0) ? Integer.MAX_VALUE : maxSize;
        if (_maxSize != Integer.MAX_VALUE)
            removeOverflow(_maxSize);
    }

    public boolean isFull() {
        return size() >= _maxSize;
    }

    public void overflowRemoved(Object key, Object value) {
    }

    public void valueExpired(Object key) {
    }

    public void keyExpired(Object value) {
    }

    public void removeExpired() {
        purge();
    }

    /**
     * Remove any entries over max size.
     */
    private void removeOverflow(int maxSize) {
        Object key;
        while (size() > maxSize) {
            key = keySet().iterator().next();
            overflowRemoved(key, remove(key));
        }
    }

    protected void addMapping(int hashIndex, int hashCode, Object key,
        Object value) {
        if (_maxSize != Integer.MAX_VALUE)
            removeOverflow(_maxSize - 1);
        super.addMapping(hashIndex, hashCode, key, value);
    }

    protected HashEntry createEntry(HashEntry next, int hashCode, Object key,
        Object value) {
        return new AccessibleEntry(this, next, hashCode, key, value);
    }

    protected void purge(Reference ref) {
        // the logic for this method is taken from the original purge method
        // we're overriding, with added logic to track the expired key/value
        int index = hashIndex(ref.hashCode(), data.length);
        AccessibleEntry entry = (AccessibleEntry) data[index];
        AccessibleEntry prev = null;
        Object key = null, value = null;
        while (entry != null) {
            if (purge(entry, ref)) {
                if (isHard(keyType))
                    key = entry.key();
                else if (isHard(valueType))
                    value = entry.value();

                if (prev == null)
                    data[index] = entry.nextEntry();
                else
                    prev.setNextEntry(entry.nextEntry());
                size--;
                break;
            }
            prev = entry;
            entry = entry.nextEntry();
        }

        if (key != null)
            valueExpired(key);
        else if (value != null)
            keyExpired(value);
    }

    /**
     * See the code for <code>ReferenceMap.ReferenceEntry.purge</code>.
     */
    private boolean purge(AccessibleEntry entry, Reference ref) {
        boolean match = (!isHard(keyType) && entry.key() == ref)
            || (!isHard(valueType) && entry.value() == ref);
        if (match) {
            if (!isHard(keyType))
                ((Reference) entry.key()).clear();
            if (!isHard(valueType))
                ((Reference) entry.value()).clear();
            else if (purgeValues)
                entry.nullValue();
        }
        return match;
    }

    private static boolean isHard(int type) {
        return type == org.apache.commons.collections.map. ReferenceMap.HARD;
    }

    protected void doWriteObject(ObjectOutputStream out) throws IOException {
        out.writeInt(_maxSize);
        super.doWriteObject(out);
    }

    protected void doReadObject(ObjectInputStream in)
        throws ClassNotFoundException, IOException {
        _maxSize = in.readInt();
        super.doReadObject(in);
    }

    /**
     * Extension of the base entry type that allows our outer class to access
     * protected state.
     */
    private static class AccessibleEntry extends ReferenceEntry {

        public AccessibleEntry(org.apache.commons.collections.map.
            AbstractReferenceMap map, HashEntry next,
            int hashCode, Object key, Object value) {
            super(map, next, hashCode, key, value);
        }

        public Object key() {
            return key;
        }

        public Object value() {
            return value;
        }

        public void nullValue() {
            value = null;
        }

        public AccessibleEntry nextEntry() {
            return (AccessibleEntry) next;
        }

        public void setNextEntry(AccessibleEntry next) {
            this.next = next;
        }
    }
}
