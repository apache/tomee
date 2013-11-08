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
package org.apache.openjpa.lib.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.openjpa.lib.util.SizedMap;

/**
 * An implementation of {@link SizedMap} that uses JDK1.5 concurrency primitives
 *
 * @since 1.1.0
 */
public class SizedConcurrentHashMap
    extends NullSafeConcurrentHashMap
    implements SizedMap, ConcurrentMap, Serializable {

    private int maxSize;

    /**
     * @param size the maximum size of this map. If additional elements are
     * put into the map, overflow will be removed via calls to
     * {@link #overflowRemoved}.
     * @param load the load factor for the underlying map
     * @param concurrencyLevel the concurrency level for the underlying map
     *
     * @see ConcurrentHashMap
     */
    public SizedConcurrentHashMap(int size, float load, int concurrencyLevel) {
        super(size, load, concurrencyLevel);
        setMaxSize(size);
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        if (maxSize != Integer.MAX_VALUE)
            removeOverflow(true);
        return super.putIfAbsent(key, value);
    }

    @Override
    public Object put(Object key, Object value) {
        if (maxSize != Integer.MAX_VALUE)
            removeOverflow(true);
        return super.put(key, value);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int max) {
        if (max < 0)
            throw new IllegalArgumentException(String.valueOf(max));
        maxSize = max;

        removeOverflow(false);
    }

    /**
     * Equivalent to <code>removeOverflow(false)</code>.
     */
    protected void removeOverflow() {
        removeOverflow(false);
    }

    /**
     * Removes overflow. If <code>forPut</code> is <code>true</code>, then
     * this uses <code>size() + 1</code> when computing size.
     */
    protected void removeOverflow(boolean forPut) {
        int sizeToCompareTo = forPut ? maxSize - 1 : maxSize;
        while (size() > sizeToCompareTo) {
            Entry entry = removeRandom();
            // if removeRandom() returns null, break out of the loop. Of course,
            // since we're not locking, the size might not actually be null
            // when we do this. But this prevents weird race conditions from
            // putting this thread into more loops.
            if (entry == null)
                break;
            overflowRemoved(entry.getKey(), entry.getValue());
        }
    }

    public boolean isFull() {
        return size() >= maxSize;
    }

    /**
     * This implementation does nothing.
     */
    public void overflowRemoved(Object key, Object value) {
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(maxSize);
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        maxSize = in.readInt();
    }
}
