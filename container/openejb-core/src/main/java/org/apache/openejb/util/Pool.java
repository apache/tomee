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

import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Any successful pop() call requires a corresponding push() or discard() call.
 * <p/>
 * A pop() call that returns null is considered successful.  A null indicates
 * that the calling code has a permit to create a poolable object and call
 * {@link Pool#push(Object)}.  This is the only situation in which that method
 * may be called.
 * <p/>
 * To simply fill the pool without a corresponding pop(), the add() method
 * must be used.  This method will attempt to aquire a permit to add to the pool.
 *
 * @version $Rev$ $Date$
 */
public class Pool<T> {

    private final LinkedList<Entry> pool = new LinkedList<Entry>();
    private final Semaphore maxPolicy;
    private final Semaphore minPolicy;
    private final int max;

    public Pool(int max, int min, boolean strict) {
        this.max = max;
        this.minPolicy = new Semaphore(min);
        if (strict) {
            this.maxPolicy = new Semaphore(max);
        } else {
            this.maxPolicy = null;
        }
    }

    /**
     * Any successful pop() call requires a corresponding push() or discard() call
     * <p/>
     * A pop() call that returns null is considered successful.
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws IllegalStateException if a permit could not be acquired
     */
    public Entry<T> pop(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (maxPolicy != null) {
            if (!maxPolicy.tryAcquire(timeout, unit)) throw new TimeoutException("Waited " + timeout + " " + unit);
        }

        Entry<T> entry = null;
        while (entry == null) {

            synchronized (pool) {
                try {
                    entry = pool.removeFirst();
                } catch (NoSuchElementException e) {
                    return null;
                }
            }

            final T obj = entry.soft.get();

            if (obj != null) {

                final boolean notBusy = entry.active.compareAndSet(null, obj);

                if (notBusy) return entry;
            }

            entry = null;
        }

        return null;
    }

    /**
     * Attempt to aquire a permit to add the object to the pool.
     *
     * @param obj
     * @return true of the item as added
     */
    public boolean add(T obj) {
        return (maxPolicy == null || maxPolicy.tryAcquire()) && push(obj);
    }

    /**
     * Never call this method without having successfully called
     * {@link #pop(long, java.util.concurrent.TimeUnit)} beforehand.
     * <p/>
     * Failure to do so will increase the max pool size by one.
     *
     * @param obj
     * @return
     */
    public boolean push(T obj) {
        return push(new Entry<T>(obj));
    }

    /**
     * Never call this method without having successfully called
     * {@link #pop(long, java.util.concurrent.TimeUnit)} beforehand.
     * <p/>
     * Failure to do so will increase the max pool size by one.
     *
     * @param entry
     * @return true of the item as added
     */
    public boolean push(Entry<T> entry) {

        boolean added = false;

        if (entry != null) {
            final T obj = entry.active.getAndSet(null);

            if (entry.hard.get() == null && minPolicy.tryAcquire()) {
                entry.hard.set(obj);
                synchronized (pool) {
                    if (pool.size() < max) {
                        pool.addFirst(entry);
                        added = true;
                    }
                }
                if (!added) {
                    minPolicy.release();
                }
            } else {
                synchronized (pool) {
                    if (pool.size() < max) {
                        pool.addLast(entry);
                        added = true;
                    }
                }
            }
        }

        if (maxPolicy != null) maxPolicy.release();

        return added;
    }

    /**
     * Used when a call to pop() was made that returned null
     * indicating that the caller has a permit to create an
     * object for this pool, but the caller will not be exercising
     * that permit and wishes intstead to return "null" to the pool.
     */
    public void discard() {
        discard(null);
    }

    public void discard(Entry<T> entry) {
        if (entry != null) {
            final T obj = entry.active.getAndSet(null);

            if (entry.hard.compareAndSet(obj, null)) {
                minPolicy.release();
            }
        }

        if (maxPolicy != null) maxPolicy.release();
    }

    public static class Entry<T> {
        private final SoftReference<T> soft;
        private final AtomicReference<T> hard = new AtomicReference<T>();

        // Added this so the soft reference isn't collected
        // after the Entry instance is returned from a "pop" method
        // Also acts as an "inUse" boolean
        private final AtomicReference<T> active = new AtomicReference<T>();

        /**
         * Constructor is private so that it is impossible for an Entry object
         * to exist without there being a corresponding permit issued for the
         * object wrapped by this Entry.
         * <p/>
         * This helps ensure that when an Entry is returned to the pool it is
         * always safe to call {@link Semaphore#release()} which increases the
         * permit size by one.
         *
         * @param obj
         */
        private Entry(T obj) {
            this.soft = new SoftReference<T>(obj);
            this.active.set(obj);
        }

        public T get() {
            return active.get();
        }

        /**
         * Largely for testing purposes
         *
         * @return
         */
        public boolean hasHardReference() {
            return hard.get() != null;
        }
    }
}
