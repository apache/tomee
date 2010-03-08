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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final Executor executor;

    private final Supplier<T> supplier = null;
    
    public Pool(int max, int min, boolean strict) {
        this(max, min, strict, 0, 0, 0, null);
    }

    public Pool(int max, int min, boolean strict, long maxAge, long idleTimeout, long interval, Executor executor) {
        if (min > max) greater("max", max, "min", min);
        if (maxAge != 0 && idleTimeout > maxAge) greater("MaxAge", maxAge, "IdleTimeout", idleTimeout);
        this.max = max;
        this.minPolicy = new Semaphore(min);
        if (strict) {
            this.maxPolicy = new Semaphore(max);
        } else {
            this.maxPolicy = null;
        }

        if (interval == 0) {
            interval = 60 * 1000; // one minute
        }

        final boolean timeouts = maxAge > 0 || idleTimeout > 0;

        this.executor = timeouts ? (executor != null) ? executor : createExecutor() : null;

        if (timeouts) {

            final Timer timer = new Timer("PoolEviction", true);
            timer.scheduleAtFixedRate(new Eviction(maxAge, idleTimeout), idleTimeout, interval);
        }
    }

    private ThreadPoolExecutor createExecutor() {
        return new ThreadPoolExecutor(0, 10, 60 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
    }

    private void greater(String maxName, long max, String minName, long min) {
        throw new IllegalArgumentException(minName + " cannot be greater than " + maxName + ": " + minName + "=" + min + ", " + maxName + "=" + max);
    }

    /**
     * Any successful pop() call requires a corresponding push() or discard() call
     * <p/>
     * A pop() call that returns null is considered successful.
     *
     * @param timeout time to block while waiting for an instance
     * @param unit    unit of time dicated by the timeout
     * @return an entry from the pool or null indicating permission to create and push() an instance into the pool
     * @throws InterruptedException  vm level thread interruption
     * @throws IllegalStateException if a permit could not be acquired
     * @throws TimeoutException      if no instance could be obtained within the timeout
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
     * @param obj object to add to the pool
     * @return true of the item as added
     */
    public boolean add(T obj) {
        return (maxPolicy == null || maxPolicy.tryAcquire()) && push(obj);
    }

    /**
     * Attempt to aquire a permit to add the object to the pool.
     *
     * @param obj    object to add to the pool
     * @param offset creation time offset, used for maxAge
     * @return true of the item as added
     */
    public boolean add(T obj, int offset) {
        return (maxPolicy == null || maxPolicy.tryAcquire()) && push(new Entry<T>(obj, offset));
    }

    /**
     * Never call this method without having successfully called
     * {@link #pop(long, java.util.concurrent.TimeUnit)} beforehand.
     * <p/>
     * Failure to do so will increase the max pool size by one.
     *
     * @param obj object to push onto the pool
     * @return false if the pool max size was exceeded
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
     * @param entry entry that was popped from the pool
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

    /**
     * This internal method allows us to "swap" the status
     * of two entries before returning them to the pool.
     * <p/>
     * This allows us to elect a replacement in the min pool
     * without ever loosing loosing pool consistency.
     * <p/>
     * Neither argument is allowed to be null.
     *
     * @param hard the "min" pool item that will be discarded
     * @param weak the "min" pool item to replace the discarded instance
     */
    private void discardAndReplace(Entry<T> hard, Entry<T> weak) {
        // The replacement becomes a hard reference -- a "min" pool item
        weak.hard.set(weak.get());
        push(weak);

        // The discarded item becomes a weak reference
        hard.hard.set(null);
        discard(hard);
    }

    public static class Entry<T> {
        private final long created;
        private long used;

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
         * always safe to call {@link java.util.concurrent.Semaphore#release()} which increases the
         * permit size by one.
         *
         * @param obj object that this Entry will wrap
         */
        private Entry(T obj) {
            this(obj, 0);
        }

        /**
         * Constructor is private so that it is impossible for an Entry object
         * to exist without there being a corresponding permit issued for the
         * object wrapped by this Entry.
         * <p/>
         * This helps ensure that when an Entry is returned to the pool it is
         * always safe to call {@link Semaphore#release()} which increases the
         * permit size by one.
         *
         * @param obj    object that this Entry will wrap
         * @param offset creation time offset, used for maxAge
         */
        private Entry(T obj, int offset) {
            this.soft = new SoftReference<T>(obj);
            this.active.set(obj);
            this.created = System.currentTimeMillis() + offset;
            this.used = created;
        }

        public T get() {
            return active.get();
        }

        /**
         * Largely for testing purposes
         *
         * @return true if this entry is in the "min" pool
         */
        public boolean hasHardReference() {
            return hard.get() != null;
        }
    }

    private final class Eviction extends TimerTask {

        private final long maxAge;
        private final long idleTimeout;

        private Eviction(long maxAge, long idleTimeout) {
            this.maxAge = maxAge;
            this.idleTimeout = idleTimeout;
        }

        public void run() {

            final long now = System.currentTimeMillis();

            final List<Entry<T>> entries = new ArrayList(max);

            try {
                while (true) entries.add(pop(0, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (TimeoutException e) {
                // pool has been drained
            }


            final List<Expired> expiredList = new ArrayList<Expired>(max);

            { // Expire aged instances

                // Any "null" entries are immediately returned
                // Any non-aged "min" refs are immediately returned

                final Iterator<Entry<T>> iter = entries.iterator();
                while (iter.hasNext()) {
                    Entry<T> entry = iter.next();

                    if (entry == null) {
                        // return the lock immediately
                        push(entry);
                        iter.remove();
                        continue;
                    }

                    long age = now - entry.created;

                    if (maxAge > 0 && age > maxAge) {

                        // Entry is too old, expire it

                        iter.remove();
                        final Expired expired = new Expired(entry);
                        expiredList.add(expired);

                        if (!expired.entry.hasHardReference()) {
                            expired.tryDiscard();
                        }

                    } else if (entry.hasHardReference()) {
                        // This is an item from the "minimum" pool
                        // and therefore cannot timeout in the next
                        // algorithm.  Return it immediately.
                        push(entry);
                        iter.remove();
                    }
                }
            }

            // At this point all Entries not eligible for idle timeout
            // have been returned to the pool and can now be in use.

            // There are no "null" and no min-pool ("hard") entries beyond
            // this point.  Everything is a weak reference, possibly timed out.

            // If items from the "min" pool have expired, we will need
            // to return that number to the pool regardless of their
            // timeout setting so that they may take the place of the
            // expired instances

            Iterator<Expired> discardables = expiredList.iterator();
            while (discardables.hasNext() && entries.size() > 0) {

                if (discardables.next().replaceMinEntry(entries.get(0))) {
                    entries.remove(0);
                }

            }

            // At this point all the expired "min" pool refs will have
            // been replaced with entries from our initial list.
            //
            // Unless, of course, we didn't have enough entries left over
            // to fill the "min" pool deficit.  In that case, the entries
            // list will be empty and this loop will do nothing.
            final Iterator<Entry<T>> iter = entries.iterator();
            while (iter.hasNext()) {

                final Entry<T> entry = iter.next();

                iter.remove(); // we know we're going to use it

                final long idle = now - entry.used;

                if (idle > idleTimeout) {
                    // too lazy -- timed out 
                    final Expired expired = new Expired(entry);

                    expiredList.add(expired);

                    expired.tryDiscard();

                } else {
                    push(entry);
                }
            }

            // Ok, now we have the task of invoking callbacks
            // on all the expired instances.
            //
            // If there are any "min" pool instances left over
            // we need to queue up creation of a replacement

            for (Expired expired : expiredList) {
                executor.execute(new Discard(expired.entry));

                if (expired.entry.hasHardReference()) {
                    executor.execute(new Replace(expired.entry));
                }
            }

        }

        private class Expired {
            private final Entry<T> entry;
            private final AtomicBoolean discarded = new AtomicBoolean();

            private Expired(Entry<T> entry) {
                this.entry = entry;
            }

            public boolean tryDiscard() {
                if (discarded.getAndSet(true)) return false;

                discard(entry);

                return true;
            }


            public boolean replaceMinEntry(Entry<T> replacement) {
                if (!entry.hasHardReference()) return false;
                if (replacement.hasHardReference()) return false;
                if (discarded.getAndSet(true)) return false;

                discardAndReplace(entry, replacement);

                return true;
            }
        }
    }

    private class Replace implements Runnable {
        private final Entry<T> expired;

        private Replace(Entry<T> expired) {
            this.expired = expired;
        }

        public void run() {
            try {
                final T t = supplier.create();
                final Entry entry = new Entry(t);
                entry.hard.set(t);
                push(entry);
            } catch (Throwable e) {
                // Possibly re-try
                // TODO: log creation failure
                discard(expired);
            }
        }
    }

    private class Discard implements Runnable {
        private final Entry<T> expired;

        private Discard(Entry<T> expired) {
            this.expired = expired;
        }

        public void run() {
            supplier.discard(expired.get());
        }
    }

    public static interface Supplier<T> {

        void discard(T t);

        T create();

    }
}
