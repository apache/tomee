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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.lib.rop.ResultList;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

///////////////////////////////////////////////////////////////
// NOTE: when adding a public API method, be sure to add it to 
// JDO and JPA facades!
///////////////////////////////////////////////////////////////

/**
 * Delegating fetch configuration that can also perform exception
 * translation for use in facades.
 *
 * @author Abe White
 * @nojavadoc
 */
public class DelegatingFetchConfiguration
    implements FetchConfiguration {

    private final FetchConfiguration _fetch;
    private final DelegatingFetchConfiguration _del;
    private final RuntimeExceptionTranslator _trans;

    /**
     * Constructor; supply delegate.
     */
    public DelegatingFetchConfiguration(FetchConfiguration fetch) {
        this(fetch, null);
    }

    /**
     * Constructor; supply delegate and exception translator.
     */
    public DelegatingFetchConfiguration(FetchConfiguration fetch,
        RuntimeExceptionTranslator trans) {
        _fetch = fetch;
        if (fetch instanceof DelegatingFetchConfiguration)
            _del = (DelegatingFetchConfiguration) fetch;
        else
            _del = null;
        _trans = trans;
    }

    /**
     * Return the direct delegate.
     */
    public FetchConfiguration getDelegate() {
        return _fetch;
    }

    /**
     * Return the native delegate.
     */
    public FetchConfiguration getInnermostDelegate() {
        return (_del == null) ? _fetch : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingFetchConfiguration)
            other = ((DelegatingFetchConfiguration) other).
                getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    /**
     * Translate the OpenJPA exception.
     */
    protected RuntimeException translate(RuntimeException re) {
        return (_trans == null) ? re : _trans.translate(re);
    }

    public StoreContext getContext() {
        try {
            return _fetch.getContext();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setContext(StoreContext ctx) {
        try {
            _fetch.setContext(ctx);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getFetchBatchSize() {
        try {
            return _fetch.getFetchBatchSize();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setFetchBatchSize(int fetchBatchSize) {
        try {
            _fetch.setFetchBatchSize(fetchBatchSize);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getMaxFetchDepth() {
        try {
            return _fetch.getMaxFetchDepth();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setMaxFetchDepth(int depth) {
        try {
            _fetch.setMaxFetchDepth(depth);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Set getRootInstances() {
        try {
            return _fetch.getRootInstances();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setRootInstances(Collection roots) {
        try {
            _fetch.setRootInstances(roots);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Set getRootClasses() {
        try {
            return _fetch.getRootClasses();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setRootClasses(Collection roots) {
        try {
            _fetch.setRootClasses(roots);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean getQueryCacheEnabled() {
        try {
            return _fetch.getQueryCacheEnabled();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setQueryCacheEnabled(boolean cache) {
        try {
            _fetch.setQueryCacheEnabled(cache);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getFlushBeforeQueries() {
        try {
            return _fetch.getFlushBeforeQueries();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setFlushBeforeQueries(int flush) {
        try {
            _fetch.setFlushBeforeQueries(flush);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public boolean getExtendedPathLookup() {
        try {
            return _fetch.getExtendedPathLookup();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setExtendedPathLookup(boolean flag) {
        try {
            _fetch.setExtendedPathLookup(flag);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    

    public Set getFetchGroups() {
        try {
            return _fetch.getFetchGroups();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean hasFetchGroup(String group) {
        try {
            return _fetch.hasFetchGroup(group);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration addFetchGroup(String group) {
        try {
            _fetch.addFetchGroup(group);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration addFetchGroups(Collection groups) {
        try {
            _fetch.addFetchGroups(groups);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration removeFetchGroup(String group) {
        try {
            _fetch.removeFetchGroup(group);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration removeFetchGroups(Collection groups) {
        try {
            _fetch.removeFetchGroups(groups);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration clearFetchGroups() {
        try {
            _fetch.clearFetchGroups();
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration resetFetchGroups() {
        try {
            _fetch.resetFetchGroups();
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Set getFields() {
        try {
            return _fetch.getFields();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean hasField(String field) {
        try {
            return _fetch.hasField(field);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration addField(String field) {
        try {
            _fetch.addField(field);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration addFields(Collection fields) {
        try {
            _fetch.addFields(fields);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration removeField(String field) {
        try {
            _fetch.removeField(field);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration removeFields(Collection fields) {
        try {
            _fetch.removeFields(fields);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration clearFields() {
        try {
            _fetch.clearFields();
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public int getLockTimeout() {
        try {
            return _fetch.getLockTimeout();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setLockTimeout(int timeout) {
        try {
            _fetch.setLockTimeout(timeout);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getQueryTimeout() {
        try {
            return _fetch.getQueryTimeout();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setQueryTimeout(int timeout) {
        try {
            _fetch.setQueryTimeout(timeout);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getLockScope() {
        try {
            return _fetch.getLockScope();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setLockScope(int scope) {
        try {
            _fetch.setLockScope(scope);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getReadLockLevel() {
        try {
            return _fetch.getReadLockLevel();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setReadLockLevel(int level) {
        try {
            _fetch.setReadLockLevel(level);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getWriteLockLevel() {
        try {
            return _fetch.getWriteLockLevel();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration setWriteLockLevel(int level) {
        try {
            _fetch.setWriteLockLevel(level);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public ResultList newResultList(ResultObjectProvider rop) {
        try {
            return _fetch.newResultList(rop);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void copy(FetchConfiguration fetch) {
        try {
            _fetch.copy(fetch);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object clone() {
        try {
            return _fetch.clone();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setHint(String name, Object value) {
        try {
            _fetch.setHint(name, value);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public void setHint(String name, Object value, Object original) {
        try {
            _fetch.setHint(name, value, original);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean isHintSet(String key) {
        try {
            return _fetch.isHintSet(key);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public boolean isDefaultPUFetchGroupConfigurationOnly() {
        try {
            return _fetch.isDefaultPUFetchGroupConfigurationOnly();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public Object getHint(String name) {
        try {
            return _fetch.getHint(name);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Map<String, Object> getHints() {
        try {
            return _fetch.getHints();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public int requiresFetch(FieldMetaData fmd) {
        try {
            return _fetch.requiresFetch(fmd);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean requiresLoad() {
        try {
            return _fetch.requiresLoad();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration traverse(FieldMetaData fmd) {
        try {
            return _fetch.traverse(fmd);
        } catch (RuntimeException re) {
            throw translate(re);
        } 
    }

    public void lock() {
        try {
            _fetch.lock();
        } catch (RuntimeException re) {
            throw translate(re);
		}
	}

    public void unlock() {
        try {
            _fetch.unlock();
        } catch (RuntimeException re) {
            throw translate(re);
		}
	}

    public DataCacheRetrieveMode getCacheRetrieveMode() {
        try {
            return _fetch.getCacheRetrieveMode();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public DataCacheStoreMode getCacheStoreMode() {
        try {
            return _fetch.getCacheStoreMode();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setCacheRetrieveMode(DataCacheRetrieveMode mode) {
        try {
            _fetch.setCacheRetrieveMode(mode);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setCacheStoreMode(DataCacheStoreMode mode) {
        try {
            _fetch.setCacheStoreMode(mode);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public boolean isFetchConfigurationSQLCacheAdmissible() {
       try {
           return _fetch.isFetchConfigurationSQLCacheAdmissible();
       } catch (RuntimeException re) {
            throw translate(re);
       }
    }
}
