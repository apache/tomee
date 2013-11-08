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
package org.apache.openjpa.slice;

import org.apache.openjpa.kernel.FinalizingBrokerImpl;
import org.apache.openjpa.kernel.OpCallbacks;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.QueryImpl;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.slice.jdbc.TargetFetchConfiguration;
import org.apache.openjpa.util.OpenJPAId;

/**
 * A specialized Broker to associate slice identifiers with the StateManagers as
 * they are persisted in a cascade. This intervention helps the user to define
 * distribution policy only for root instances i.e. the instances that are
 * explicit argument to persist() call. The cascaded instances are assigned the
 * same slice to honor collocation constraint.
 * 
 * @author Pinaki Poddar
 * 
 */
@SuppressWarnings("serial")
public class DistributedBrokerImpl extends FinalizingBrokerImpl implements DistributedBroker {
    private transient String _rootSlice;
    private transient DistributedConfiguration _conf;
    private final ReentrantSliceLock _lock;

    private static final Localizer _loc = Localizer.forPackage(DistributedBrokerImpl.class);

    public DistributedBrokerImpl() {
        super();
        _lock = new ReentrantSliceLock();
    }

    public DistributedConfiguration getConfiguration() {
        if (_conf == null) {
            _conf = (DistributedConfiguration) super.getConfiguration();
        }
        return _conf;
    }

    public DistributedStoreManager getDistributedStoreManager() {
        return (DistributedStoreManager) getStoreManager().getInnermostDelegate();
    }

    public TargetFetchConfiguration getFetchConfiguration() {
        return (TargetFetchConfiguration) super.getFetchConfiguration();
    }

    /**
     * Assigns slice identifier to the resultant StateManager as initialized by
     * the super class implementation. The slice identifier is decided by
     * {@link DistributionPolicy} for given <code>pc</code> if it is a root
     * instance i.e. the argument of the user application's persist() call. The
     * cascaded instances are detected by non-empty status of the current
     * operating set. The slice is assigned only if a StateManager has never
     * been assigned before.
     */
    @Override
    public OpenJPAStateManager persist(Object pc, Object id, boolean explicit, OpCallbacks call) {
        OpenJPAStateManager sm = getStateManager(pc);
        SliceInfo info = null;
        boolean replicated = SliceImplHelper.isReplicated(pc, getConfiguration());
        if (getOperatingSet().isEmpty() && !SliceImplHelper.isSliceAssigned(sm)) {
            info = SliceImplHelper.getSlicesByPolicy(pc, getConfiguration(), this);
            if (info != null) {
                _rootSlice = info.getSlices()[0];
            }
        }
        if (sm == null) {
            sm = super.persist(pc, id, explicit, call);
        }
        if (!SliceImplHelper.isSliceAssigned(sm)) {
            if (info == null) {
                info = replicated 
                     ? SliceImplHelper.getSlicesByPolicy(pc, getConfiguration(), this) 
                     : _rootSlice != null ? new SliceInfo(_rootSlice) : null;
            }
            if (info != null)
                info.setInto(sm);
        }
        return sm;
    }
    
    @Override
    protected void setStateManager(Object id, StateManagerImpl sm, int status) {
        try {
            super.setStateManager(id, sm, status);
        } catch (Exception e) {
            if (status == 0) { // STATUS_INIT
                // ignore
            }
        }
    }    

    @Override
    public boolean endOperation() {
        try {
            return super.endOperation();
        } catch (Exception ex) {

        }
        return true;
    }

    /**
     * Create a new query.
     */
    protected QueryImpl newQueryImpl(String lang, StoreQuery sq) {
        return new DistributedQueryImpl(this, lang, sq);
    }

    /**
     * Always uses lock irrespective of super's multi-threaded settings.
     */
    @Override
    public void lock() {
        _lock.lock();
    }

    @Override
    public void unlock() {
        _lock.unlock();
    }

    /**
     * A virtual datastore need not be opened.
     */
    @Override
    public void beginStore() {
    }
    
    @Override
    protected void flush(int reason) {
    	setStatusFlag(2 << 8);
    	super.flush(reason);
    }

    /**
     * Overrides to target specific slices for find() calls.
     */
    @Override
    public Object processArgument(Object oid) {
        TargetFetchConfiguration fetch = getFetchConfiguration();
        if (!fetch.isExplicitTarget()) {
            FinderTargetPolicy policy = _conf.getFinderTargetPolicyInstance();
            if (policy != null) {
                if (oid instanceof OpenJPAId) {
                    String[] targets = policy.getTargets(((OpenJPAId) oid).getType(), 
                            ((OpenJPAId) oid).getIdObject(),
                            _conf.getActiveSliceNames(), this);
                    fetch.setTargets(targets);
                }
            }
        }
        return super.processArgument(oid);
    }
}
