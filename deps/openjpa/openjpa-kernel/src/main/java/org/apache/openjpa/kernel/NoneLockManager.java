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
package org.apache.openjpa.kernel;

/**
 * A lock manager that does not perform any locking.
 *
 * @author Marc Prud'hommeaux
 */
public class NoneLockManager
    extends AbstractLockManager {

    public void lock(OpenJPAStateManager sm, int level, int timeout,
        Object context) {
        // Duplicate code in the 2 lock methods to enforce proper action
        // and avoid unexpected behavior due to method override.
        sm.setLock(Boolean.TRUE);
    }

    public void refreshLock(OpenJPAStateManager sm, int level, int timeout,
        Object context) {
        sm.setLock(Boolean.TRUE);
    }

    public void release(OpenJPAStateManager sm) {
        sm.setLock(null);
    }

    public int getLockLevel(OpenJPAStateManager sm) {
        return LOCK_NONE;
    }
}
