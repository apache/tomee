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
 * Represents an embedded instance that is not transactional, but that
 * allows access to persistent data. This state is reachable only if the
 * retainValues flag of the broker is set.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
class ENonTransState
    extends PCState {

    @Override
    void initialize(StateManagerImpl context, PCState previous) {
        if (previous == null)
            return;
        
        // spec says all proxies to second class objects should be reset
        context.proxyFields(true, true);
        
        context.setDirty(false);
        context.clearSavedFields();
    }

    PCState delete(StateManagerImpl context) {
        context.preDelete();
        return EDELETED;
    }

    PCState transactional(StateManagerImpl context) {
        // state is discarded when entering the transaction
        if (!context.getBroker().getOptimistic())
            context.clearFields();
        return ECLEAN;
    }

    PCState release(StateManagerImpl context) {
        return TRANSIENT;
    }

    PCState evict(StateManagerImpl context) {
        return TRANSIENT;
    }

    PCState beforeRead(StateManagerImpl context, int field) {
        return error("embed-ref", context);
    }

    PCState beforeWrite(StateManagerImpl context, int field, boolean mutate) {
        return error("embed-ref", context);
    }

    PCState beforeOptimisticWrite(StateManagerImpl context, int field,
        boolean mutate) {
        return EDIRTY;
    }

    boolean isPersistent() {
        return true;
    }
    
    public String toString() {
        return "Embedded-Nontransactional";
    }
}

