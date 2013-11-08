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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockScope;

import org.apache.openjpa.kernel.DataCacheRetrieveMode;
import org.apache.openjpa.kernel.DataCacheStoreMode;
import org.apache.openjpa.kernel.DelegatingFetchConfiguration;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.QueryFlushModes;

/**
 * Implements FetchPlan via delegation to FetchConfiguration.
 *
 * @author Abe White
 * @author Pinaki Poddar
 * @since 0.4.1
 * @nojavadoc
 */
public class FetchPlanImpl
	implements FetchPlan {

    private final DelegatingFetchConfiguration _fetch;
    
    /**
     * Structure holds ranking of equivalent hint keys. Each entry value is a list of other keys that are higher rank
     * than the entry key.   
     */
    protected static Map<String, List<String>> _precedence = new HashMap<String, List<String>>();
    
    /**
     * Structure holds one or more converters for a user-specified hint value. 
     */
    protected static Map<String,HintValueConverter[]> _hints = new HashMap<String,HintValueConverter[]>();
    
    /**
     * Statically registers supported hint keys with their ranking and converters. 
     */
    static {
        registerHint(new String[]{"openjpa.FetchPlan.ExtendedPathLookup"}, 
                new HintValueConverter.StringToBoolean());
        registerHint(new String[]{"openjpa.FetchBatchSize", "openjpa.FetchPlan.FetchBatchSize"}, 
                new HintValueConverter.StringToInteger());
        registerHint(new String[]{"openjpa.MaxFetchDepth", "openjpa.FetchPlan.MaxFetchDepth"}, 
                new HintValueConverter.StringToInteger());
        registerHint(new String[]{"openjpa.LockTimeout", "openjpa.FetchPlan.LockTimeout", 
                "javax.persistence.lock.timeout"}, new HintValueConverter.StringToInteger());
        registerHint(new String[]{"openjpa.QueryTimeout", "openjpa.FetchPlan.QueryTimeout", 
                "javax.persistence.query.timeout"}, new HintValueConverter.StringToInteger());
        registerHint(new String[]{"openjpa.FlushBeforeQueries", "openjpa.FetchPlan.FlushBeforeQueries"}, 
                new HintValueConverter.StringToInteger(
                   new String[] {"0", "1", "2"},
                   new int[]{QueryFlushModes.FLUSH_TRUE, QueryFlushModes.FLUSH_FALSE, 
                           QueryFlushModes.FLUSH_WITH_CONNECTION}));
        registerHint(new String[]{"openjpa.ReadLockMode", "openjpa.FetchPlan.ReadLockMode"},
                new MixedLockLevelsHelper());
        registerHint(new String[]{"openjpa.ReadLockLevel", "openjpa.FetchPlan.ReadLockLevel"},
                new MixedLockLevelsHelper());
        registerHint(new String[]{"openjpa.WriteLockMode", "openjpa.FetchPlan.WriteLockMode"}, 
                new MixedLockLevelsHelper());
        registerHint(new String[]{"openjpa.WriteLockLevel", "openjpa.FetchPlan.WriteLockLevel"}, 
                new MixedLockLevelsHelper());
    }
    
    /**
     * Registers a hint key with its value converters. 
     * 
     * @param keys a set of keys in increasing order of ranking. Can not be null or empty.
     * 
     * @param converters array of converters that are attempts in order to convert a user-specified hint value
     * to a value that is consumable by the kernel.
     */
    protected static void registerHint(String[] keys, HintValueConverter... converters) {
        for (String key : keys) {
            _hints.put(key, converters);
        }
        if (keys.length > 1) {
            for (int i = 0; i < keys.length-1; i++) {
                List<String> list = new ArrayList<String>(keys.length-i-1);
                for (int j = i+1; j < keys.length; j++) {
                    list.add(keys[j]);
                }
                _precedence.put(keys[i], list);
            }
        }
    }
    
    /**
     * Constructor; supply delegate.
     */
    public FetchPlanImpl(FetchConfiguration fetch) {
        _fetch = newDelegatingFetchConfiguration(fetch);
    }

    /**
     * Create a new exception-translating delegating fetch configuration.
     */
    protected DelegatingFetchConfiguration newDelegatingFetchConfiguration(FetchConfiguration fetch) {
        return new DelegatingFetchConfiguration(fetch, PersistenceExceptions.TRANSLATOR);
    }

    /**
     * Delegate.
     */
    public FetchConfiguration getDelegate() {
        return _fetch.getDelegate();
    }

    public int getMaxFetchDepth() {
        return _fetch.getMaxFetchDepth();
    }

    public FetchPlan setMaxFetchDepth(int depth) {
        _fetch.setMaxFetchDepth(depth);
        return this;
    }

    public int getFetchBatchSize() {
        return _fetch.getFetchBatchSize();
    }

    public FetchPlan setFetchBatchSize(int fetchBatchSize) {
        _fetch.setFetchBatchSize(fetchBatchSize);
        return this;
    }

    public boolean getQueryResultCacheEnabled() {
        return _fetch.getQueryCacheEnabled();
    }

    public FetchPlan setQueryResultCacheEnabled(boolean cache) {
        _fetch.setQueryCacheEnabled(cache);
        return this;
    }

    public boolean getQueryResultCache() {
        return getQueryResultCacheEnabled();
    }

    public FetchPlan setQueryResultCache(boolean cache) {
        return setQueryResultCacheEnabled(cache);
    }

    public Collection<String> getFetchGroups() {
        return _fetch.getFetchGroups();
    }

    public FetchPlan addFetchGroup(String group) {
        _fetch.addFetchGroup(group);
        return this;
    }

    public FetchPlan addFetchGroups(String... groups) {
        return addFetchGroups(Arrays.asList(groups));
    }

    public FetchPlan addFetchGroups(Collection groups) {
        _fetch.addFetchGroups(groups);
        return this;
    }

    public FetchPlan removeFetchGroup(String group) {
        _fetch.removeFetchGroup(group);
        return this;
    }

    public FetchPlan removeFetchGroups(String... groups) {
        return removeFetchGroups(Arrays.asList(groups));
    }

    public FetchPlan removeFetchGroups(Collection groups) {
        _fetch.removeFetchGroups(groups);
        return this;
    }

    public FetchPlan clearFetchGroups() {
        _fetch.clearFetchGroups();
        return this;
    }

    public FetchPlan resetFetchGroups() {
        _fetch.resetFetchGroups();
        return this;
    }

    public Collection<String> getFields() {
        return (Collection<String>) _fetch.getFields();
    }

    public boolean hasField(String field) {
        return _fetch.hasField(field);
    }

    public boolean hasField(Class cls, String field) {
        return hasField(toFieldName(cls, field));
    }

    public FetchPlan addField(String field) {
        _fetch.addField(field);
        return this;
    }

    public FetchPlan addField(Class cls, String field) {
        return addField(toFieldName(cls, field));
    }

    public FetchPlan addFields(String... fields) {
        return addFields(Arrays.asList(fields));
    }

    public FetchPlan addFields(Class cls, String... fields) {
        return addFields(cls, Arrays.asList(fields));
    }

    public FetchPlan addFields(Collection fields) {
        _fetch.addFields(fields);
        return this;
    }

    public FetchPlan addFields(Class cls, Collection fields) {
        return addFields(toFieldNames(cls, fields));
    }

    public FetchPlan removeField(String field) {
        _fetch.removeField(field);
        return this;
    }

    public FetchPlan removeField(Class cls, String field) {
        return removeField(toFieldName(cls, field));
    }

    public FetchPlan removeFields(String... fields) {
        return removeFields(Arrays.asList(fields));
    }

    public FetchPlan removeFields(Class cls, String... fields) {
        return removeFields(cls, Arrays.asList(fields));
    }

    public FetchPlan removeFields(Collection fields) {
        _fetch.removeFields(fields);
        return this;
    }

    public FetchPlan removeFields(Class cls, Collection fields) {
        return removeFields(toFieldNames(cls, fields));
    }

    public FetchPlan clearFields() {
        _fetch.clearFields();
        return this;
    }

    private static String toFieldName(Class cls, String field) {
        return cls.getName() + "." + field;
    }

    private static Collection toFieldNames(Class cls, Collection fields) {
        if (fields.isEmpty())
            return fields;
        Collection names = new ArrayList(fields);
        for (Iterator itr = fields.iterator(); itr.hasNext();)
            names.add(toFieldName(cls, (String) itr.next()));
        return names;
    }

    public int getLockTimeout() {
        return _fetch.getLockTimeout();
    }

    public FetchPlan setLockTimeout(int timeout) {
        _fetch.setLockTimeout(timeout);
        return this;
    }

    public PessimisticLockScope getLockScope() {
        return LockScopesHelper.fromLockScope(_fetch.getLockScope());
    }

    public FetchPlan setLockScope(PessimisticLockScope scope) {
        _fetch.setLockScope(LockScopesHelper.toLockScope(scope));
        return this;
    }

    public int getQueryTimeout() {
        return _fetch.getQueryTimeout();
    }

    public FetchPlan setQueryTimeout(int timeout) {
        _fetch.setQueryTimeout(timeout);
        return this;
    }

    public LockModeType getReadLockMode() {
        return MixedLockLevelsHelper.fromLockLevel(_fetch.getReadLockLevel());
    }

    public FetchPlan setReadLockMode(LockModeType mode) {
        _fetch.setReadLockLevel(MixedLockLevelsHelper.toLockLevel(mode));
        return this;
    }

    public LockModeType getWriteLockMode() {
        return MixedLockLevelsHelper.fromLockLevel(_fetch.getWriteLockLevel());
    }

    public FetchPlan setWriteLockMode(LockModeType mode) {
        _fetch.setWriteLockLevel(MixedLockLevelsHelper.toLockLevel(mode));
        return this;
    }
    
    public boolean getExtendedPathLookup() {
        return _fetch.getExtendedPathLookup();
    }
    
    public FetchPlan setExtendedPathLookup(boolean flag) {
        _fetch.setExtendedPathLookup(flag);
        return this;
    }

    public Object getHint(String key) {
        return _fetch.getHint(key);
    }
    
    /**
     * Sets the hint after converting the value appropriately.
     * If a higher ranking equivalent hint is already set, then bypasses this hint. 
     */
    public void setHint(String key, Object value) {
        if (!isRecognizedHint(key))
            return;
        if (_precedence.containsKey(key)) {
            List<String> higherKeys = _precedence.get(key);
            for (String higherKey : higherKeys) {
                if (_fetch.isHintSet(higherKey))
                    return;
            }
        }
        Object newValue = convertHintValue(key, value); 
        _fetch.setHint(key, newValue, value);
    }

    public void setHints(Map<String, Object> hints) {
        if (hints == null || hints.isEmpty()) {
            return;
        }
        for (Map.Entry<String,Object> hint : hints.entrySet()) {
            setHint(hint.getKey(), hint.getValue());
        }
    }
    
    public Map<String, Object> getHints() {
        return _fetch.getHints();
    }
    
    public int hashCode() {
        return ((_fetch == null) ? 0  : _fetch.hashCode());
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if ((other == null) || (other.getClass() != this.getClass()))
            return false;
        if (_fetch == null)
        	return false;
        
        return _fetch.equals(((FetchPlanImpl) other)._fetch);
    }

    public DataCacheRetrieveMode getCacheRetrieveMode() {
        return _fetch.getCacheRetrieveMode();
    }

    public DataCacheStoreMode getCacheStoreMode() {
        return _fetch.getCacheStoreMode();
    }

    public FetchPlan setCacheStoreMode(DataCacheStoreMode mode) {
        _fetch.setCacheStoreMode(mode);
        return this;
    }

    public FetchPlan setCacheRetrieveMode(DataCacheRetrieveMode mode) {
        _fetch.setCacheRetrieveMode(mode);
        return this;
    }

    Object convertHintValue(String key, Object value) {
        if (value == null)
            return null;
        HintValueConverter[] converters = _hints.get(key);
        if (converters == null)
            return value;
        for (HintValueConverter converter : converters) {
            if (converter.canConvert(value.getClass())) {
                return converter.convert(value);
            }
        }
        return value;
    }
    
    boolean isRecognizedHint(String key) {
        if (key == null)
            return false;
        if (_hints.containsKey(key))
            return true;
        return key.startsWith("openjpa.");
    }
    
    boolean intersects(Collection<String> keys, Collection<String> b) {
        if (keys == null || keys.isEmpty() || b == null || b.isEmpty())
            return false;
        for (String key : keys) {
            if (b.contains(key))
                return true;
        }
        return false;
    }
}
