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
package org.apache.openejb.util;

import java.util.LinkedList;
import java.io.Serializable;

import org.apache.openejb.cache.InstanceFactory;
import org.apache.openejb.cache.InstancePool;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class SoftLimitedInstancePool implements InstancePool, Serializable {
    private final InstanceFactory factory;
    private final int maxSize;
    private transient final LinkedList pool;

    public SoftLimitedInstancePool(final InstanceFactory factory, final int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        pool = new LinkedList();
    }

    public Object acquire() throws Exception {
        // get the instance from the pool if possible
        synchronized (this) {
            if (!pool.isEmpty()) {
                return pool.removeFirst();
            }
        }

        // if there was nothing in the pool, we will just create one
        return factory.createInstance();
    }

    public boolean release(Object instance) {
        synchronized (this) {
            // if we are under the limit put it back in the pool at the head
            // this encourages reuse of the same instances to improve memory management
            if (pool.size() < maxSize) {
                pool.addFirst(instance);
                return true;
            }
        }

        // we aren't going to keep this instance, shut it down
        factory.destroyInstance(instance);
        return false;
    }

    public void remove(Object instance) {
        // You broke one, so you get to take the hit and create a replacement
        // Do this outside the synchronized block because the factory can take a long time.
        try {
            instance = factory.createInstance();
        } catch (Exception ignored) {
            // We ignore this as we want the app to see the Exception that
            // caused the instance to be discarded in the first place
            // If the problem is serious, then the next user will see
            // it again when they acquire a new instance
            return;
        }

        // Add the replacement to the pool
        // This may cause us to exceed maxSize, but we'll put it in anyway given
        // we went to the trouble to create it.
        synchronized (this) {
            // add this new instance to the end
            // we prefer other users get older instances first
            pool.addLast(instance);
        }
    }

    private Object readResolve() {
        return new SoftLimitedInstancePool(factory, maxSize);
    }
}

