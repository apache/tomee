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
package org.apache.openejb.core.stateful;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultLockFactory implements LockFactory {
    public static class DefaultLock implements StatefulLock {
        private final ReentrantLock lock = new ReentrantLock();

        @Override
        public void lock() {
            lock.lock();
        }

        @Override
        public void unlock() {
            lock.unlock();
        }

        @Override
        public boolean isHeldByCurrentThread() {
            return lock.isHeldByCurrentThread();
        }

        @Override
        public boolean tryLock() {
            return lock.tryLock();
        }

        @Override
        public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
            return lock.tryLock(time, unit);
        }
    }

    @Override
    public StatefulLock newLock(final String beanId) {
        return new DefaultLock();
    }

    @Override
    public void setContainer(final StatefulContainer statefulContainer) {
        // no-op
    }
}
