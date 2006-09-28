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

/**
 * Instance pool holds a number if instances that can be borrowed from the pool.
 * Once an instance has been acquired it must be returned to the pool by calling
 * release, which puts the instance back into the pool, or by calling remove,
 * which marks the instance as no longer used but does not put it back in the pool.
 * An implementation will have to refill the pool as entries are removed.
 *
 *
 * @version $Revision$ $Date$
 */
public interface InstancePool {
    /**
     * Get an instance from the pool.  This method may block indefinately if the pool has a
     * strict limit.
     *
     * @return an instance
     * @throws InterruptedException if pool is using hard limits and thread was interrupted
     * while waiting for an instance to become available
     * @throws Exception if object demand object construction was required and
     * an error occured during construction
     */
    Object acquire() throws Exception;

    /**
     * Releases the hold on the instance.  This method may or may not reinsert the instance
     * into the pool. This method can not block.
     *
     * @param instance the instance to return to the pool
     * @return true is the instance was reinserted into the pool.
     */
    boolean release(Object instance);

    /**
     * Drop an instance permanently from the pool.  The instance will never be used again.
     * This method can not block.
     *
     * @param instance the instance to discard
     */
    void remove(Object instance);
}

