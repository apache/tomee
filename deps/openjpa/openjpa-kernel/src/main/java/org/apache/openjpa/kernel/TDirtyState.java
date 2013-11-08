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
 * Represents a transient instance that is participating in the current
 * transaction, and has been modified.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
class TDirtyState
    extends PCState {

    @Override
    void initialize(StateManagerImpl context, PCState previous) {
        context.saveFields(false);
        context.setLoaded(true);
        context.setDirty(true);
    }

    PCState commit(StateManagerImpl context) {
        return TCLEAN;
    }

    PCState commitRetain(StateManagerImpl context) {
        return TCLEAN;
    }

    PCState rollback(StateManagerImpl context) {
        return TCLEAN;
    }

    PCState rollbackRestore(StateManagerImpl context) {
        context.restoreFields();
        return TCLEAN;
    }

    PCState persist(StateManagerImpl context) {
        return (context.getBroker().isActive()) ? PNEW : PNONTRANSNEW;
    }

    PCState delete(StateManagerImpl context) {
        return error("transient", context);
    }

    PCState nontransactional(StateManagerImpl context) {
        return error("dirty", context);
    }

    boolean isTransactional() {
        return true;
    }

    boolean isDirty() {
        return true;
    }
    
    public String toString() {
        return "Transient-Dirty";
    }
}

