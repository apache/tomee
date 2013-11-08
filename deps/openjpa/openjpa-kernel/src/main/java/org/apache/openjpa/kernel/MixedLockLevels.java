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
 * Defines lock levels used for MixedLockManager.
 *
 * @author Albert Lee
 * @since 2.0.0
 */
public interface MixedLockLevels extends LockLevels {

    /**
     * Generic optimistic read lock level. Value of 15.
     *
     */
    public static final int LOCK_OPTIMISTIC = LOCK_READ + 5;

    /**
     * Generic optimistic write lock level. Value of 25.
     */
    public static final int LOCK_OPTIMISTIC_FORCE_INCREMENT =
        LockLevels.LOCK_WRITE + 5;

    /**
     * Generic pessimistic read lock level. Value of 30.
     */
    public static final int LOCK_PESSIMISTIC_READ = 30;

    /**
     * Generic pessimistic write lock level. Value of 40.
     */
    public static final int LOCK_PESSIMISTIC_WRITE = 40;

    /**
     * Generic pessimistic force increment level. Value of 50.
     */
    public static final int LOCK_PESSIMISTIC_FORCE_INCREMENT = 50;

}
