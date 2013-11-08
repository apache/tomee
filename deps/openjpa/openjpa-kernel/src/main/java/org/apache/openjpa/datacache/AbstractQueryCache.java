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
package org.apache.openjpa.datacache;

import java.io.PrintStream;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.event.RemoteCommitEvent;
import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager;
import org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashSet;
import org.apache.openjpa.lib.util.concurrent.SizedConcurrentHashMap;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.Id;

/**
 * Abstract {@link QueryCache} implementation that provides various
 * statistics, logging, and timeout functionality common across cache
 * implementations.
 *
 * @author Patrick Linskey
 * @author Abe White
 */
public abstract class AbstractQueryCache
    extends AbstractConcurrentEventManager 
    implements QueryCache, Configurable {

    private static final Localizer s_loc =
        Localizer.forPackage(AbstractQueryCache.class);

    private static final String TIMESTAMP = "timestamp";
    public enum EvictPolicy {DEFAULT, TIMESTAMP};
    /**
     * The configuration set by the system.
     */
    protected OpenJPAConfiguration conf;

    /**
     * The log to use.
     */
    protected Log log;

    protected ConcurrentHashMap<String,Long> entityTimestampMap = null;
    private boolean _closed = false;
    
    private String _name = null;
    
    // default evict policy
    public EvictPolicy evictPolicy = EvictPolicy.DEFAULT;
    
    private QueryStatistics<QueryKey> _stats;
    private boolean _statsEnabled = false;

    public void setEnableStatistics(boolean enable){
        _statsEnabled = enable;
    }
    public boolean getEnableStatistics(){
        return _statsEnabled;
    }

    public QueryStatistics<QueryKey> getStatistics() {
        return _stats;
    }
    
    public void initialize(DataCacheManager manager) {
        if (evictPolicy == EvictPolicy.TIMESTAMP) {
            entityTimestampMap = new ConcurrentHashMap<String,Long>();
        
            // Get all persistence types to pre-load the entityTimestamp Map
            Collection perTypes =
                conf.getMetaDataRepositoryInstance().getPersistentTypeNames(
                    false,
                    AccessController.doPrivileged(J2DoPrivHelper
                        .getContextClassLoaderAction()));
            
            if(perTypes == null)
                return;
            
            // Pre-load all the entity types into the HashMap to handle 
            // synchronization on the map efficiently
            for (Object o : perTypes)
                entityTimestampMap.put((String)o, Long.valueOf(0));
        }
    }

    public void onTypesChanged(TypesChangedEvent ev) {
        if (evictPolicy == EvictPolicy.DEFAULT) {
            writeLock();
            Collection keys = null;
            try {
                if (hasListeners())
                    fireEvent(ev);
                keys = keySet();
            } finally {
                writeUnlock();
            }
    
            QueryKey qk;
                List<QueryKey> removes = null;
                for (Object o: keys) {
                    qk = (QueryKey) o;
                if (qk.changeInvalidatesQuery(ev.getTypes())) {
                    if (removes == null)
                        removes = new ArrayList<QueryKey>();
                    removes.add(qk);
                }
            }
            if (removes != null)
                removeAllInternal(removes);
        } else {
            Collection changedTypes = ev.getTypes();
            HashMap<String,Long> changedClasses = 
                new HashMap<String,Long>();
            Long tstamp = Long.valueOf(System.currentTimeMillis());
            for (Object o: changedTypes) {
                String name = ((Class) o).getName();
                if(!changedClasses.containsKey(name)) {
                    changedClasses.put(name, tstamp );
                }
            }           
            // Now update entity timestamp map
            updateEntityTimestamp(changedClasses);
        }
    }

    public QueryResult get(QueryKey key) {
        if (_statsEnabled) {
            _stats.recordExecution(key);
        }
        QueryResult o = getInternal(key);
        if (o != null && o.isTimedOut()) {
            o = null;
            removeInternal(key);
            if (log.isTraceEnabled())
                log.trace(s_loc.get("cache-timeout", key));
        }

        if (log.isTraceEnabled()) {
            if (o == null)
                log.trace(s_loc.get("cache-miss", key));
            else
                log.trace(s_loc.get("cache-hit", key));
        }
        if (_statsEnabled && o != null) {
            ((Default<QueryKey>)_stats).recordHit(key);
        }
        return o;
    }

    public QueryResult put(QueryKey qk, QueryResult oids) {
        QueryResult o = putInternal(qk, oids);
        if (log.isTraceEnabled())
            log.trace(s_loc.get("cache-put", qk));
        return (o == null || o.isTimedOut()) ? null : o;
    }

    public QueryResult remove(QueryKey key) {
        QueryResult o = removeInternal(key);
        if (_statsEnabled) {
            _stats.recordEviction(key);
        }
        if (o != null && o.isTimedOut())
            o = null;
        if (log.isTraceEnabled()) {
            if (o == null)
                log.trace(s_loc.get("cache-remove-miss", key));
            else
                log.trace(s_loc.get("cache-remove-hit", key));
        }
        return o;
    }

    public boolean pin(QueryKey key) {
        boolean bool = pinInternal(key);
        if (log.isTraceEnabled()) {
            if (bool)
                log.trace(s_loc.get("cache-pin-hit", key));
            else
                log.trace(s_loc.get("cache-pin-miss", key));
        }
        return bool;
    }

    public boolean unpin(QueryKey key) {
        boolean bool = unpinInternal(key);
        if (log.isTraceEnabled()) {
            if (bool)
                log.trace(s_loc.get("cache-unpin-hit", key));
            else
                log.trace(s_loc.get("cache-unpin-miss", key));
        }
        return bool;
    }

    public void clear() {
        clearInternal();
        if (log.isTraceEnabled())
            log.trace(s_loc.get("cache-clear", "<query-cache>"));
        if (_statsEnabled) {
            _stats.clear();
        }
    }

    public void close() {
        close(true);
    }

    protected void close(boolean clear) {
        if (!_closed) {
            if (clear)
                clearInternal();
            _closed = true;
        }
    }

    public boolean isClosed() {
        return _closed;
    }

    public void addTypesChangedListener(TypesChangedListener listen) {
        addListener(listen);
    }

    public boolean removeTypesChangedListener(TypesChangedListener listen) {
        return removeListener(listen);
    }

    /**
     * This method is part of the {@link RemoteCommitListener} interface. If
     * your cache subclass relies on OpenJPA for clustering support, make it
     * implement <code>RemoteCommitListener</code>. This method will take
     * care of invalidating entries from remote commits, by delegating to
     * {@link #onTypesChanged}.
     */
    public void afterCommit(RemoteCommitEvent event) {
        if (_closed)
            return;

        // drop all committed classes
        Set classes = Caches.addTypesByName(conf,
            event.getPersistedTypeNames(), null);
        if (event.getPayloadType() == RemoteCommitEvent.PAYLOAD_EXTENTS) {
            classes = Caches.addTypesByName(conf, event.getUpdatedTypeNames(),
                classes);
            classes = Caches.addTypesByName(conf, event.getDeletedTypeNames(),
                classes);
        } else {
            classes = addTypes(event.getUpdatedObjectIds(), classes);
            classes = addTypes(event.getDeletedObjectIds(), classes);
        }
        if (classes != null)
            onTypesChanged(new TypesChangedEvent(this, classes));
    }

    /**
     * Build up a set of classes for the given oids.
     */
    private Set addTypes(Collection oids, Set classes) {
        if (oids.isEmpty())
            return classes;
        if (classes == null)
            classes = new HashSet();

        MetaDataRepository repos = conf.getMetaDataRepositoryInstance();
        ClassMetaData meta;
        Object oid;
        for (Iterator itr = oids.iterator(); itr.hasNext();) {
            oid = itr.next();
            if (oid instanceof Id)
                classes.add(((Id) oid).getType());
            else {
                // ok if no metadata for oid; that just means the pc type
                // probably hasn't been loaded into this JVM yet, and therefore
                // there's no chance that it's in the cache anyway
                meta = repos.getMetaData(oid, null, false);
                if (meta != null)
                    classes.add(meta.getDescribedType());
            }
        }
        return classes;
    }

    /**
     * Return a threadsafe view of the keys in this cache. This collection
     * must be iterable without risk of concurrent modification exceptions.
     * It does not have to implement contains() efficiently or use set
     * semantics.
     */
    protected abstract Collection keySet();

    /**
     * Return the list for the given key.
     */
    protected abstract QueryResult getInternal(QueryKey qk);

    /**
     * Add the given result to the cache, returning the old result under the
     * given key.
     */
    protected abstract QueryResult putInternal(QueryKey qk, QueryResult oids);

    /**
     * Remove the result under the given key from the cache.
     */
    protected abstract QueryResult removeInternal(QueryKey qk);

    /**
     * Remove all results under the given keys from the cache.
     */
    protected void removeAllInternal(Collection qks) {
        for (Iterator iter = qks.iterator(); iter.hasNext();)
            removeInternal((QueryKey) iter.next());
    }

    /**
     * Clear the cache.
     */
    protected abstract void clearInternal();

    /**
     * Pin an object to the cache.
     */
    protected abstract boolean pinInternal(QueryKey qk);

    /**
     * Unpin an object from the cache.
     */
    protected abstract boolean unpinInternal(QueryKey qk);

    // ---------- Configurable implementation ----------

    public void setConfiguration(Configuration conf) {
        this.conf = (OpenJPAConfiguration) conf;
        this.log = conf.getLog(OpenJPAConfiguration.LOG_DATACACHE);
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
        _stats = _statsEnabled ? new Default<QueryKey>() :
            new QueryStatistics.None<QueryKey>();
    }

    // ---------- AbstractEventManager implementation ----------

    protected void fireEvent(Object event, Object listener) {
        TypesChangedListener listen = (TypesChangedListener) listener;
        TypesChangedEvent ev = (TypesChangedEvent) event;
        try {
            listen.onTypesChanged(ev);
        } catch (Exception e) {
            if (log.isWarnEnabled())
                log.warn(s_loc.get("exp-listener-ex"), e);
        }
    }

    /**
     * Individual query results will be registered as types changed
     * listeners. We want such query results to be gc'd once
     * the only reference is held by the list of expiration listeners.
     */
    protected Collection newListenerCollection() {
        return new ConcurrentReferenceHashSet (ConcurrentReferenceHashSet.WEAK);
	}

    /**
     * Sets the eviction policy for the query cache
     * @param evictPolicy -- String value that specifies the eviction policy
     */
    public void setEvictPolicy(String evictPolicy) {
        if (evictPolicy.equalsIgnoreCase(TIMESTAMP))
            this.evictPolicy = EvictPolicy.TIMESTAMP;
    }

    /**
     * Returns the evictionPolicy for QueryCache
     * @return -- returns a String value of evictPolicy attribute
     */
    public EvictPolicy getEvictPolicy() {
        return this.evictPolicy;
    }

    /**
     * Updates the entity timestamp map with the current time in milliseconds
     * @param timestampMap -- a map that contains entityname and its last
     * updated timestamp
     */
    protected void updateEntityTimestamp(Map<String,Long> timestampMap) {
        if (entityTimestampMap != null)
            entityTimestampMap.putAll(timestampMap);
     }

    /**
     * Returns a list of timestamps in the form of Long objects
     * which are the last updated time stamps for the given entities in the
     * keylist.
     * @param keyList -- List of entity names 
     * @return -- Returns a list that has the timestamp for the given entities
     */
    public List<Long> getAllEntityTimestamp(List<String> keyList) { 
        ArrayList<Long> tmval = null;
        if (entityTimestampMap != null) {
            for (String s: keyList) {
                if (entityTimestampMap.containsKey(s)) {
                    if(tmval == null)
                        tmval = new ArrayList<Long>();
                    tmval.add(entityTimestampMap.get(s));
                }
            }
        }
        return tmval;
    }
    
    public void setName(String n) {
        _name = n;
    }

    public String getName() {
        return _name;
    }
    
    public int count() {
        return keySet().size();
    }
    
    /**
     * A default implementation of query statistics for the Query result cache.
     * 
     * Maintains statistics for only a fixed number of queries.
     * Statistical counts are approximate and not exact (to keep thread synchorization overhead low).
     * 
     */
    public static class Default<T> implements QueryStatistics<T> {

        private static final long serialVersionUID = -7889619105916307055L;
        
        private static final int FIXED_SIZE = 1000;
        private static final float LOAD_FACTOR = 0.75f;
        private static final int CONCURRENCY = 16;
        
        private static final int ARRAY_SIZE = 3;
        private static final int READ  = 0;
        private static final int HIT   = 1;
        private static final int EVICT = 2;
        
        private long[] astat = new long[ARRAY_SIZE];
        private long[] stat  = new long[ARRAY_SIZE];
        private Map<T, long[]> stats  = new SizedConcurrentHashMap(FIXED_SIZE, LOAD_FACTOR, CONCURRENCY);
        private Map<T, long[]> astats = new SizedConcurrentHashMap(FIXED_SIZE, LOAD_FACTOR, CONCURRENCY);
        private Date start = new Date();
        private Date since = start;
        
        public Set<T> keys() {
            return stats.keySet();
        }

        public long getExecutionCount() {
            return stat[READ];
        }

        public long getTotalExecutionCount() {
            return astat[READ];
        }

        public long getExecutionCount(T query) {
            return getCount(stats, query, READ);
        }

        public long getTotalExecutionCount(T query) {
            return getCount(astats, query, READ);
        }

        public long getHitCount() {
            return stat[HIT];
        }

        public long getTotalHitCount() {
            return astat[HIT];
        }

        public long getHitCount(T query) {
            return getCount(stats, query, HIT);
        }

        public long getTotalHitCount(T query) {
            return getCount(astats, query, HIT);
        }

        public long getEvictionCount() {
            return stat[EVICT];
        }

        public long getTotalEvictionCount() {
            return astat[EVICT];
        }

        private long getCount(Map<T, long[]> target, T query, int i) {
            long[] row = target.get(query);
            return (row == null) ? 0 : row[i];
        }

        public Date since() {
            return since;
        }

        public Date start() {
            return start;
        }

        public synchronized void reset() {
            stat = new long[ARRAY_SIZE];
            stats.clear();
            since = new Date();
        }
        
        @SuppressWarnings("unchecked")
        public synchronized void clear() {
           astat = new long[ARRAY_SIZE];
           stat  = new long[ARRAY_SIZE];
           stats = new SizedConcurrentHashMap(FIXED_SIZE, LOAD_FACTOR, CONCURRENCY);
           astats = new SizedConcurrentHashMap(FIXED_SIZE, LOAD_FACTOR, CONCURRENCY);
           start  = new Date();
           since  = start;
        }
        
        private void addSample(T query, int index) {
            stat[index]++;
            astat[index]++;
            addSample(stats, query, index);
            addSample(astats, query, index);
        }
        
        private void addSample(Map<T, long[]> target, T query, int i) {
            long[] row = target.get(query);
            if (row == null) {
                row = new long[ARRAY_SIZE];
            }
            row[i]++;
            target.put(query, row);
        }
        
        public void recordExecution(T query) {
            if (query == null)
                return;
            addSample(query, READ);
        }
        
        public void recordHit(T query) {
            addSample(query, HIT);
        }
        
        public void recordEviction(T query) {
            if (query == null)
                return;
            addSample(query, EVICT);
        }
        
        public void dump(PrintStream out) {
            String header = "Query Statistics starting from " + start;
            out.print(header);
            if (since == start) {
                out.println();
                out.println("Total Query Execution: " + toString(astat)); 
                out.println("\tTotal \t\tQuery");
            } else {
                out.println(" last reset on " + since);
                out.println("Total Query Execution since start " + 
                        toString(astat)  + " since reset " + toString(stat));
                out.println("\tSince Start \tSince Reset \t\tQuery");
            }
            int i = 0;
            for (T key : stats.keySet()) {
                i++;
                long[] arow = astats.get(key);
                if (since == start) {
                    out.println(i + ". \t" + toString(arow) + " \t" + key);
                } else {
                    long[] row  = stats.get(key);
                    out.println(i + ". \t" + toString(arow) + " \t"  + toString(row) + " \t\t" + key);
                }
            }
        }
        
        long pct(long per, long cent) {
            if (cent <= 0)
                return 0;
            return (100*per)/cent;
        }
        
        String toString(long[] row) {
            return row[READ] + ":" + row[HIT] + "(" + pct(row[HIT], row[READ]) + "%)";
        }
    }

}
