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
package org.apache.openjpa.persistence;

import javax.persistence.LockModeType;

import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.MixedLockLevels;

/**
 * Helper methods translate between JPA-defined lock mode and OpenJPA 
 * internal lock levels.
 *
 * @author Albert Lee
 * @since 2.0.0
 */
public class MixedLockLevelsHelper implements HintValueConverter {
    /**
     * Translates javax.persistence LockModeType to internal lock level.
     */
    public static int toLockLevel(LockModeType mode) {
        if (mode == null || mode == LockModeType.NONE)
            return MixedLockLevels.LOCK_NONE;
        if (mode == LockModeType.READ)
            return MixedLockLevels.LOCK_READ;
        if (mode == LockModeType.OPTIMISTIC)
            return MixedLockLevels.LOCK_OPTIMISTIC;
        if (mode == LockModeType.WRITE)
            return MixedLockLevels.LOCK_WRITE;
        if (mode == LockModeType.OPTIMISTIC_FORCE_INCREMENT)
            return MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT;
        if (mode == LockModeType.PESSIMISTIC_READ)
            return MixedLockLevels.LOCK_PESSIMISTIC_READ;
        if (mode == LockModeType.PESSIMISTIC_WRITE)
            return MixedLockLevels.LOCK_PESSIMISTIC_WRITE;
        return MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT;
    }
    
    public static int toLockLevel(int mode) {
        switch (mode) {
        case MixedLockLevels.LOCK_OPTIMISTIC:
        case MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT:
        case MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT:
        case MixedLockLevels.LOCK_PESSIMISTIC_READ:
        case MixedLockLevels.LOCK_PESSIMISTIC_WRITE:
        case MixedLockLevels.LOCK_NONE:
        case MixedLockLevels.LOCK_READ:
        case MixedLockLevels.LOCK_WRITE:
        case FetchConfiguration.DEFAULT:
            return mode;
         default:
             throw new IllegalArgumentException("Unknown lock level " + mode);
        }
    }


    /**
     * Translates internal lock level to javax.persistence LockModeType.
     */
    public static LockModeType fromLockLevel(int level) {
        if (level < MixedLockLevels.LOCK_READ)
            return LockModeType.NONE;
        if (level < MixedLockLevels.LOCK_OPTIMISTIC)
            return LockModeType.READ;
        if (level < MixedLockLevels.LOCK_WRITE)
            return LockModeType.OPTIMISTIC;
        if (level < MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT)
            return LockModeType.WRITE;
        if (level < MixedLockLevels.LOCK_PESSIMISTIC_READ)
            return LockModeType.OPTIMISTIC_FORCE_INCREMENT;
        if (level < MixedLockLevels.LOCK_PESSIMISTIC_WRITE)
            return LockModeType.PESSIMISTIC_READ;
        if (level < MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT)
            return LockModeType.PESSIMISTIC_WRITE;
        return LockModeType.PESSIMISTIC_FORCE_INCREMENT;
    }

    public boolean canConvert(Class<?> type) {
        return type == LockModeType.class || type == String.class || type == Integer.class || type == int.class;
    }

    public Object convert(Object original) {
        if (original instanceof LockModeType)
            return MixedLockLevelsHelper.toLockLevel((LockModeType)original);
        if (original instanceof String) {
            try {
                int value = Integer.parseInt(original.toString());
                return MixedLockLevelsHelper.toLockLevel(value);
            } catch (NumberFormatException nfe) {
                if ("none".equalsIgnoreCase(original.toString())) {
                    return MixedLockLevels.LOCK_NONE;
                }
                return MixedLockLevelsHelper.toLockLevel(
                        LockModeType.valueOf(original.toString().toUpperCase().replace('-', '_')));
            }
        }
        if (original instanceof Integer) {
            return MixedLockLevelsHelper.toLockLevel((Integer)original);
        }
        
        throw new IllegalArgumentException("can not convert " + original + " of " + original.getClass());
    }
}
