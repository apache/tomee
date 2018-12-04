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
package org.apache.openejb.util;

import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.monitoring.Managed;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

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
@SuppressWarnings("StatementWithEmptyBody")
public class Pool<T> {

    private final LinkedList<Entry> pool = new LinkedList<>();
    private final Semaphore instances;
    private final Semaphore available;
    private final Semaphore minimum;
    private final Executor executor;

    @Managed
    private final long maxAge;

    @Managed
    private final AtomicInteger poolVersion = new AtomicInteger();

    private final Supplier<T> supplier;
    private final AtomicReference<ScheduledExecutorService> scheduler = new AtomicReference<>();
    private final AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
    private final Sweeper sweeper;

    private final CountingLatch out = new CountingLatch();

    @Managed
    private final long sweepInterval;

    @Managed
    private final boolean replaceAged;

    @Managed
    private final boolean replaceFlushed;

    @Managed
    private final double maxAgeOffset;

    @Managed
    private final Stats stats;

    @Managed
    private final boolean garbageCollection;

    public Pool(final int max, final int min, final boolean strict) {
        this(max, min, strict, 0, 0, 0, null, null, false, -1, false, false);
    }

    @SuppressWarnings("unchecked")
    public Pool(final int max, final int min, final boolean strict, final long maxAge, final long idleTimeout, long sweepInterval, final Executor executor, final Supplier<T> supplier, final boolean replaceAged, final double maxAgeOffset, final boolean garbageCollection, final boolean replaceFlushed) {
        if (min > max) {
            greater("max", max, "min", min);
        }
        if (maxAge != 0 && idleTimeout > maxAge) {
            greater("MaxAge", maxAge, "IdleTimeout", idleTimeout);
        }
        this.executor = executor != null ? executor : createExecutor();
        this.supplier = supplier != null ? supplier : new NoSupplier();
        this.available = strict ? new Semaphore(max) : new Overdraft(max);
        this.minimum = new Semaphore(min);
        this.instances = new Semaphore(max);
        this.maxAge = maxAge;
        this.maxAgeOffset = maxAgeOffset;
        this.replaceAged = replaceAged;
        this.replaceFlushed = replaceFlushed;
        if (sweepInterval == 0) {
            sweepInterval = 5 * 60 * 1000; // five minutes
        }
        this.sweepInterval = sweepInterval;
        this.sweeper = new Sweeper(idleTimeout, max);
        this.stats = new Stats(min, max, idleTimeout);
        this.garbageCollection = garbageCollection;
    }

    public Pool start() {
        ScheduledExecutorService scheduledExecutorService = this.scheduler.get();
        boolean createdSES = scheduledExecutorService == null;
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1, new SchedulerThreadFactory());
            if (!this.scheduler.compareAndSet(null, scheduledExecutorService)) {
                scheduledExecutorService.shutdownNow();
                scheduledExecutorService = this.scheduler.get();
                createdSES = false;
            }
        }
        final ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(sweeper, 0, this.sweepInterval, MILLISECONDS);
        if (!this.future.compareAndSet(null, scheduledFuture)) {
            scheduledFuture.cancel(true);
        }
        if (!createdSES) {
            // we don't want to shutdown it, we'll just stop the task
            this.scheduler.set(null);
        }
        return this;
    }

    public void stop() {
        final ScheduledFuture<?> future = this.future.getAndSet(null);
        if (future != null
                && !future.isDone() && !future.isCancelled()
                && !future.cancel(false)) {
            Logger.getLogger(Pool.class.getName()).log(Level.WARNING, "Pool scheduler task termination timeout expired");
        }

        final ScheduledExecutorService scheduler = this.scheduler.getAndSet(null);
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, SECONDS)) { // should last something like 0s max since we killed the task
                    Logger.getLogger(Pool.class.getName()).log(Level.WARNING, "Pool scheduler termination timeout expired");
                }
            } catch (final InterruptedException e) {
                //Ignore
            }
        }
    }

    public boolean running() {
        return this.future.get() != null;
    }

    private Executor createExecutor() {
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 10,
                60L, SECONDS,
                new LinkedBlockingQueue<>(2), new DaemonThreadFactory("org.apache.openejb.util.Pool", hashCode()));

        threadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {

                if (null == r || null == tpe || tpe.isShutdown() || tpe.isTerminated() || tpe.isTerminating()) {
                    return;
                }

                try {
                    if (!tpe.getQueue().offer(r, 20, SECONDS)) {
                        org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources")
                                .warning("Default pool executor failed to run asynchronous process: " + r);
                    }
                } catch (final InterruptedException e) {
                    //Ignore
                }
            }
        });

        return threadPoolExecutor;
    }

    private void greater(final String maxName, final long max, final String minName, final long min) {
        throw new IllegalArgumentException(minName + " cannot be greater than " + maxName + ": " + minName + "=" + min + ", " + maxName + "=" + max);
    }

    @Managed
    public void flush() {
        stats.flushes.record();
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
    public Entry pop(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
        return pop(timeout, unit, true);
    }

    /**
     * Any successful pop() call requires a corresponding push() or discard() call
     * <p/>
     * A pop() call that returns null is considered successful.
     *
     * @param timeout time to block while waiting for an instance
     * @param unit    unit of time dicated by the timeout
     * @param record  should this be reflected in the stats
     * @return an entry from the pool or null indicating permission to create and push() an instance into the pool
     * @throws InterruptedException  vm level thread interruption
     * @throws IllegalStateException if a permit could not be acquired
     * @throws TimeoutException      if no instance could be obtained within the timeout
     */
    private Entry pop(final long timeout, final TimeUnit unit, final boolean record) throws InterruptedException, TimeoutException {
        if (timeout == -1) {
            available.tryAcquire();
        } else if (!available.tryAcquire(timeout, unit)) {
            if (record) {
                stats.accessTimeouts.record();
            }
            throw new TimeoutException("Waited " + timeout + " " + unit);
        }

        Entry entry;
        do {
            synchronized (pool) {
                try {
                    entry = pool.removeFirst();
                } catch (final NoSuchElementException e) {
                    return null;
                }
            }

            final Pool<T>.Entry.Instance instance = entry.soft.get();

            if (instance != null) {

                final boolean notBusy = entry.active.compareAndSet(null, instance);

                if (notBusy) {
                    return entry;
                }
            } else {
                // the SoftReference was garbage collected
                instances.release();
            }
        } while (true);
    }

    /**
     * Attempt to aquire a permit to add the object to the pool.
     *
     * @param obj object to add to the pool
     * @return true of the item as added
     */
    public boolean add(final T obj) {
        return add(obj, 0);
    }

    /**
     * Attempt to aquire a permit to add the object to the pool.
     *
     * @param obj    object to add to the pool
     * @param offset creation time offset, used for maxAge
     * @return true of the item as added
     */
    public boolean add(final T obj, final long offset) {
        try {
            if (available.tryAcquire(100, MILLISECONDS)) {

                try {
                    if (push(obj, offset)) {
                        return true;
                    }
                    available.release();
                } catch (final RuntimeException e) {
                    available.release();
                    throw e;
                }
            }

            return false;
        } catch (final InterruptedException e) {
            Thread.interrupted();
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Never call this method without having successfully called
     * {@link #pop(long, TimeUnit)} beforehand.
     * <p/>
     * Failure to do so will increase the max pool size by one.
     *
     * @param obj object to push onto the pool
     * @return false if the pool max size was exceeded
     */
    public boolean push(final T obj) {
        return push(obj, 0);
    }

    /**
     * Never call this method without having successfully called
     * {@link #pop(long, TimeUnit)} beforehand.
     * <p/>
     * Failure to do so will increase the max pool size by one.
     *
     * @param obj    object to push onto the pool
     * @param offset long
     * @return false if the pool max size was exceeded
     */
    private boolean push(final T obj, final long offset) {
        if (instances.tryAcquire()) {
            return push(new Entry(obj, offset));
        }

        if (obj != null) {
            new Discard(obj, Event.FULL).run();
        }

        if (available instanceof Overdraft) {
            available.release();
        }

        return false;
    }

    public boolean push(final Entry entry) {
        return push(entry, false);
    }

    private boolean push(final Entry entry, final boolean sweeper) {
        boolean added = false;
        boolean release = true;
        Event event = Event.FULL;

        final Entry.Instance obj = entry == null ? null : entry.active.getAndSet(null);

        try {
            if (entry == null) {
                return false;
            }

            if (!sweeper) {
                entry.markLastUsed();
            }

            final long age = now() - entry.created;

            final boolean aged = maxAge > 0 && age > maxAge;
            final boolean flushed = entry.version != this.poolVersion.get();

            if (aged || flushed) {
                if (aged) {
                    event = Event.AGED;
                }
                if (flushed) {
                    event = Event.FLUSHED;
                }
                if (entry.hasHardReference() || aged && replaceAged || flushed && replaceFlushed) {
                    // Don't release the lock, this
                    // entry will be directly replaced
                    release = false;
                    entry.active.set(obj);
                    executor.execute(new Replace(entry));
                }
            } else {
                // make this a "min" instance if we can
                if (!entry.hasHardReference() && minimum.tryAcquire()) {
                    entry.hard.set(obj);
                }

                synchronized (pool) {
                    pool.addFirst(entry);
                }
                added = true;
            }
        } finally {
            if (release) {

                available.release();

                if (entry != null && !added) {
                    instances.release();
                }
            }
        }

        if (!added && obj != null) {
            if (sweeper) {
                // if the caller is the PoolEviction thread, we do not
                // want to be calling discard() directly and should just
                // queue it up instead.
                executor.execute(obj.discard(event));
            } else {
                obj.discard(event).run();
            }
        }

        return added;
    }

//    private void println(String s) {
//        Thread thread = Thread.currentThread();
//        PrintStream out = System.out;
//        synchronized (out) {
//            String s1 = thread.getName();
//            out.format("%1$tH:%1$tM:%1$tS.%1$tL - %2$s - %3$s\n", System.currentTimeMillis(), s1, s);
//            out.flush();
//        }
//    }

    /**
     * Used when a call to pop() was made that returned null
     * indicating that the caller has a permit to create an
     * object for this pool, but the caller will not be exercising
     * that permit and wishes intstead to return "null" to the pool.
     */
    public void discard() {
        discard(null);
    }

    public void discard(final Entry entry) {
        if (entry != null) {

            if (entry.hasHardReference()) {
                minimum.release();
            }

            instances.release();
        }

        available.release();
    }

    public boolean close(final long timeout, final TimeUnit unit) throws InterruptedException {

        // Stop the sweeper thread
        stop();

        // drain all keys so no new instances will be accepted into the pool
        while (instances.tryAcquire()) {
            Thread.yield();
        }

        while (minimum.tryAcquire()) {
            Thread.yield();
        }

        // flush and sweep
        flush();

        try {
            sweeper.run();
        } catch (final Exception ignore) {
            //no-op
        }

        // Drain all leases
        if (!(available instanceof Overdraft)) {
            while (available.tryAcquire()) {
                Thread.yield();
            }

            available.drainPermits();
        }

        instances.drainPermits();
        minimum.drainPermits();

        // Wait for any pending discards
        return out.await(timeout, unit);
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
     * @param soft the "min" pool item to replace the discarded instance
     */
    private void discardAndReplace(final Entry hard, final Entry soft) {
        // The replacement becomes a hard reference -- a "min" pool item
        soft.hard.set(soft.active());
        push(soft);

        // The discarded item becomes a soft reference
        hard.hard.set(null);
        discard(hard);
    }

    private static long now() {
        return MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public final class Entry {
        private final long created;
        private long used;
        private final int version;
        private final SoftReference<Instance> soft;
        private final AtomicReference<Instance> hard = new AtomicReference<>();

        // Added this so the soft reference isn't collected
        // after the Entry instance is returned from a "pop" method
        // Also acts as an "inUse" boolean
        private final AtomicReference<Instance> active = new AtomicReference<>();

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
        private Entry(final T obj, final long offset) {
            if (obj == null) {
                throw new NullPointerException("entry is null");
            }
            final Instance instance = new Instance(obj);
            this.soft = garbageCollection ?
                    new SoftReference<>(instance) :
                    new HardReference<>(instance);
            this.version = poolVersion.get();
            this.active.set(instance);
            this.created = now() + offset;
            this.used = created;
        }

        public T get() {
            return active().instance;
        }

        private Instance active() {
            return active.get();
        }

        public void harden() {
            hard.set(active());
        }

        public void markLastUsed() {
            used = now();
        }

        public long getUsed() {
            return used;
        }

        /**
         * Largely for testing purposes
         *
         * @return true if this entry is in the "min" pool
         */
        public boolean hasHardReference() {
            return hard.get() != null;
        }

        @Override
        public String toString() {
            final long now = now();
            return "Entry{" +
                    "min=" + (hard.get() != null) +
                    ", age=" + (now - created) +
                    ", idle=" + (now - used) +
                    ", bean=" + soft.get() +
                    '}';
        }

        private class Discarded implements Runnable {
            @Override
            public void run() {
            }
        }

        /**
         * Exists for the garbage collection related callbacks
         */
        public class Instance {

            private final AtomicBoolean callback = new AtomicBoolean();

            private final T instance;

            public Instance(final T instance) {
                this.instance = instance;
            }

            @Override
            protected void finalize() throws Throwable {
                try {
                    discard(Event.GC).run();
                } finally {
                    super.finalize();
                }
            }

            public Runnable discard(final Event event) {
                if (callback.compareAndSet(false, true)) {
                    return new Discard(instance, event);
                }
                return new Discarded();
            }
        }
    }

    private final class Sweeper implements Runnable {

        private final AtomicInteger previousVersion = new AtomicInteger(poolVersion.get());
        private final long idleTimeout;
        private final boolean timeouts;
        private final int max;

        private Sweeper(final long idleTimeout, final int max) {
            this.idleTimeout = idleTimeout;
            timeouts = maxAge > 0 || idleTimeout > 0;
            this.max = max;
        }

        @Override
        public void run() {

            stats.sweeps.record();

            final int currentVersion = poolVersion.get();

            final boolean isCurrent = previousVersion.getAndSet(currentVersion) == currentVersion;

            // No timeouts to enforce?
            // Pool version not changed?
            // Just return
            if (!timeouts && isCurrent) {
                return;
            }

            final long now = now();

            final List<Entry> entries = new ArrayList<>(max);

            // Pull all the entries from the pool
            try {
                while (true) {
                    final Entry entry = pop(0, MILLISECONDS, false);
                    if (entry == null) {
                        push(null, true);
                        break;
                    }
                    entries.add(entry);
                }
            } catch (final InterruptedException e) {
                Thread.interrupted();
            } catch (final TimeoutException e) {
                // pool has been drained
            }

            final List<Expired> expiredList = new ArrayList<>(max);

            { // Expire aged instances, enforce pool "versioning"

                // Any non-aged "min" refs are immediately returned

                final Iterator<Entry> iter = entries.iterator();
                while (iter.hasNext()) {
                    final Entry entry = iter.next();

                    // is too old || is old version?
                    final boolean aged = maxAge > 0 && now - entry.created > maxAge;
                    final boolean flushed = entry.version != currentVersion;

                    if (aged || flushed) {

                        // Entry is too old, expire it

                        iter.remove();
                        final Expired expired = new Expired(entry, aged ? Event.AGED : Event.FLUSHED);
                        expiredList.add(expired);

                        if (!expired.entry.hasHardReference() && !(aged && replaceAged)) {
                            expired.tryDiscard();
                        }

                    } else if (entry.hasHardReference()) {
                        // This is an item from the "minimum" pool
                        // and therefore cannot timeout in the next
                        // algorithm.  Return it immediately.
                        push(entry, true);
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

            final Iterator<Expired> discardables = expiredList.iterator();
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
            final Iterator<Entry> iter = entries.iterator();
            while (iter.hasNext()) {

                final Entry entry = iter.next();

                iter.remove(); // we know we're going to use it

                final long idle = now - entry.used;

                if (idleTimeout > 0 && idle > idleTimeout) {
                    // too lazy -- timed out 
                    final Expired expired = new Expired(entry, Event.IDLE);

                    expiredList.add(expired);

                    expired.tryDiscard();

                } else {
                    push(entry, true);
                }
            }

            // Ok, now we have the task of invoking callbacks
            // on all the expired instances.
            //
            // If there are any "min" pool instances left over
            // we need to queue up creation of a replacement

            final List<Expired> replace = new ArrayList<>();

            for (final Expired expired : expiredList) {
                executor.execute(expired.entry.active().discard(expired.event));

                if (expired.entry.hasHardReference() || expired.aged() && replaceAged) {
                    replace.add(expired);
                }
            }

            for (int i = 0; i < replace.size(); i++) {
                final long offset = maxAge > 0 ? (long) (maxAge / replace.size() * i * maxAgeOffset) % maxAge : 0L;
                executor.execute(new Replace(replace.get(i).entry, offset));
            }
        }

    }

    public enum Event {
        FULL, IDLE, AGED, FLUSHED, GC
    }

    private final class Expired {
        private final Entry entry;
        private final AtomicBoolean discarded = new AtomicBoolean();
        private final Event event;

        private Expired(final Entry entry, final Event event) {
            this.entry = entry;
            this.event = event;
        }

        public boolean aged() {
            return event == Event.AGED;
        }

        public boolean tryDiscard() {
            if (discarded.getAndSet(true)) {
                return false;
            }

            discard(entry);

            return true;
        }

        public boolean replaceMinEntry(final Entry replacement) {
            if (!entry.hasHardReference()) {
                return false;
            }
            if (replacement.hasHardReference()) {
                return false;
            }
            if (discarded.getAndSet(true)) {
                return false;
            }

            discardAndReplace(entry, replacement);

            return true;
        }
    }

    private final class Replace implements Runnable {
        private final Entry expired;
        private final long offset;

        private Replace(final Entry expired) {
            this(expired, 0);
        }

        private Replace(final Entry expired, final long offset) {
            this.expired = expired;
            this.offset = offset;
        }

        @Override
        public void run() {
            if (!running()) {
                discard(expired);
                return;
            }

            try {
                final T t = supplier.create();

                if (t == null) {
                    discard(expired);
                } else {
                    final Entry entry = new Entry(t, offset);
                    if (expired.hasHardReference()) {
                        entry.harden();
                    }
                    push(entry);
                }
            } catch (final Throwable e) {
                // Retry and logging should be done in
                // the Supplier implementation
                discard(expired);
            } finally {
                stats.replaced.record();
            }
        }
    }

    private final class Discard implements Runnable {
        private final T expired;
        private final Event event;

        private Discard(final T expired, final Event event) {
            out.countUp();
            if (expired == null) {
                throw new NullPointerException("expired object cannot be null");
            }
            this.expired = expired;
            this.event = event;
        }

        @Override
        public void run() {
            switch (event) {
                case AGED:
                    stats.aged.record();
                    break;
                case FLUSHED:
                    stats.flushed.record();
                    break;
                case FULL:
                    stats.overdrafts.record();
                    break;
                case IDLE:
                    stats.idleTimeouts.record();
                    break;
                case GC:
                    stats.garbageCollected.record();
                    break;
            }
            try {
                supplier.discard(expired, event);
            } finally {
                out.countDown();
            }
        }
    }

    public interface Supplier<T> {

        void discard(T t, Event reason);

        T create();

    }

    private static class NoSupplier implements Supplier {
        @Override
        public void discard(final Object o, final Event reason) {
        }

        @Override
        public Object create() {
            return null;
        }
    }

    private static final class Overdraft extends Semaphore {

        private static final long serialVersionUID = 1L;
        private final AtomicInteger permits = new AtomicInteger();

        public Overdraft(final int permits) {
            super(0);
            this.permits.set(permits);
        }

        @Override
        public void acquire() throws InterruptedException {
            permits.decrementAndGet();
        }

        @Override
        public void acquireUninterruptibly() {
            permits.decrementAndGet();
        }

        @Override
        public boolean tryAcquire() {
            permits.decrementAndGet();
            return true;
        }

        @Override
        public boolean tryAcquire(final long timeout, final TimeUnit unit) throws InterruptedException {
            permits.decrementAndGet();
            return true;
        }

        @Override
        public void acquire(final int permits) throws InterruptedException {
            this.permits.addAndGet(-permits);
        }

        @Override
        public void acquireUninterruptibly(final int permits) {
            this.permits.addAndGet(-permits);
        }

        @Override
        public boolean tryAcquire(final int permits) {
            this.permits.addAndGet(-permits);
            return true;
        }

        @Override
        public boolean tryAcquire(final int permits, final long timeout, final TimeUnit unit) throws InterruptedException {
            this.permits.addAndGet(-permits);
            return true;
        }

        @Override
        public void release() {
            this.permits.incrementAndGet();
        }

        @Override
        public void release(final int permits) {
            this.permits.addAndGet(permits);
        }

        @Override
        public int availablePermits() {
            return permits.get();
        }

        @Override
        public int drainPermits() {
            return 0;
        }

        @Override
        protected void reducePermits(final int reduction) {
        }
    }

    @SuppressWarnings({"PMD.UnusedPrivateField", "UnusedDeclaration"})
    @Managed
    private final class Stats {

        @Managed
        private final org.apache.openejb.monitoring.Event sweeps = new org.apache.openejb.monitoring.Event();

        @Managed
        private final org.apache.openejb.monitoring.Event flushes = new org.apache.openejb.monitoring.Event();

        @Managed
        private final org.apache.openejb.monitoring.Event accessTimeouts = new org.apache.openejb.monitoring.Event();

        @Managed
        private final org.apache.openejb.monitoring.Event garbageCollected = new org.apache.openejb.monitoring.Event();

        @Managed
        private final org.apache.openejb.monitoring.Event idleTimeouts = new org.apache.openejb.monitoring.Event();

        @Managed
        private final org.apache.openejb.monitoring.Event aged = new org.apache.openejb.monitoring.Event();

        @Managed
        private final org.apache.openejb.monitoring.Event flushed = new org.apache.openejb.monitoring.Event();

        @Managed
        private final org.apache.openejb.monitoring.Event overdrafts = new org.apache.openejb.monitoring.Event();

        @Managed
        private final org.apache.openejb.monitoring.Event replaced = new org.apache.openejb.monitoring.Event();

        @Managed
        private final int minSize;

        @Managed
        private final int maxSize;

        @Managed
        private final long idleTimeout;

        private Stats(final int minSize, final int maxSize, final long idleTimeout) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.idleTimeout = idleTimeout;
        }

        @Managed
        private boolean getStrictPooling() {
            return !(available instanceof Overdraft);
        }

        @Managed
        private int getAvailablePermits() {
            return available.availablePermits();
        }

        @Managed
        private int getInstancesPooled() {
            return maxSize - Pool.this.instances.availablePermits();
        }

        @Managed
        private int getInstancesIdle() {
            return Math.max(0, getInstancesPooled() - getInstancesActive());
        }

        @Managed
        private int getInstancesInitializing() {
            return Math.max(0, getInstancesActive() - getInstancesPooled());
        }

        @Managed
        private int getInstancesActive() {
            return maxSize - getAvailablePermits();
        }

        @Managed
        private int getMinimumInstances() {
            return minSize - minimum.availablePermits();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class Builder<T> {

        private int max = 10;
        private int min;
        private boolean strict = true;
        private Duration maxAge = new Duration(0, MILLISECONDS);
        private double maxAgeOffset = -1;
        private Duration idleTimeout = new Duration(0, MILLISECONDS);
        private Duration interval = new Duration(5 * 60, SECONDS);
        private Supplier<T> supplier;
        private Executor executor;
        private ScheduledExecutorService scheduledExecutorService;
        private boolean replaceAged;
        private boolean replaceFlushed;
        private boolean garbageCollection = true;

        public Builder(final Builder<T> that) {
            this.max = that.max;
            this.min = that.min;
            this.strict = that.strict;
            this.maxAge = that.maxAge;
            this.idleTimeout = that.idleTimeout;
            this.interval = that.interval;
            this.executor = that.executor;
            this.supplier = that.supplier;
            this.maxAgeOffset = that.maxAgeOffset;
            this.replaceAged = that.replaceAged;
            this.replaceFlushed = that.replaceFlushed;
            this.garbageCollection = that.garbageCollection;
        }

        public Builder() {
        }

        public int getMin() {
            return min;
        }

        public boolean isGarbageCollection() {
            return garbageCollection;
        }

        public void setGarbageCollection(final boolean garbageCollection) {
            this.garbageCollection = garbageCollection;
        }

        public void setReplaceAged(final boolean replaceAged) {
            this.replaceAged = replaceAged;
        }

        public void setReplaceFlushed(final boolean replaceFlushed) {
            this.replaceFlushed = replaceFlushed;
        }

        public void setMaxSize(final int max) {
            this.max = max;
        }

        /**
         * Alias for pool size
         *
         * @param max int
         */
        public void setPoolSize(final int max) {
            setMaxSize(max);
        }

        public void setMinSize(final int min) {
            this.min = min;
        }

        public void setStrictPooling(final boolean strict) {
            this.strict = strict;
        }

        public void setMaxAge(final Duration maxAge) {
            this.maxAge = maxAge;
        }

        public Duration getMaxAge() {
            return maxAge;
        }

        public boolean isStrict() {
            return strict;
        }

        public Duration getIdleTimeout() {
            return idleTimeout;
        }

        public Duration getInterval() {
            return interval;
        }

        public boolean isReplaceAged() {
            return replaceAged;
        }

        public void setMaxAgeOffset(final double maxAgeOffset) {
            this.maxAgeOffset = maxAgeOffset;
        }

        public double getMaxAgeOffset() {
            return maxAgeOffset;
        }

        public void setIdleTimeout(final Duration idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public void setSweepInterval(final Duration interval) {
            this.interval = interval;
        }

        public void setSupplier(final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public void setExecutor(final Executor executor) {
            this.executor = executor;
        }

        public void setScheduledExecutor(final ScheduledExecutorService scheduledExecutorService) {
            this.scheduledExecutorService = scheduledExecutorService;
        }

        @SuppressWarnings("unchecked")
        public Pool<T> build() {
            //noinspection unchecked
            final Pool pool = new Pool(max, min, strict, maxAge.getTime(MILLISECONDS), idleTimeout.getTime(MILLISECONDS), interval.getTime(MILLISECONDS), executor, supplier, replaceAged, maxAgeOffset, this.garbageCollection, replaceFlushed);
            if (scheduledExecutorService != null) {
                pool.scheduler.set(scheduledExecutorService);
            }
            return pool;
        }
    }

    public static class HardReference<T> extends SoftReference<T> {
        /**
         * Effectively disables the soft reference
         */
        private final T hard;

        public HardReference(final T referent) {
            super(referent);
            this.hard = referent;
        }

        @SuppressWarnings("UnusedDeclaration")
        public T getHard() {
            return hard;
        }
    }

    static class SchedulerThreadFactory implements ThreadFactory {

        private static final AtomicInteger count = new AtomicInteger(1);
        private final ThreadGroup group;

        SchedulerThreadFactory() {
            final SecurityManager s = System.getSecurityManager();
            this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(final Runnable r) {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            final ClassLoader containerLoader = ParentClassLoaderFinder.Helper.get();
            Thread.currentThread().setContextClassLoader(containerLoader);
            try {
                final Thread t = new Thread(group, r, "org.apache.openejb.pool.scheduler." + count.getAndIncrement());
                if (!t.isDaemon()) {
                    t.setDaemon(true);
                }

                if (t.getPriority() != Thread.NORM_PRIORITY) {
                    t.setPriority(Thread.NORM_PRIORITY);
                }

                t.setContextClassLoader(containerLoader);

                return t;
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        }
    }
}
