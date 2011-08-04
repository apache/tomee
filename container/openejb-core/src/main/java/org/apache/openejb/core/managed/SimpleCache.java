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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Duration;

public class SimpleCache<K, V> implements Cache<K, V> {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    /**
     * Map of all known values by key
     */
    private final ConcurrentHashMap<K, Entry> cache = new ConcurrentHashMap<K, Entry>();

    /**
     * All values not in use in least resently used order
     */
    private final Queue<Entry> lru = new LinkedBlockingQueue<Entry>();

    /**
     * Notified when values are loaded, stored, or timedOut
     */
    private CacheListener<V> listener;

    /**
     * Used to load and store values
     */
    private PassivationStrategy passivator;

    /**
     * Maximum number of values that should be in the LRU
     */
    private int capacity;

    /**
     * When the LRU is exceeded, this is the is the number of beans stored.
     * This helps to avoid passivating a bean at a time.
     */
    private int bulkPassivate;

    /**
     * A bean may be destroyed if it isn't used in this length of time (in
     * milliseconds).
     */
    private long timeOut;

    public SimpleCache() {
    }

    public SimpleCache(CacheListener<V> listener, PassivationStrategy passivator, int capacity, int bulkPassivate, Duration timeOut) {
        this.listener = listener;
        this.passivator = passivator;
        this.capacity = capacity;
        this.bulkPassivate = bulkPassivate;
        this.timeOut = timeOut.getTime(TimeUnit.MILLISECONDS);
    }

    public synchronized CacheListener<V> getListener() {
        return listener;
    }

    public synchronized void setListener(CacheListener<V> listener) {
        this.listener = listener;
    }

    public synchronized PassivationStrategy getPassivator() {
        return passivator;
    }

    public synchronized void setPassivator(PassivationStrategy passivator) {
        this.passivator = passivator;
    }

    public synchronized void setPassivator(Class<? extends PassivationStrategy> passivatorClass) throws Exception {
        this.passivator = passivatorClass.newInstance();
    }

    public synchronized int getCapacity() {
        return capacity;
    }

    public synchronized void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Old configurations use "PoolSize" to configure max cache size
    public synchronized void setPoolSize(int capacity) {
        this.capacity = capacity;
    }

    public synchronized int getBulkPassivate() {
        return bulkPassivate;
    }

    public synchronized void setBulkPassivate(int bulkPassivate) {
        this.bulkPassivate = bulkPassivate;
    }

    public synchronized long getTimeOut() {
        return timeOut;
    }

    public synchronized void setTimeOut(long timeOut) {
        this.timeOut = timeOut * 60 * 1000;
    }

    public void add(K key, V value) {
        // find the existing entry
        Entry entry = cache.get(key);
        if (entry != null) {
            entry.lock.lock();
            try {
                if (entry.getState() != EntryState.REMOVED) {
                    throw new IllegalStateException("An entry for the key " + key + " already exists");
                }
                // Entry has been removed between get and lock, simply remove the garbage entry
                cache.remove(key);
                lru.remove(entry);
            } finally {
                entry.lock.unlock();
            }
        }

        entry = new Entry(key, value, EntryState.CHECKED_OUT);
        cache.put(key, entry);
    }

    public V checkOut(K key) throws Exception {
        // attempt (up to 10 times) to obtain the entry from the cache
        for (int i = 0; i < 10; i++) {
            // find the entry
            Entry entry = cache.get(key);
            if (entry == null) {
                entry = loadEntry(key);
                if (entry == null) {
                    return null;
                }
            }

            entry.lock.lock();
            try {
                // verfiy state
                switch (entry.getState()) {
                    case AVAILABLE:
                        break;
                    case CHECKED_OUT:
                        throw new IllegalStateException("The entry " + key + " is already checked-out");
                    case PASSIVATED:
                        // Entry was passivated between get and lock, we need to load the Entry again
                        // If the cache somehow got corrupted by an entry containing in state PASSIVATED, this remove
                        // call will remove the corruption
                        cache.remove(key, entry);
                        continue;
                    case REMOVED:
                        // Entry has been removed between get and lock (most likely by undeploying the EJB), simply drop the instance
                        return null;
                }

                // mark entry as in-use
                entry.setState(EntryState.CHECKED_OUT);

                // entry is removed from the lru while in use
                lru.remove(entry);

                return entry.getValue();
            } finally {
                entry.lock.unlock();
            }
        }

        // something is really messed up with this entry, try to cleanup before throwing an exception
        Entry entry = cache.remove(key);
        if (entry != null) {
            lru.remove(entry);
        }
        throw new RuntimeException("Cache is corrupted: the entry " + key + " in the Map 'cache' is in state PASSIVATED");
    }

    public void checkIn(K key) {
        // find the entry
        Entry entry = cache.get(key);
        if (entry == null) {
            return;
        }

        entry.lock.lock();
        try {
            // verfiy state
            switch (entry.getState()) {
                case AVAILABLE:
                    if (lru.contains(entry)) {
                        entry.resetTimeOut();
                        return;
                    } else {
                        throw new IllegalStateException("The entry " + key + " is not checked-out");
                    }
                case PASSIVATED:
                    // An entry in-use should not be passivated so we can only assume
                    // that the caller never checked out the bean in the first place
                    throw new IllegalStateException("The entry " + key + " is not checked-out");
                case REMOVED:
                    // Entry has been removed between get and lock (most likely by undeploying the EJB), simply drop the instance
                    return;
            }

            // mark entry as available
            entry.setState(EntryState.AVAILABLE);

            // add entry to lru
            lru.add(entry);
            entry.resetTimeOut();
        } finally {
            entry.lock.unlock();
        }

        processLRU();
    }

    public V remove(K key) {
        // find the entry
        Entry entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        entry.lock.lock();
        try {
            // remove the entry from the cache and lru
            cache.remove(key);
            lru.remove(entry);

            // There is no need to check the state because users of the cache
            // are responsible for maintaining references to beans in use

            // mark the entry as removed
            entry.setState(EntryState.REMOVED);

            return entry.getValue();
        } finally {
            entry.lock.unlock();
        }
    }

    public void removeAll(CacheFilter<V> filter) {
        for (Iterator<Entry> iterator = cache.values().iterator(); iterator.hasNext();) {
            Entry entry = iterator.next();

            entry.lock.lock();
            try {
                if (filter.matches(entry.getValue())) {
                    // remove the entry from the cache and lru
                    iterator.remove();
                    lru.remove(entry);

                    // There is no need to check the state because users of the cache
                    // are responsible for maintaining references to beans in use

                    // mark the entry as removed
                    entry.setState(EntryState.REMOVED);
                }
            } finally {
                entry.lock.unlock();
            }
        }
    }

    public void processLRU() {
        CacheListener<V> listener = this.getListener();

        // check for timed out entries
        Iterator<Entry> iterator = lru.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            entry.lock.lock();
            try {
                switch (entry.getState()) {
                    case AVAILABLE:
                        break;
                    case CHECKED_OUT:
                        // bean is in use so cannot be passivated
                        continue;
                    case PASSIVATED:
                        // Entry was passivated between get and lock
                        iterator.remove();
                        continue;
                    case REMOVED:
                        // Entry was remmoved between get and lock
                        iterator.remove();
                        continue;
                }


                if (entry.isTimedOut()) {
                    iterator.remove();
                    cache.remove(entry.getKey());
                    entry.setState(EntryState.REMOVED);

                    // notify listener that the entry has been removed
                    if (listener != null) {
                        try {
                            listener.timedOut(entry.getValue());
                        } catch (Exception e) {
                            logger.error("An unexpected exception occured from timedOut callback", e);
                        }
                    }
                } else {
                    // entries are in order of last updates, so if this bean isn't timed out
                    // no further entries will be timed out
                    break;
                }
            } finally {
                entry.lock.unlock();
            }
        }

        // if there are to many beans in the lru, shink is by on bulkPassivate size
        // bulkPassivate size is just an estimate, as locked or timed out beans are skipped
        if (lru.size() >= getCapacity()) {
            Map<K, V> valuesToStore = new LinkedHashMap<K, V>();
            List<Entry> entries = new ArrayList<Entry>();

            int bulkPassivate = getBulkPassivate();
            if (bulkPassivate < 1) bulkPassivate = 1;
            for (int i = 0; i < bulkPassivate; i++) {
                Entry entry = lru.poll();
                if (entry == null) {
                    // lru is empty
                    break;
                }

                if (!entry.lock.tryLock()) {
                    // If two threads are running in this method, you could get a deadlock
                    // due to lock acquisition order since this section gathers a group of
                    // locks. Simply skip beans we can not obtain a lock on
                    continue;
                }
                try {
                    switch (entry.getState()) {
                        case AVAILABLE:
                            break;
                        case CHECKED_OUT:
                            // bean is in use so cannot be passivated
                            continue;
                        case PASSIVATED:
                            // Entry was passivated between get and lock
                            lru.remove(entry);
                            continue;
                        case REMOVED:
                            // Entry was remmoved between get and lock
                            lru.remove(entry);
                            continue;
                    }

                    // remove it from the cache
                    cache.remove(entry.getKey());

                    // there is a race condition where the item could get added back into the lru
                    lru.remove(entry);

                    // if the entry is actually timed out we just destroy it; othewise it is written to disk
                    if (entry.isTimedOut()) {
                        entry.setState(EntryState.REMOVED);
                        if (listener != null) {
                            try {
                                listener.timedOut(entry.getValue());
                            } catch (Exception e) {
                                logger.error("An unexpected exception occured from timedOut callback", e);
                            }
                        }
                    } else {
                        // entry will be passivated, so we need to obtain an additional lock until the passivation is complete
                        entry.lock.lock();
                        entries.add(entry);

                        entry.setState(EntryState.PASSIVATED);
                        valuesToStore.put(entry.getKey(), entry.getValue());
                    }
                } finally {
                    entry.lock.unlock();
                }
            }

            if (!valuesToStore.isEmpty()) {
                try {
                    storeEntries(valuesToStore);
                } finally {
                    for (Entry entry : entries) {
                        // release the extra passivation lock
                        entry.lock.unlock();
                    }
                }
            }
        }
    }

    private Entry loadEntry(K key) throws Exception {
        PassivationStrategy passivator = getPassivator();
        if (passivator == null) {
            return null;
        }

        V value = null;
        try {
            value = (V) passivator.activate(key);
        } catch (Exception e) {
            logger.error("An unexpected exception occured while reading entries from disk", e);
        }

        if (value == null) {
            return null;
        }

        CacheListener<V> listener = this.getListener();
        if (listener != null) {
            listener.afterLoad(value);
        }
        Entry entry = new Entry(key, value, EntryState.AVAILABLE);
        cache.put(key, entry);
        return entry;
    }

    private void storeEntries(Map<K, V> entriesToStore) {
        CacheListener<V> listener = this.getListener();
        for (Iterator<java.util.Map.Entry<K, V>> iterator = entriesToStore.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry<K, V> entry = iterator.next();

            if (listener != null) {
                try {
                    listener.beforeStore(entry.getValue());
                } catch (Exception e) {
                    iterator.remove();
                    logger.error("An unexpected exception occured from beforeStore callback", e);
                }
            }

        }

        PassivationStrategy passivator = getPassivator();
        if (passivator == null) {
            return;
        }

        try {
            passivator.passivate(entriesToStore);
        } catch (Exception e) {
            logger.error("An unexpected exception occured while writting the entries to disk", e);
        }
    }

    private enum EntryState {
        AVAILABLE, CHECKED_OUT, PASSIVATED, REMOVED
    }

    private class Entry {
        private final K key;
        private final V value;
        private final ReentrantLock lock = new ReentrantLock();
        private EntryState state;
        private long lastAccess;

        private Entry(K key, V value, EntryState state) {
            this.key = key;
            this.value = value;
            this.state = state;
            lastAccess = System.currentTimeMillis();
        }

        private K getKey() {
            assertLockHeld();
            return key;
        }

        private V getValue() {
            assertLockHeld();
            return value;
        }

        private EntryState getState() {
            assertLockHeld();
            return state;
        }

        private void setState(EntryState state) {
            assertLockHeld();
            this.state = state;
        }

        private boolean isTimedOut() {
            assertLockHeld();

            long timeOut = getTimeOut();
            if (timeOut == 0) {
                return false;
            }
            long now = System.currentTimeMillis();
            return (now - lastAccess) > timeOut;
        }

        private void resetTimeOut() {
            assertLockHeld();

            if (getTimeOut() > 0) {
                lastAccess = System.currentTimeMillis();
            }
        }

        private void assertLockHeld() {
            if (!lock.isHeldByCurrentThread()) {
                throw new IllegalStateException("Entry must be locked");
            }
        }
    }
}
