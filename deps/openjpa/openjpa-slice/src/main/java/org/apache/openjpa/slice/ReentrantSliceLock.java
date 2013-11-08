/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.slice;

import java.util.concurrent.locks.ReentrantLock;

/**
 * A reentrant lock that lets a child to work with the parent's lock.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class ReentrantSliceLock extends ReentrantLock {

    public ReentrantSliceLock() {
    }

    public ReentrantSliceLock(boolean fair) {
        super(fair);
    }
    
    /**
     * Locks only for parent thread and let the child use parent's lock. 
     */
    @Override
    public void lock() {
        if (Thread.currentThread() instanceof SliceThread) 
            return;
        super.lock();
    }

    /**
     * Unlocks only if parent thread. 
     */
    @Override
    public void unlock() {
        if (Thread.currentThread() instanceof SliceThread) 
            return;
        super.unlock();
    }
}
