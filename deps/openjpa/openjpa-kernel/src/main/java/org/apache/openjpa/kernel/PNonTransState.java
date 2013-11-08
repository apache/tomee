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

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Lifecycle state.
 * Represents a persistent instance that is not transactional, but that
 * allows access to persistent data. This state is reachable only if the
 * RetainState property is set.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
class PNonTransState
    extends PCState {

    private static final Localizer _loc = Localizer.forPackage
        (PNonTransState.class);

    @Override
    void initialize(StateManagerImpl context, PCState previous) {
        if (previous == null)
         return;
        // If our previous state is clean, we don't need to do any sort of cleanup
        if (previous != PCLEAN) {
            // spec says all proxies to second class objects should be reset
            context.proxyFields(true, false);
            context.setDirty(false);
        }
        context.clearSavedFields();
    }

    PCState delete(StateManagerImpl context) {
        context.preDelete();
        if (!context.getBroker().isActive())
            return PNONTRANSDELETED;
        return PDELETED;
    }

    PCState transactional(StateManagerImpl context) {
        // state is discarded when entering the transaction
        if (!context.getBroker().getOptimistic()
            || context.getBroker().getAutoClear() == AutoClear.CLEAR_ALL)
            context.clearFields();
        return PCLEAN;
    }

    PCState release(StateManagerImpl context) {
        return TRANSIENT;
    }

    PCState evict(StateManagerImpl context) {
        return HOLLOW;
    }

    PCState beforeRead(StateManagerImpl context, int field) {
        // state is discarded when entering the transaction
        context.clearFields();
        return PCLEAN;
    }

    PCState beforeWrite(StateManagerImpl context, int field, boolean mutate) {
        return beforeWrite(context, field, mutate, false);
    }

    PCState beforeOptimisticWrite(StateManagerImpl context, int field,
        boolean mutate) {
        if (context.getBroker().getAutoClear() == AutoClear.CLEAR_ALL)
            return beforeWrite(context, field, mutate, true);
        return PDIRTY;
    }

    private PCState beforeWrite(StateManagerImpl context, int field,
        boolean mutate, boolean optimistic) {
        // if this is a direct mutation on an SCO field, we can't clear our
        // fields because that would also null the SCO; depending on whether
        // the user was directly manipulating the field or was using a method,
        // that will result in either an NPE or having the SCO be detached
        // from its owning object, making the user's change have no affect

        if (mutate && !optimistic) {
            Log log = context.getBroker().getConfiguration().getLog
                (OpenJPAConfiguration.LOG_RUNTIME);
            if (log.isWarnEnabled()) {
                log.warn(_loc.get("pessimistic-mutate",
                    context.getMetaData().getField(field),
                    context.getManagedInstance()));
            }
        } else if (!mutate) {
            // state is stored for rollback and fields are reloaded
            if (context.getDirty().length() > 0)
                context.saveFields(true);
            context.clearFields();
            context.load(null, context.LOAD_FGS, null, null, true);
        }
        return PDIRTY;
    }

    PCState beforeNontransactionalWrite(StateManagerImpl context, int field,
        boolean mutate) {
        return PNONTRANSDIRTY;
    }

    boolean isPersistent() {
        return true;
    }
    
    public String toString() {
        return "Persistent-Notransactional";
    }
}

