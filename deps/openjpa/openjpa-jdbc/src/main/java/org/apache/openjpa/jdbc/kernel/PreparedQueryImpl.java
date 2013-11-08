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

package org.apache.openjpa.jdbc.kernel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.LogicalUnion;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.jdbc.sql.SelectImpl;
import org.apache.openjpa.jdbc.sql.Union;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.PreparedQuery;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.QueryImpl;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.PreparedQueryCache.Exclusion;
import org.apache.openjpa.kernel.exps.Parameter;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.lib.rop.RangeResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultList;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.UserException;

/**
 * Implements {@link PreparedQuery} for SQL queries.
 * PreparedQuery holds the post-compilation and post-execution state of a kernel Query.
 * The post-execution internal state of a query is appended as a <em>user object</em>
 * to the user-visible result to maintain the API contract. 
 * 
 * @author Pinaki Poddar
 *
 */
public class PreparedQueryImpl implements PreparedQuery {
    private static Localizer _loc = 
        Localizer.forPackage(PreparedQueryImpl.class);

    private final String _id;
    private String _sql;
    private boolean _initialized;
    
    // Post-compilation state of an executable query, populated on construction
    private Class<?> _candidate;
    private boolean _subclasses;
    
    // post-execution state of a query
    private QueryExpressions[] _exps;
    private Class<?>[] _projTypes;

    // Position of the user defined parameters in the _params list
    private Map<Object, Integer[]>    _userParamPositions;
    private Map<Integer, Object> _template;
    private SelectImpl select;

    /**
     * Construct.
     * 
     * @param id an identifier for this query to be used as cache key
     * @param compiled a compiled query 
     */
    public PreparedQueryImpl(String id, Query compiled) {
        this(id, null, compiled);
    }
    
    /**
     * Construct.
     * 
     * @param id an identifier for this query to be used as cache key
     * @param corresponding data store language query string 
     * @param compiled a compiled query 
     */
    public PreparedQueryImpl(String id, String sql, Query compiled) {
        this._id = id;
        this._sql = sql;
        if (compiled != null) {
            _candidate    = compiled.getCandidateType();
            _subclasses   = compiled.hasSubclasses();
        }
    }
    
    public String getIdentifier() {
        return _id;
    }
    
    public String getLanguage() {
        return QueryLanguages.LANG_PREPARED_SQL;
    }
    
    /**
     * Get the original query string which is same as the identifier of this 
     * receiver.
     */
    public String getOriginalQuery() {
        return getIdentifier();
    }
    
    public String getTargetQuery() {
        return _sql;
    }
    
    void setTargetQuery(String sql) {
        _sql = sql;
    }
    
    public boolean isInitialized() {
        return _initialized;
    }
    
    public QueryExpressions[] getQueryExpressions() {
        return _exps;
    }
    
    public Class[] getProjectionTypes() {
        return _projTypes;
    }
    
    /**
     * Pours the post-compilation state held by this receiver to the given
     * query.
     */
    public void setInto(Query q) {
    	q.setQuery(_id);
        q.setCandidateType(_candidate, _subclasses);
    }

    /**
     * Initialize this receiver with post-execution result.
     * The input argument is processed only if it is a {@link ResultList} with
     * an attached {@link SelectResultObjectProvider} as its
     * {@link ResultList#getUserObject() user object}. 
     * 
     * @return an exclusion if can not be initialized for some reason. 
     * null if initialization is successful. 
     */
    public Exclusion initialize(Object result) {
        if (isInitialized())
            return null;
        Object[] extract = extractSelectExecutor(result);
        SelectExecutor selector = (SelectExecutor)extract[0];
        if (selector == null)
            return new PreparedQueryCacheImpl.StrongExclusion(_id, ((Localizer.Message)extract[1]).getMessage());
        if (selector == null || selector.hasMultipleSelects()
            || ((selector instanceof Union) 
            && (((Union)selector).getSelects().length != 1)))
            return new PreparedQueryCacheImpl.StrongExclusion(_id, _loc.get("exclude-multi-select", _id).getMessage());
        select = extractImplementation(selector);
        if (select == null)
            return new PreparedQueryCacheImpl.StrongExclusion(_id, _loc.get("exclude-no-select", _id).getMessage());
        SQLBuffer buffer = selector.getSQL();
        if (buffer == null)
            return new PreparedQueryCacheImpl.StrongExclusion(_id, _loc.get("exclude-no-sql", _id).getMessage());;
        if (isUsingFieldStrategy())
            return new PreparedQueryCacheImpl.StrongExclusion(_id, 
                _loc.get("exclude-user-strategy", _id).getMessage());;
                
        if (isPaginated())
            return new PreparedQueryCacheImpl.StrongExclusion(_id, 
                _loc.get("exclude-pagination", _id).getMessage());;

        setTargetQuery(buffer.getSQL());
        setParameters(buffer.getParameters());
        setUserParameterPositions(buffer.getUserParameters());
        _initialized = true;
        
        return null;
    }
    
    /**
     * Extract the underlying SelectExecutor from the given argument, if possible.
     * 
     * @return two objects in an array. The element at index 0 is SelectExecutor, 
     * if it can be extracted. The element at index 1 is the reason why it can
     * not be extracted.
     */
    private Object[] extractSelectExecutor(Object result) {
        if (result instanceof ResultList == false)
            return new Object[]{null, _loc.get("exclude-not-result", _id)};
        Object userObject = ((ResultList<?>)result).getUserObject();
        if (userObject == null || !userObject.getClass().isArray() || ((Object[])userObject).length != 2)
            return new Object[]{null, _loc.get("exclude-no-user-object", _id)};
        Object provider = ((Object[])userObject)[0];
        Object executor = ((Object[])userObject)[1];
        if (executor instanceof StoreQuery.Executor == false)
            return new Object[]{null, _loc.get("exclude-not-executor", _id)};
        _exps = ((StoreQuery.Executor)executor).getQueryExpressions();
        for (int i = 0; i < _exps.length; i++) {
            QueryExpressions exp = _exps[i];
            if (exp.hasInExpression)
                return new Object[]{null, _loc.get("exclude-in-expression", _id)};
            if (isUsingExternalizedParameter(exp)) {
                return new Object[]{null, _loc.get("exclude-externalized-param", _id)};
            }
        }
        if (_exps[0].projections.length == 0) {
            _projTypes = StoreQuery.EMPTY_CLASSES;
        } else {
            _projTypes = new Class[_exps[0].projections.length];
            for (int i = 0; i < _exps[0].projections.length; i++) {
                _projTypes[i] = _exps[0].projections[i].getType();
            }
        }
        if (provider instanceof QueryImpl.PackingResultObjectProvider) {
            provider = ((QueryImpl.PackingResultObjectProvider)provider).getDelegate();
        }
        if (provider instanceof RangeResultObjectProvider) {
            provider = ((RangeResultObjectProvider)provider).getDelegate();
        }
        if (provider instanceof SelectResultObjectProvider) {
            return new Object[]{((SelectResultObjectProvider)provider).getSelect(), null};
        } 
        return new Object[]{null, _loc.get("exclude-not-select-rop", _id, provider.getClass().getName())};
    }
    
    private SelectImpl extractImplementation(SelectExecutor selector) {
        if (selector == null)
            return null;
        if (selector instanceof SelectImpl) 
            return (SelectImpl)selector;
        if (selector instanceof LogicalUnion.UnionSelect)
            return ((LogicalUnion.UnionSelect)selector).getDelegate();
        if (selector instanceof Union) 
            return extractImplementation(((Union)selector).getSelects()[0]);
        
        return null;
    }
    
    private boolean isUsingExternalizedParameter(QueryExpressions exp) {
        if (exp == null)
            return false;
        List<FieldMetaData> fmds = exp.getParameterizedFields();
        if (fmds == null || fmds.isEmpty())
            return false;
        for (FieldMetaData fmd : fmds) {
            if (fmd.isExternalized())
                return true;
        }
        return false;
    }
    
    private boolean isPaginated() {
        if (select instanceof SelectImpl) {
            if (((SelectImpl)select).getStartIndex() != 0 || 
                ((SelectImpl)select).getEndIndex() != Long.MAX_VALUE)
                return true;
        }
        return false;
    }        
    private boolean isUsingFieldStrategy() {
        for (int i = 0; i < _exps.length; i++) {
            if (isUsingFieldStrategy(_exps[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsingFieldStrategy(QueryExpressions exp) {
        if (exp == null)
            return false;
        List<FieldMetaData> fmds = exp.getParameterizedFields();
        if (fmds == null || fmds.isEmpty())
            return false;
        for (FieldMetaData fmd : fmds) {
            if (((FieldMapping)fmd).getMappingInfo().getStrategy() != null)
                return true;
        }
        return false;
    }
    
    /**
     * Merge the given user parameters with its own parameter. The given map
     * must be compatible with the user parameters extracted during 
     * {@link #initialize(Object) initialization}. 
     * 
     * @return 0-based parameter index mapped to corresponding values.
     * 
     */
    public Map<Integer, Object> reparametrize(Map user, Broker broker) {
        if (!isInitialized())
            throw new InternalException("reparameterize() on uninitialized.");
        if (user == null || user.isEmpty()) {
            if (!_userParamPositions.isEmpty()) {
                throw new UserException(_loc.get("uparam-null", 
                    _userParamPositions.keySet(), this));
            } else {
                return _template;
            }
        }
        if (!_userParamPositions.keySet().equals(user.keySet())) {
            throw new UserException(_loc.get("uparam-mismatch", 
                _userParamPositions.keySet(), user.keySet(), this));
        }
        Map<Integer, Object> result = new HashMap<Integer, Object>(_template);
        
        Set<Map.Entry<Object,Object>> userSet = user.entrySet();
        for (Map.Entry<Object,Object> userEntry : userSet) {
            Object key = userEntry.getKey();
            Integer[] indices = _userParamPositions.get(key);
            if (indices == null || indices.length == 0)
                throw new UserException(_loc.get("uparam-no-pos", key, this));
            Object val = userEntry.getValue();
            if (ImplHelper.isManageable(val)) {
                setPersistenceCapableParameter(result, val, indices, broker);
            } else if (val instanceof Collection) {
                setCollectionValuedParameter(result, (Collection)val, indices, 
                    key, broker);
            } else {
                for (int j : indices) {
                    if (val instanceof Enum) {
                        if (_template.get(j) instanceof Integer) {
                            val = ((Enum)val).ordinal();
                        } else {
                            val = ((Enum)val).name();
                        }
                    } 
                    result.put(j, val);
                }
            }
        }
        return result;
    }
    
    /**
     * Calculate primary key identity value(s) of the given manageable instance
     * and fill in the given map.
     * 
     * @param values a map of integer parameter index to parameter value
     * @param pc a manageable instance
     * @param indices the indices of the column values
     * @param broker used to obtain the primary key values
     */
    private void setPersistenceCapableParameter(Map<Integer,Object> result, 
        Object pc, Integer[] indices, Broker broker) {
        JDBCStore store = (JDBCStore)broker.getStoreManager()
            .getInnermostDelegate();
        MappingRepository repos = store.getConfiguration()
            .getMappingRepositoryInstance();
        ClassMapping mapping = repos.getMapping(pc.getClass(), 
            broker.getClassLoader(), true);
        Column[] pks = mapping.getPrimaryKeyColumns();
        Object cols = mapping.toDataStoreValue(pc, pks, store);
        if (cols instanceof Object[]) {
            Object[] array = (Object[])cols;
            int n = array.length;
            if (n > indices.length || indices.length%n != 0)
                throw new UserException(_loc.get("uparam-pc-key", 
                    pc.getClass(), n, Arrays.toString(indices)));
            int k = 0;
            for (int j : indices) {
                result.put(j, array[k%n]);
                k++;
            }
        } else {
            for (int j : indices) {
                result.put(j, cols);
            }
        } 
    }
    
    private void setCollectionValuedParameter(Map<Integer,Object> result, 
        Collection values, Integer[] indices, Object param, Broker broker) {
        int n = values.size();
        Object[] array = values.toArray();
        if (n == 0 || n > indices.length || indices.length%n != 0) {
            throw new UserException(_loc.get("uparam-coll-size", param, values, 
                Arrays.toString(indices)));
        }
        int k = 0;
        for (int j : indices) {
            Object val = array[k%n];
            if (ImplHelper.isManageable(val))
                setPersistenceCapableParameter(result, val, indices, broker);
            else
                result.put(j, val);
            k++;
        }
        
    }
    /**
     * Marks the positions and keys of user parameters.
     * 
     * @param list even elements are numbers representing the position of a 
     * user parameter in the _param list. Odd elements are the user parameter
     * key. A user parameter key may appear more than once.
     */
    void setUserParameterPositions(List list) {
        _userParamPositions = new HashMap<Object, Integer[]>();
        List<Integer> positions = new ArrayList<Integer>();
        for (int i = 1; list != null && i < list.size(); i += 2) {
            Object key = ((Parameter)list.get(i)).getParameterKey();
            positions.clear();
            for (int j = 1; j < list.size(); j += 2) {
                Object other = ((Parameter)list.get(j)).getParameterKey();
                if (key.equals(other))
                    positions.add((Integer)list.get(j-1));
            }
            _userParamPositions.put(key, positions.toArray(new Integer[positions.size()]));
        }
    }
    
    void setParameters(List list) {
        Map<Integer, Object> tmp = new HashMap<Integer, Object>();
        for (int i = 0; list != null && i < list.size(); i++) {
            tmp.put(i, list.get(i));
        }
        _template = Collections.unmodifiableMap(tmp);
    }
    
    SelectImpl getSelect() {
        return select;
    }
    
    public String toString() {
        return "PreparedQuery: [" + getOriginalQuery() + "] --> [" + 
               getTargetQuery() + "]";
    }
}
