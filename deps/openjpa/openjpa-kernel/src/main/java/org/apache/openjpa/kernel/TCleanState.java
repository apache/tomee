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
 * Represents a transient instance that is managed by a StateManager and
 * may be participating in the current	transaction, but has not yet been
 * modified.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
class TCleanState
    extends PCState {

    @Override
    void initialize(StateManagerImpl context, PCState previous) {
        if (previous == null)
            return;
        
        // need to replace the second class objects with proxies that
        // listen for dirtying so we can track changes to these objects
        context.proxyFields(true, false);
        
        context.clearSavedFields();
        context.setLoaded(true);
        context.setDirty(false);
    }

    PCState persist(StateManagerImpl context) {
        return (context.getBroker().isActive()) ? PNEW : PNONTRANSNEW;
    }

    PCState delete(StateManagerImpl context) {
        return error("transient", context);
    }

    PCState nontransactional(StateManagerImpl context) {
        return TRANSIENT;
    }

    PCState beforeWrite(StateManagerImpl context, int field, boolean mutate) {
        return TDIRTY;
    }

    PCState beforeOptimisticWrite(StateManagerImpl context, int field,
        boolean mutate) {
        return TDIRTY;
    }
    
    public String toString() {
        return "Transient-Clean";
    }
}

