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
package org.apache.openejb.cache;

import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.Semaphore;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class SimpleInstancePool implements InstancePool {
    private LinkedList pool;
    private int allocated;
    private Semaphore semaphore;

    private InstanceFactory factory;
    private int maxSize;
    private boolean hardLimit;

    public SimpleInstancePool(final InstanceFactory factory, final int maxSize, final boolean hardLimit) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.hardLimit = hardLimit;

        pool = new LinkedList();
        if (hardLimit) {
            semaphore = new Semaphore(maxSize);
        }
    }

    public void fill() throws Exception {
        synchronized (this) {
            while (pool != null && allocated + pool.size() < maxSize) {
                Object instance = factory.createInstance();
                pool.addFirst(instance);
            }
        }
    }

    public Object acquire() throws Exception {
        // if we are using hard limits we need to acquire a permit
        if (hardLimit) {
            semaphore.acquire();
        }

        // get the instance from the pool is possible
        Object instance = null;
        synchronized (this) {
            allocated++;

            // if we have not stopped pooling and there is one in the pool, use it
            if (pool != null && !pool.isEmpty()) {
                instance = pool.removeFirst();
            }
        }

        // didn't get an instance? create a new one
        if (instance == null) {
            instance = factory.createInstance();
        }

        return instance;
    }

    public boolean release(Object instance) {
        boolean reinserted = false;
        synchronized (this) {
            // if we have not stopped pooling and we are under the limit put it back in the pool
            if (pool != null && allocated + pool.size() < maxSize) {
                pool.addFirst(instance);
                reinserted = true;
            }
            allocated--;
        }

        // if we are using hard limits we need to release our permit
        if (hardLimit) {
            semaphore.release();
        }
        return reinserted;
    }

    public void remove(Object instance) {
        instance = null;

        // Create a new one... You have done nothing good for the pool, so at least try to
        // create a replacement instance for the one you broke
        // Do this outside the synchronized block because the factory can take a long time.
        try {
            instance = factory.createInstance();
        } catch (Exception ignored) {
            // well that didn't work either
        }

        synchronized (this) {
            // Always add... if we have a hard limit, we will be down one, and if we have a soft
            // limit, an extra one is no big deal.  If we have stopped pooling, then it is a
            // wasted creation.
            if (pool != null) {
                pool.addFirst(instance);
            }
            allocated--;
        }

        // if we are using hard limits we need to release our permit
        if (hardLimit) {
            semaphore.release();
        }
    }

    public List stopPooling() {
        synchronized (this) {
            List temp = pool;
            pool = null;
            return temp;
        }
    }

    public void startPooling() {
        synchronized (this) {
            if (pool == null) {
                pool = new LinkedList();
            }
        }
    }


    /**
     * Return the size of the pool.
     *
     * @return the size of the pool
     */
    public int getSize() {
        synchronized (this) {
            return allocated + pool.size();
        }
    }

    /**
     * Gets the number of allocated instances.  This may be larger then the max if the pools
     * is using a soft limit.
     */
    public int getAllocatedSize() {
        synchronized (this) {
            return allocated;
        }
    }

    /**
     * Get the maximum size of the pool.
     *
     * @return the size of the pool
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Determines if this pool has a hard limit.
     *
     * @return true if this pool is using a hard limit
     */
    public boolean isHardLimit() {
        return hardLimit;
    }
}

