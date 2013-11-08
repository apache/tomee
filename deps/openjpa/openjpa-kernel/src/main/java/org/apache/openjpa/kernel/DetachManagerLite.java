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

import java.util.BitSet;
import java.util.Collection;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.Proxy;

/**
 * Handles detaching instances.
 */
public class DetachManagerLite {
    private final boolean _detachProxies;
    private final TransferFieldManager _tsm;

    public DetachManagerLite(OpenJPAConfiguration conf) {       
        _detachProxies = conf.getDetachStateInstance().getDetachProxyFields();
        _tsm = new TransferFieldManager();
    }

    /**
     * This method will detach all provided StateManagers in place.
     * 
     * @param states
     *            The StateManagers to be detached.
     */
    public void detachAll(Collection<StateManagerImpl> states) {
        
        for (StateManagerImpl sm : states) {
            ClassMetaData cmd = sm.getMetaData();
            if (sm.isPersistent() && cmd.isDetachable()) {
                PersistenceCapable pc = sm.getPersistenceCapable();
                if (pc.pcIsDetached() == false) {
                    // Detach proxy fields.
                    BitSet loaded = sm.getLoaded();
                    for (FieldMetaData fmd : cmd.getProxyFields()) {
                        if (loaded.get(fmd.getIndex())) {
                            detachProxyField(fmd, pc, sm, _tsm);
                        }
                    }
                    pc.pcReplaceStateManager(null);
                }
            }
        }
    }

    /**
     * Detach the provided proxy field.
     * 
     * @param fmd
     *            The field to be detached.
     * @param pc
     *            The PersistenceCapable that the field belongs to.
     * @param sm
     *            The StateManagerImpl that the PersistenceCapable belongs to.
     */
    private void detachProxyField(FieldMetaData fmd, PersistenceCapable pc, 
        StateManagerImpl sm, TransferFieldManager fm) {
        
        int fieldIndex = fmd.getIndex();
        if (fmd.isLRS() == true) {
            // need to null out LRS fields.
            nullField(fieldIndex, pc, sm, fm);
        } else {
            Object o = sm.fetchObject(fieldIndex);
            if (o instanceof Proxy) {
                // Get unproxied object and replace
                Proxy proxy = (Proxy) o;
                if (!_detachProxies) {
                    // Even if we're not detaching proxies, we need to remove the reference to the SM.
                    proxy.setOwner(null, -1);
                    return;
                }
                Object unproxied = proxy.copy(proxy);
                fm.storeObjectField(fieldIndex, unproxied);
                sm.replaceField(pc, fm, fieldIndex);
                fm.clear();
                // clean up old proxy
                proxy.setOwner(null, -1);
                if (proxy.getChangeTracker() != null) {
                    proxy.getChangeTracker().stopTracking();
                }
            }
        }
    }

    /**
     * Private worker method that replaces the value at fieldIndex in sm with null.
     * 
     * @param fieldIndex
     *            The index of the field to be nulled out.
     * @param pc
     *            The PersistenceCapable that the field belongs to.
     * @param sm
     *            The StateManagerImpl that the PersistenceCapable belongs to.
     */
    private void nullField(int fieldIndex, PersistenceCapable pc, StateManagerImpl sm, TransferFieldManager fm) {
        fm.storeObjectField(fieldIndex, null);
        sm.replaceField(pc, fm, fieldIndex);
        fm.clear();
    }
}
