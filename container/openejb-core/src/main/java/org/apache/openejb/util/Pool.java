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
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final Semaphore instances;
    private final Semaphore available;
    private final Semaphore minimum;
    private final Executor executor;
    private final long maxAge;
    private final AtomicInteger poolVersion = new AtomicInteger();
    private final Supplier<T> supplier;

    public static class Builder<T> {

        private int max = 10;
        private int min = 0;
        private boolean strict = true;
        private Duration maxAge = new Duration(0, MILLISECONDS);
        private Duration idleTimeout =  new Duration(0, MILLISECONDS);
        private Duration interval =  new Duration(5 * 60, TimeUnit.SECONDS);
        private Supplier<T> supplier;
        private Executor executor;

        public Builder(Builder<T> that) {
            this.max = that.max;
            this.min = that.min;
            this.strict = that.strict;
            this.maxAge = that.maxAge;
            this.idleTimeout = that.idleTimeout;
            this.interval = that.interval;
            this.executor = that.executor;
            this.supplier = that.supplier;
        }

        public Builder() {
        }

        public int getMin() {
            return min;
        }

        public void setPoolMax(int max) {
            this.max = max;
        }

        /**
         * Alias for pool size
         * @param max
         * @return
         */
        public void setPoolSize(int max) {
            setPoolMax(max);
        }

        public void setPoolMin(int min) {
            this.min = min;
        }

        public void setStrictPooling(boolean strict) {
            this.strict = strict;
        }

        public void setMaxAge(Duration maxAge) {
            this.maxAge = maxAge;
        }

        public void setIdleTimeout(Duration idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public void setPollInterval(Duration interval) {
            this.interval = interval;
        }

        public void setSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public void setExecutor(Executor executor) {
            this.executor = executor;
        }

        public Pool<T> build() {
            return new Pool(max, min, strict, maxAge.getTime(MILLISECONDS), idleTimeout.getTime(MILLISECONDS), interval.getTime(MILLISECONDS), executor, supplier);
        }
    }

    public Pool(int max, int min, boolean strict) {
        this(max, min, strict, 0, 0, 0, null, null);
    }
    
    public Pool(int max, int min, boolean strict, long maxAge, long idleTimeout, long interval, Executor executor, Supplier<T> supplier) {
        if (min > max) greater("max", max, "min", min);
        if (maxAge != 0 && idleTimeout > maxAge) greater("MaxAge", maxAge, "IdleTimeout", idleTimeout);
        this.executor = executor != null ? executor : createExecutor();
        this.supplier = supplier != null ? supplier : new NoSupplier();
        this.available = (strict) ? new Semaphore(max) : new Overdraft();
        this.minimum = new Semaphore(min);
        this.instances = new Semaphore(max);
        this.maxAge = maxAge;

        if (interval == 0) interval = 5 * 60 * 1000; // five minutes

        final Timer timer = new Timer("PoolEviction", true);
        timer.scheduleAtFixedRate(new Eviction(idleTimeout, max), idleTimeout, interval);
    }

    private Executor createExecutor() {
        return new ThreadPoolExecutor(5, 10,
                                      0L, TimeUnit.SECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }

    private void greater(String maxName, long max, String minName, long min) {
        throw new IllegalArgumentException(minName + " cannot be greater than " + maxName + ": " + minName + "=" + min + ", " + maxName + "=" + max);
    }

    public void flush() {
        poolVersion.incrementAndGet();
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
        if (!available.tryAcquire(timeout, unit)) throw new TimeoutException("Waited " + timeout + " " + unit);

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
        return add(obj, 0);
    }

    /**
     * Attempt to aquire a permit to add the object to the pool.
     *
     * @param obj    object to add to the pool
     * @param offset creation time offset, used for maxAge
     * @return true of the item as added
     */
    public boolean add(T obj, int offset) {
        if (available.tryAcquire()) {

            if (push(obj, offset)) return true;

            available.release();
        }

        return false;
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
        return push(obj, 0);
    }

    /**
     * Never call this method without having successfully called
     * {@link #pop(long, java.util.concurrent.TimeUnit)} beforehand.
     * <p/>
     * Failure to do so will increase the max pool size by one.
     *
     * @param obj object to push onto the pool
     * @param offset
     * @return false if the pool max size was exceeded
     */
    private boolean push(T obj, int offset) {
        if (instances.tryAcquire()){
            return push(new Entry<T>(obj, offset, poolVersion.get()));
        }
        return false;
    }

    public boolean push(Entry<T> entry) {

        boolean added = false;
        boolean release = true;

        try {
            if (entry == null) return added;

            final T obj = entry.active.getAndSet(null);

            final long age = System.currentTimeMillis() - entry.created;

            final boolean aged = maxAge > 0 && age > maxAge;
            final boolean flushed = entry.version != this.poolVersion.get();

            if (aged || flushed) {
                if (entry.hasHardReference()) {
                    // Don't release the lock, this
                    // entry will be directly replaced
                    release = false;
                    executor.execute(new Replace(entry));
                }
            } else if (entry.hard.get() == null && minimum.tryAcquire()) {
                entry.hard.set(obj);

                if (!(added = insert(entry))) minimum.release();
            } else {
                added = insert(entry);
            }
        } finally {
            if (release) available.release();
        }

        return added;
    }

    private boolean insert(Entry<T> entry) {
        synchronized (pool) {
//            if (pool.size() >= max) return false;
            pool.addFirst(entry);
        }
        return true;
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
            final T obj = entry.get();

            if (entry.hard.compareAndSet(obj, null)) {
                minimum.release();
            }
            instances.release();
        }

        available.release();
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
        private final int version;
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
         * @param obj    object that this Entry will wrap
         * @param offset creation time offset, used for maxAge
         * @param version
         */
        private Entry(T obj, int offset, int version) {
            this.soft = new SoftReference<T>(obj);
            this.active.set(obj);
            this.created = System.currentTimeMillis() + offset;
            this.used = created;
            this.version = version;
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

        private final AtomicInteger previousVersion = new AtomicInteger(poolVersion.get());
        private final long idleTimeout;
        private final boolean timeouts;
        private final int max;

        private Eviction(long idleTimeout, int max) {
            this.idleTimeout = idleTimeout;
            timeouts = maxAge > 0 || idleTimeout > 0;
            this.max = max;
        }

        public void run() {

            final int currentVersion = poolVersion.get();

            final boolean isCurrent = previousVersion.getAndSet(currentVersion) == currentVersion;

            // No timeouts to enforce?
            // Pool version not changed?
            // Just return
            if (!timeouts && isCurrent) return;
            
            final long now = System.currentTimeMillis();

            final List<Entry<T>> entries = new ArrayList(max);

            // Pull all the entries from the pool
            try {
                while (true) entries.add(pop(0, MILLISECONDS));
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (TimeoutException e) {
                // pool has been drained
            }


            { // Immediately return all "null" instances to free up locks

                final Iterator<Entry<T>> iter = entries.iterator();
                while (iter.hasNext()) {
                    final Entry<T> entry = iter.next();
                    if (entry == null) {
                        // return the lock immediately
                        push(entry);
                        iter.remove();
                    }
                }

            }

            final List<Expired> expiredList = new ArrayList<Expired>(max);

            { // Expire aged instances, enforce pool "versioning"

                // Any non-aged "min" refs are immediately returned

                final Iterator<Entry<T>> iter = entries.iterator();
                while (iter.hasNext()) {
                    final Entry<T> entry = iter.next();

                    // is too old || is old version?
                    final boolean aged = maxAge > 0 && now - entry.created > maxAge;
                    final boolean flushed = entry.version != currentVersion;

                    if (aged || flushed) {

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

                if (idleTimeout > 0 && idle > idleTimeout) {
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

    private class Replace implements Runnable {
        private final Entry<T> expired;

        private Replace(Entry<T> expired) {
            this.expired = expired;
        }

        public void run() {
            try {
                final T t = supplier.create();

                if (t == null) {
                    discard(expired);
                } else {
                    final Entry entry = new Entry(t, 0 , poolVersion.get());
                    entry.hard.set(t);
                    push(entry);
                }
            } catch (Throwable e) {
                // Retry and logging should be done in
                // the Supplier implementation
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

    private static class NoSupplier implements Supplier {
        public void discard(Object o) {
        }

        public Object create() {
            return null;
        }
    }

    private static final class Overdraft extends Semaphore {
        public Overdraft() {
            super(0);
        }

        @Override
        public void acquire() throws InterruptedException {
        }

        @Override
        public void acquireUninterruptibly() {
        }

        @Override
        public boolean tryAcquire() {
            return true;
        }

        @Override
        public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void release() {
        }

        @Override
        public void acquire(int permits) throws InterruptedException {
        }

        @Override
        public void acquireUninterruptibly(int permits) {
        }

        @Override
        public boolean tryAcquire(int permits) {
            return true;
        }

        @Override
        public boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void release(int permits) {
        }

        @Override
        public int availablePermits() {
            return 0;
        }

        @Override
        public int drainPermits() {
            return 0;
        }

        @Override
        protected void reducePermits(int reduction) {
        }
    }
}
