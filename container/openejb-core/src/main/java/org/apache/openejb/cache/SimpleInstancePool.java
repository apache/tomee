/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
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

