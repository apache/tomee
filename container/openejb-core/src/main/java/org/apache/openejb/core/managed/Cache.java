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
package org.apache.openejb.core.managed;

public interface Cache<K, V> {
    /**
     * Gets the listener for cache events.
     */
    CacheListener<V> getListener();

    /**
     * Sets the listener for cache events.  This should be called by the
     * container before using the cache.
     */
    void setListener(CacheListener<V> listener);

    /**
     * Add a new entry to the cache.  The entry is marked checked-out and can
     * not be accessed again until checked-in.
     *
     * @IllegalStateException if an value is already associated with the key
     */
    void add(K key, V value);

    /**
     * Marks the entry checked-out, so this entry can not be accessed until
     * checked-in.
     *
     * @throws IllegalStateException if the entry is already checked out.
     * @throws Exception if an entry is loaded and the afterLoad method threw an
     * exception
     */
    V checkOut(K key) throws Exception;

    /**
     * Marks the entry available, so it can be accessed again.
     *
     * @throws IllegalStateException if the entry is not checked out.
     */
    void checkIn(K key);

    /**
     * Removes the entry from the cache.
     */
    V remove(K key);

    /**
     * Removes all of th entries that match the specified filter.
     */
    void removeAll(CacheFilter<V> filter);

    /**
     * Callback listener for cache events.
     */
    public interface CacheListener<V> {
        /**
         * Called after an entry is loaded from a store.
         *
         * @throws Exception if there is a problem with the instance
         */
        void afterLoad(V value) throws Exception;

        /**
         * Called before an entry is written to a store.
         *
         * @throws Exception if there is a problem with the instance
         */
        void beforeStore(V value) throws Exception;

        /**
         * Called when an instance has been removed from the cache due to a
         * time-out.
         */
        void timedOut(V value);
    }

    /**
     * CacheFileter is used to select values to remove during a removeAll
     * invocation.
     */
    public interface CacheFilter<V> {
        /**
         * True if the filter matches the value.
         */
        boolean matches(V v);
    }
}
