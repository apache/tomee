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

import javax.persistence.PessimisticLockScope;

import org.apache.openjpa.kernel.LockScopes;

/**
 * Helper methods translate between JPA-defined pessimistic lock scope and
 * OpenJPA internal lock scope levels.
 * 
 * @since 2.0.0
 */
public class LockScopesHelper {
    /**
     * Translates javax.persistence LockModeType to internal lock level.
     */
    public static int toLockScope(PessimisticLockScope scope) {
        if (scope == null || scope == PessimisticLockScope.NORMAL)
            return LockScopes.LOCKSCOPE_NORMAL;
        return LockScopes.LOCKSCOPE_EXTENDED;
    }

    /**
     * Translates internal lock level to javax.persistence LockModeType.
     */
    public static PessimisticLockScope fromLockScope(int level) {
        if (level < LockScopes.LOCKSCOPE_EXTENDED)
            return PessimisticLockScope.NORMAL;
        return PessimisticLockScope.EXTENDED;
    }
}
