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
package org.apache.openjpa.slice.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.kernel.ConnectionInfo;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.kernel.JDBCStoreManager;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.ResultSetResult;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.Seq;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.lib.rop.MergedResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.slice.DistributedConfiguration;
import org.apache.openjpa.slice.DistributedStoreManager;
import org.apache.openjpa.slice.Slice;
import org.apache.openjpa.slice.SliceImplHelper;
import org.apache.openjpa.slice.SliceInfo;
import org.apache.openjpa.slice.SlicePersistence;
import org.apache.openjpa.slice.SliceThread;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.StoreException;

/**
 * A Store manager for multiple physical databases referred as <em>slice</em>.
 * This receiver behaves like a Transaction Manager as it implements two-phase
 * commit protocol if all the component slices is XA-complaint. The actions are
 * delegated to the underlying slices. The actions are executed in parallel
 * threads whenever possible such as flushing or query. <br>
 * 
 * @author Pinaki Poddar
 * 
 */
class DistributedJDBCStoreManager extends JDBCStoreManager 
    implements DistributedStoreManager {
    private final List<SliceStoreManager> _slices;
    private JDBCStoreManager _master;
    private final DistributedJDBCConfiguration _conf;
    private static final Localizer _loc = Localizer.forPackage(DistributedJDBCStoreManager.class);

    /**
     * Constructs a set of child StoreManagers each connected to a physical
     * DataSource.
     * 
     * The supplied configuration carries multiple URL for underlying physical
     * slices. The first slice is referred as <em>master</em> and is used to
     * get Sequence based entity identifiers.
     */
    public DistributedJDBCStoreManager(DistributedJDBCConfiguration conf) {
        super();
        _conf = conf;
        _slices = new ArrayList<SliceStoreManager>();
        List<Slice> slices = conf.getSlices(Slice.Status.ACTIVE);
        Slice masterSlice = conf.getMasterSlice();
        for (Slice slice : slices) {
            SliceStoreManager store = new SliceStoreManager(slice);
            _slices.add(store);
            if (slice == masterSlice) {
                _master = store;
            }
        }
    }

    public DistributedJDBCConfiguration getConfiguration() {
        return _conf;
    }
    
    public SliceStoreManager getSlice(int i) {
    	return _slices.get(i);
    }
    
    public SliceStoreManager addSlice(Slice slice) {
        SliceStoreManager result = new SliceStoreManager(slice);
        result.setContext(getContext(), (JDBCConfiguration)slice.getConfiguration());
        _slices.add(result);
        return result;
    }

    /**
     * Decides the index of the StoreManager by first looking at the
     * implementation data. If no implementation data is found, then estimates 
     * targets slices by using additional connection info. If no additional
     * connection info then calls back to user-defined policy. 
     */
    protected SliceInfo findSliceNames(OpenJPAStateManager sm, Object edata) {
        if (SliceImplHelper.isSliceAssigned(sm))
            return SliceImplHelper.getSliceInfo(sm);
        SliceInfo result = null;
        PersistenceCapable pc = sm.getPersistenceCapable();
        Object ctx = getContext();
        if (_conf.isReplicated(sm.getMetaData().getDescribedType())) {
            result = SliceImplHelper.getSlicesByPolicy(pc, _conf, ctx);
        } else {
            String origin = estimateSlice(sm, edata);
            if (origin == null) {
                result = SliceImplHelper.getSlicesByPolicy(pc, _conf, ctx);
            } else {
                result = new SliceInfo(origin);
            }
        }
        return result;
    }
    
    private void assignSlice(OpenJPAStateManager sm, String hint) {
        if (_conf.isReplicated(sm.getMetaData().getDescribedType())) {
            SliceImplHelper.getSlicesByPolicy(sm, _conf, getContext())
                .setInto(sm);
            return;
        }
        new SliceInfo(hint).setInto(sm);
    }
    
    /**
     * The additional edata is used, if possible, to find the StoreManager
     * managing the given StateManager. If the additional data is unavailable
     * then return null.
     * 
     */
    private String estimateSlice(OpenJPAStateManager sm, Object edata) {
        if (edata == null || !(edata instanceof ConnectionInfo))
            return null;

        Result result = ((ConnectionInfo) edata).result;
        if (result instanceof ResultSetResult) {
            JDBCStore store = ((ResultSetResult) result).getStore();
            for (SliceStoreManager slice : _slices) {
                if (slice == store) {
                    return slice.getName();
                }
            }
        }
        return null; 
    }

    /**
     * Selects child StoreManager(s) where the given instance resides.
     */
    private StoreManager selectStore(OpenJPAStateManager sm, Object edata) {
        String[] targets = findSliceNames(sm, edata).getSlices();
        for (String target : targets) {
        	SliceStoreManager slice = lookup(target);
        	if (slice == null)
        	    throw new InternalException(_loc.get("wrong-slice", target,
        	            sm));
        	return slice;
        }
        return null;
    }

    public boolean assignField(OpenJPAStateManager sm, int field,
            boolean preFlush) {
        return selectStore(sm, null).assignField(sm, field, preFlush);
    }

    public boolean assignObjectId(OpenJPAStateManager sm, boolean preFlush) {
        return _master.assignObjectId(sm, preFlush);
    }

    public void beforeStateChange(OpenJPAStateManager sm, PCState fromState,
            PCState toState) {
        _master.beforeStateChange(sm, fromState, toState);
    }

    public void beginOptimistic() {
        for (SliceStoreManager slice : _slices)
            slice.beginOptimistic();
    }

    public boolean cancelAll() {
        boolean ret = true;
        for (SliceStoreManager slice : _slices)
            ret = slice.cancelAll() & ret;
        return ret;
    }

    public int compareVersion(OpenJPAStateManager sm, Object v1, Object v2) {
        return selectStore(sm, null).compareVersion(sm, v1, v2);
    }

    public Object copyDataStoreId(Object oid, ClassMetaData meta) {
        return _master.copyDataStoreId(oid, meta);
    }

    public ResultObjectProvider executeExtent(ClassMetaData meta,
            boolean subclasses, FetchConfiguration fetch) {
        int i = 0;
        List<SliceStoreManager> targets = getTargets(fetch);
        ResultObjectProvider[] tmp = new ResultObjectProvider[targets.size()];
        for (SliceStoreManager slice : targets) {
            tmp[i++] = slice.executeExtent(meta, subclasses, fetch);
        }
        return new MergedResultObjectProvider(tmp);
    }

    public boolean exists(OpenJPAStateManager sm, Object edata) {
    	String origin = null;
        for (SliceStoreManager slice : _slices) {
            if (slice.exists(sm, edata)) {
            	origin = slice.getName();
            	break;
            }
        }
        if (origin != null)
            assignSlice(sm, origin);
        return origin != null;
    }

    
    /**
     * Flush the given StateManagers after binning them to respective physical
     * slices.
     */
    public Collection flush(Collection sms) {
        Collection exceptions = new ArrayList();
        List<Future<Collection>> futures = new ArrayList<Future<Collection>>();
        Map<String, StateManagerSet> subsets = bin(sms, null);
        Collection<StateManagerSet> remaining = 
            new ArrayList<StateManagerSet>(subsets.values());
        ExecutorService threadPool = SliceThread.getPool();
        for (int i = 0; i < _slices.size(); i++) {
            SliceStoreManager slice = _slices.get(i);
            StateManagerSet subset = subsets.get(slice.getName());
            if (subset.isEmpty())
                continue;
            if (subset.containsReplicated()) {
                Map<OpenJPAStateManager, Object> oldVersions = cacheVersion(
                    subset.getReplicated());
            	collectException(slice.flush(subset), exceptions);
                remaining.remove(subset);
            	rollbackVersion(subset.getReplicated(), oldVersions, remaining);
            } else {
            	futures.add(threadPool.submit(new Flusher(slice, subset)));
            }
        }
        for (Future<Collection> future : futures) {
            try {
            	collectException(future.get(), exceptions);
            } catch (InterruptedException e) {
                throw new StoreException(e);
            } catch (ExecutionException e) {
                throw new StoreException(e.getCause());
            }
        }
        
	    return exceptions;
    }
    
    private void collectException(Collection error,  Collection holder) {
        if (!(error == null || error.isEmpty())) {
        	holder.addAll(error);
        }
    }
    
    @Override
    public void commit() {
    	for (SliceStoreManager slice : _slices) {
    		slice.commit();
    	}
    }
    
    @Override
    public void rollback() {
    	for (SliceStoreManager slice : _slices) {
    		slice.rollback();
    	}
    }
    
    /**
     * Collect the current versions of the given StateManagers.
     */
    private Map<OpenJPAStateManager, Object> cacheVersion(
        List<OpenJPAStateManager> sms) {
        Map<OpenJPAStateManager, Object> result = 
            new HashMap<OpenJPAStateManager, Object>();
        for (OpenJPAStateManager sm : sms)
            result.put(sm, sm.getVersion());
        return result;
    }
    
    /**
     * Sets the version of the given StateManagers from the cached versions.
     * Provided that the StateManager does not appear in the FlusSets of the
     * remaining.
     */
    private void rollbackVersion(List<OpenJPAStateManager> sms, 
        Map<OpenJPAStateManager, Object> oldVersions, 
        Collection<StateManagerSet> reminder) {
        if (reminder.isEmpty())
            return;
        for (OpenJPAStateManager sm : sms) {
            if (occurs(sm, reminder))
              sm.setVersion(oldVersions.get(sm));
        }
    }
    
    boolean occurs(OpenJPAStateManager sm, 
        Collection<StateManagerSet> reminder) {
        for (StateManagerSet set : reminder)
            if (set.contains(sm))
                return true;
        return false;
    }
    
    /**
     * Separate the given list of StateManagers in separate lists for each slice
     * by the associated slice identifier of each StateManager.
     */
    private Map<String, StateManagerSet> bin(Collection sms, Object edata) {
        Map<String, StateManagerSet> subsets =  new HashMap<String, StateManagerSet>();
        for (SliceStoreManager slice : _slices) {
            subsets.put(slice.getName(), new StateManagerSet(_conf));
        }
        for (Object x : sms) {
            OpenJPAStateManager sm = (OpenJPAStateManager) x;
            String[] targets = findSliceNames(sm, edata).getSlices();
           	for (String slice : targets) {
            	subsets.get(slice).add(sm);
            }
        }
        return subsets;
    }

    public Object getClientConnection() {
        return _master.getClientConnection();
    }

    public Seq getDataStoreIdSequence(ClassMetaData forClass) {
        return _master.getDataStoreIdSequence(forClass);
    }

    public Class<?> getDataStoreIdType(ClassMetaData meta) {
        return _master.getDataStoreIdType(meta);
    }

    public Class<?> getManagedType(Object oid) {
        return _master.getManagedType(oid);
    }

    public Seq getValueSequence(FieldMetaData forField) {
        return _master.getValueSequence(forField);
    }

    public boolean initialize(OpenJPAStateManager sm, PCState state,
            FetchConfiguration fetch, Object edata) {
        if (edata instanceof ConnectionInfo) {
            String origin = estimateSlice(sm, edata);
            if (origin != null) {
                if (lookup(origin).initialize(sm, state, fetch, edata)) {
                    assignSlice(sm, origin);
                    return true;
                }
            }
        }
        // not a part of Query result load. Look into the slices till found
        List<SliceStoreManager> targets = getTargets(fetch);
        for (SliceStoreManager slice : targets) {
            if (slice.initialize(sm, state, fetch, edata)) {
                assignSlice(sm, slice.getName());
                return true;
            }
        }
        return false;
    }

    public boolean load(OpenJPAStateManager sm, BitSet fields,
            FetchConfiguration fetch, int lockLevel, Object edata) {
        return selectStore(sm, edata).load(sm, fields, fetch, lockLevel, edata);
    }

    public Collection loadAll(Collection sms, PCState state, int load,
            FetchConfiguration fetch, Object edata) {
        Map<String, StateManagerSet> subsets = bin(sms, edata);
        Collection result = new ArrayList();
        for (SliceStoreManager slice : _slices) {
            StateManagerSet subset = subsets.get(slice.getName());
            if (subset.isEmpty())
                continue;
            Collection tmp = slice.loadAll(subset, state, load, fetch, edata);
            if (tmp != null && !tmp.isEmpty())
                result.addAll(tmp);
        }
        return result;
    }

    public Object newDataStoreId(Object oidVal, ClassMetaData meta) {
        return _master.newDataStoreId(oidVal, meta);
    }

    /**
     * Construct a distributed query to be executed against all the slices.
     */
    public StoreQuery newQuery(String language) {
    	if (QueryLanguages.LANG_SQL.equals(language)) {
    		DistributedSQLStoreQuery ret = new DistributedSQLStoreQuery(this);
            for (SliceStoreManager slice : _slices) {
                ret.add(slice.newQuery(language));
            }
            return ret;
    	}
        ExpressionParser parser = QueryLanguages.parserForLanguage(language);
        if (parser == null) {
    		throw new UnsupportedOperationException("Language [" + language + "] not supported");
        } 

        DistributedStoreQuery ret = new DistributedStoreQuery(this, parser);
        for (SliceStoreManager slice : _slices) {
            ret.add(slice.newQuery(language));
        }
        return ret;
    }
    
    @Override
    public FetchConfiguration newFetchConfiguration() {
        return new TargetFetchConfiguration();
    }


    /**
     * Sets the context for this receiver and all its underlying slices.
     */
    public void setContext(StoreContext ctx) {
        super.setContext(ctx);
        for (SliceStoreManager store : _slices) {
            store.setContext(ctx, 
                    (JDBCConfiguration)store.getSlice().getConfiguration());
        }
    }

    private SliceStoreManager lookup(String name) {
        for (SliceStoreManager slice : _slices)
            if (slice.getName().equals(name))
                return slice;
        return null;
    }

    public boolean syncVersion(OpenJPAStateManager sm, Object edata) {
    	String[] targets = findSliceNames(sm, edata).getSlices();
    	boolean sync = true;
    	for (String replica : targets) {
    		SliceStoreManager slice = lookup(replica);
    		sync &= slice.syncVersion(sm, edata);
    	}
    	return sync;
    }

    @Override
    protected RefCountConnection connectInternal() throws SQLException {
        List<Connection> list = new ArrayList<Connection>();
        for (SliceStoreManager slice : _slices)
            list.add(slice.getConnection());
        DistributedConnection con = new DistributedConnection(list);
        return new RefCountConnection(con);
    }
    
    /**
     * Gets the list of slices mentioned as  
     * {@link SlicePersistence#HINT_TARGET hint} of the given
     * {@link FetchConfiguration#getHint(String) fetch configuration}. 
     * 
     * @return all active slices if a) the hint is not specified or b) a null 
     * value or c) a non-String or d) matches no active slice.
     */
    List<SliceStoreManager> getTargets(FetchConfiguration fetch) {
        if (fetch == null)
            return _slices;
        Object hint = fetch.getHint(SlicePersistence.HINT_TARGET);
        if (hint == null || !(hint instanceof String || hint instanceof String[])) 
            return _slices;
        String[] targetNames = hint instanceof String 
                ? new String[]{hint.toString()} : (String[])hint;
        List<SliceStoreManager> targets = new ArrayList<SliceStoreManager>();
        for (SliceStoreManager slice : _slices) {
            for (String name : targetNames) {
                if (slice.getName().equals(name)) {
                    targets.add(slice);
                }
            }
        }
        if (targets.isEmpty())
            return _slices;
        return targets;
    }
    
    private static class Flusher implements Callable<Collection> {
        final SliceStoreManager store;
        final StateManagerSet toFlush;

        Flusher(SliceStoreManager store, StateManagerSet toFlush) {
            this.store = store;
            this.toFlush = toFlush;
        }

        public Collection call() throws Exception {
        	return store.flush(toFlush);
        }
    }
    
    /**
     * A specialized, insert-only collection of StateManagers that notes 
     * if any of its member is replicated.
     *  
     */
    private static class StateManagerSet extends HashSet<OpenJPAStateManager> {
        private final DistributedConfiguration conf;
        List<OpenJPAStateManager> replicated;
        
        StateManagerSet(DistributedConfiguration conf) {
            this.conf = conf;
        }
        @Override
        public boolean add(OpenJPAStateManager sm) {
            boolean isReplicated =  conf.isReplicated(sm.getMetaData().getDescribedType());
            if (isReplicated) {
                if (replicated == null)
                    replicated = new ArrayList<OpenJPAStateManager>();
                replicated.add(sm);
            }
            return super.add(sm);
        }
        
        @Override
        public boolean remove(Object sm) {
            throw new UnsupportedOperationException();
        }
        
        boolean containsReplicated() {
            return replicated != null && !replicated.isEmpty();
        }
        
        List<OpenJPAStateManager> getReplicated() {
            return replicated;
        }
    }
}
