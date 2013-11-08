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
 * Lifecycle state.
 * Represents an instance that was made persistent within the
 * current	transaction, flushed, and subsequently deleted.
 *
 * @author Patrick Linskey
 */
@SuppressWarnings("serial")
class PNewFlushedDeletedState
    extends PCState {

    PCState flush(StateManagerImpl context) {
        return PNEWFLUSHEDDELETEDFLUSHED;
    }

    PCState commit(StateManagerImpl context) {
        return TRANSIENT;
    }

    PCState commitRetain(StateManagerImpl context) {
        return TRANSIENT;
    }

    PCState rollback(StateManagerImpl context) {
        return TRANSIENT;
    }

    PCState rollbackRestore(StateManagerImpl context) {
        context.restoreFields();
        return TRANSIENT;
    }

    PCState persist(StateManagerImpl context) {
        return PNEW;
    }

    PCState nontransactional(StateManagerImpl context) {
        return error("deleted", context);
    }

    PCState release(StateManagerImpl context) {
        return error("deleted", context);
    }

    PCState beforeWrite(StateManagerImpl context, int field, boolean mutate) {
        return error("deleted", context);
    }

    PCState beforeOptimisticWrite(StateManagerImpl context, int field,
        boolean mutate) {
        return error("deleted", context);
    }

    boolean isTransactional() {
        return true;
    }

    boolean isPersistent() {
        return true;
    }

    boolean isNew() {
        return true;
    }

    boolean isDeleted() {
        return true;
    }

    boolean isDirty() {
        return true;
    }
    
    public String toString() {
        return "Persistent-New-Flushed-Deleted";
    }
}
