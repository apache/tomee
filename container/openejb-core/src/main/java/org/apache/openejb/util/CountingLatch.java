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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
*/
public class CountingLatch {

     private final Sync sync;

     public CountingLatch() {
         this.sync = new Sync();
     }

    public void await() throws InterruptedException {
         sync.acquireSharedInterruptibly(1);
     }

     public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
         return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
     }

     public void countDown() {
         sync.releaseShared(-1);
     }

     public void countUp() {
         sync.releaseShared(1);
     }

     public long getCount() {
         return sync.getCount();
     }

     private static final class Sync extends AbstractQueuedSynchronizer {
         private Sync() {
             setState(0);
         }

         public boolean tryReleaseShared(int releases) {
             while (true) {
                 int count = getState();

                 int next = count + releases;

                 if (next < 0) return false;

                 if (compareAndSetState(count, next)) {
                     return next == 0;
                 }
             }
         }

         public int tryAcquireShared(int acquires) {
             return getState() == 0 ? 1: -1;
         }

         int getCount() {
             return getState();
         }
     }
 }
