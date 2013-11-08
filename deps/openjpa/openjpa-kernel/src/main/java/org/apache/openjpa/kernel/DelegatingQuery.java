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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

/**
 * Delegating query that can also perform exception translation
 * for use in facades.
 *
 * @since 0.4.0
 * @author Abe White
 * @nojavadoc
 */
public class DelegatingQuery
    implements Query {
    ///////////////////////////////////////////////////////////////
    // NOTE: when adding a public API method, be sure to add it to
    // JDO and JPA facades!
    ///////////////////////////////////////////////////////////////

    private final Query _query;
    private final DelegatingQuery _del;
    private final transient RuntimeExceptionTranslator _trans;

    /**
     * Constructor; supply delegate.
     */
    public DelegatingQuery(Query query) {
        this(query, null);
    }

    /**
     * Constructor; supply delegate and exception translator.
     */
    public DelegatingQuery(Query query, RuntimeExceptionTranslator trans) {
        _query = query;
        if (query instanceof DelegatingQuery)
            _del = (DelegatingQuery) query;
        else
            _del = null;
        _trans = trans;
    }

    /**
     * Return the direct delegate.
     */
    public Query getDelegate() {
        return _query;
    }

    /**
     * Return the native delegate.
     */
    public Query getInnermostDelegate() {
        return (_del == null) ? _query : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingQuery)
            other = ((DelegatingQuery) other).getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    /**
     * Translate the OpenJPA exception.
     */
    protected RuntimeException translate(RuntimeException re) {
        return (_trans == null) ? re : _trans.translate(re);
    }

    public Broker getBroker() {
        try {
            return _query.getBroker();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Query getQuery() {
        return this;
    }

    public StoreContext getStoreContext() {
        try {
            return _query.getStoreContext();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getOperation() {
        try {
            return _query.getOperation();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public String getLanguage() {
        try {
            return _query.getLanguage();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration getFetchConfiguration() {
        try {
            return _query.getFetchConfiguration();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public String getQueryString() {
        try {
            return _query.getQueryString();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean getIgnoreChanges() {
        try {
            return _query.getIgnoreChanges();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object getCompilation() {
        try {
            return _query.getCompilation();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public String getAlias() {
        try {
            return _query.getAlias();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public String[] getProjectionAliases() {
        try {
            return _query.getProjectionAliases();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Class[] getProjectionTypes() {
        try {
            return _query.getProjectionTypes();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean isAggregate() {
        try {
            return _query.isAggregate();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean hasGrouping() {
        try {
            return _query.hasGrouping();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public ClassMetaData[] getAccessPathMetaDatas() {
        try {
            return _query.getAccessPathMetaDatas();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FilterListener getFilterListener(String tag) {
        try {
            return _query.getFilterListener(tag);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public AggregateListener getAggregateListener(String tag) {
        try {
            return _query.getAggregateListener(tag);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Collection getFilterListeners() {
        try {
            return _query.getFilterListeners();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Collection getAggregateListeners() {
        try {
            return _query.getAggregateListeners();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Collection getCandidateCollection() {
        try {
            return _query.getCandidateCollection();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Class getCandidateType() {
        try {
            return _query.getCandidateType();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean hasSubclasses() {
        try {
            return _query.hasSubclasses();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setCandidateType(Class cls, boolean subs) {
        try {
            _query.setCandidateType(cls, subs);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean isReadOnly() {
        try {
            return _query.isReadOnly();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setReadOnly(boolean readOnly) {
        try {
            _query.setReadOnly(readOnly);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Class getResultMappingScope() {
        try {
            return _query.getResultMappingScope();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public String getResultMappingName() {
        try {
            return _query.getResultMappingName();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setResultMapping(Class scope, String name) {
        try {
            _query.setResultMapping(scope, name);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean isUnique() {
        try {
            return _query.isUnique();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setUnique(boolean unique) {
        try {
            _query.setUnique(unique);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public boolean isDistinct() {
        try {
            return _query.isDistinct();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Class getResultType() {
        try {
            return _query.getResultType();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setResultType(Class cls) {
        try {
            _query.setResultType(cls);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public long getStartRange() {
        try {
            return _query.getStartRange();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public long getEndRange() {
        try {
            return _query.getEndRange();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setRange(long start, long end) {
        try {
            _query.setRange(start, end);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public String getParameterDeclaration() {
        try {
            return _query.getParameterDeclaration();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public OrderedMap<Object,Class<?>> getOrderedParameterTypes() {
        try {
            return _query.getOrderedParameterTypes();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public LinkedMap getParameterTypes() {
        try {
            return _query.getParameterTypes();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Map getUpdates() {
        try {
            return _query.getUpdates();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void declareParameters(String params) {
        try {
            _query.declareParameters(params);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Number deleteInMemory(StoreQuery q, StoreQuery.Executor ex, 
        Object[] params) {
        try {
            return _query.deleteInMemory(q, ex, params);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Number updateInMemory(StoreQuery q, StoreQuery.Executor ex, 
        Object[] params) {
        try {
            return _query.updateInMemory(q, ex, params);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Class classForName(String name, String[] imports) {
        try {
            return _query.classForName(name, imports);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void lock() {
        try {
            _query.lock();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void unlock() {
        try {
            _query.unlock();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void addFilterListener(FilterListener listener) {
        try {
            _query.addFilterListener(listener);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void removeFilterListener(FilterListener listener) {
        try {
            _query.removeFilterListener(listener);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void addAggregateListener(AggregateListener listener) {
        try {
            _query.addAggregateListener(listener);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void removeAggregateListener(AggregateListener listener) {
        try {
            _query.removeAggregateListener(listener);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Extent getCandidateExtent() {
        try {
            return _query.getCandidateExtent();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setCandidateExtent(Extent extent) {
        try {
            _query.setCandidateExtent(extent);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setCandidateCollection(Collection coll) {
        try {
            _query.setCandidateCollection(coll);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void compile() {
        try {
            _query.compile();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object execute() {
        try {
            return _query.execute();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object execute(Map params) {
        try {
            return _query.execute(params);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object execute(Object[] params) {
        try {
            return _query.execute(params);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public long deleteAll() {
        try {
            return _query.deleteAll();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public long deleteAll(Object[] parameters) {
        try {
            return _query.deleteAll(parameters);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public long deleteAll(Map parameterMap) {
        try {
            return _query.deleteAll(parameterMap);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public long updateAll() {
        try {
            return _query.updateAll();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public long updateAll(Object[] parameters) {
        try {
            return _query.updateAll(parameters);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public long updateAll(Map parameterMap) {
        try {
            return _query.updateAll(parameterMap);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void closeAll() {
        try {
            _query.closeAll();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void closeResources() {
        try {
            _query.closeResources();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public String[] getDataStoreActions(Map params) {
        try {
            return _query.getDataStoreActions(params);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean setQuery(Object query) {
        try {
            return _query.setQuery(query);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setIgnoreChanges(boolean ignore) {
        try {
            _query.setIgnoreChanges(ignore);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void assertOpen() {
        try {
            _query.assertOpen();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void assertNotReadOnly() {
        try {
            _query.assertNotReadOnly();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void assertNotSerialized() {
        try {
            _query.assertNotSerialized();
        } catch (RuntimeException re) {
            throw translate(re);
		}
	}
}

