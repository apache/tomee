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

import static org.apache.openjpa.kernel.QueryLanguages.LANG_PREPARED_SQL;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.DelegatingQuery;
import org.apache.openjpa.kernel.DelegatingResultList;
import org.apache.openjpa.kernel.DistinctResultList;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.PreparedQuery;
import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.QueryOperations;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.rop.ResultList;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.persistence.criteria.CriteriaBuilderImpl;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.RuntimeExceptionTranslator;
import org.apache.openjpa.util.UserException;


/**
 * Implementation of {@link Query} interface.
 * 
 * @author Marc Prud'hommeaux
 * @author Abe White
 * @nojavadoc
 */
@SuppressWarnings("serial")
public class QueryImpl<X> extends AbstractQuery<X> implements Serializable {

    private static final Localizer _loc = Localizer.forPackage(QueryImpl.class);
	private transient FetchPlan _fetch;

	private String _id;
    private transient ReentrantLock _lock = null;
	private HintHandler _hintHandler;
    private DelegatingQuery _query;
	/**
	 * Constructor; supply factory exception translator and delegate.
	 * 
	 * @param em  The EntityManager which created this query
	 * @param ret Exception translator for this query
	 * @param query The underlying "kernel" query.
	 */
    public QueryImpl(EntityManagerImpl em, RuntimeExceptionTranslator ret, org.apache.openjpa.kernel.Query query,
        QueryMetaData qmd) {
        super(qmd, em);
        _query = new DelegatingQuery(query, ret);
        _lock = new ReentrantLock();
        if(query.getLanguage() == QueryLanguages.LANG_SQL) { 
            _convertPositionalParams = false; 
        }
        else { 
            Compatibility compat  = query.getStoreContext().getConfiguration().getCompatibilityInstance(); 
            _convertPositionalParams = compat.getConvertPositionalParametersToNamed();    
        }
        
    }

	/**
	 * Constructor; supply factory and delegate.
	 * 
	 * @deprecated
	 */
    public QueryImpl(EntityManagerImpl em, RuntimeExceptionTranslator ret, org.apache.openjpa.kernel.Query query) {
        this(em, ret, query, null);
    }
	
    /**
     * Constructor; supply factory and delegate.
     * 
     * @deprecated
     */
    public QueryImpl(EntityManagerImpl em, org.apache.openjpa.kernel.Query query) {
        this(em, null, query, null);
    }	

	/**
	 * Delegate.
	 */
	public org.apache.openjpa.kernel.Query getDelegate() {
		return _query.getDelegate();
	}

	public OpenJPAEntityManager getEntityManager() {
		return _em;
	}

	public String getLanguage() {
		return _query.getLanguage();
	}

	public QueryOperationType getOperation() {
        return QueryOperationType.fromKernelConstant(_query.getOperation());
	}

	public FetchPlan getFetchPlan() {
		_em.assertNotCloseInvoked();
		_query.assertNotSerialized();
		_query.lock();
		try {
			if (_fetch == null)
                _fetch = ((EntityManagerFactoryImpl) _em
                        .getEntityManagerFactory()).toFetchPlan(_query
                        .getBroker(), _query.getFetchConfiguration());
			return _fetch;
		} finally {
			_query.unlock();
		}
	}

	public String getQueryString() {
		String result = _query.getQueryString();
		return result != null ? result : _id;
	}

	public boolean getIgnoreChanges() {
		return _query.getIgnoreChanges();
	}

	public OpenJPAQuery<X> setIgnoreChanges(boolean ignore) {
		_em.assertNotCloseInvoked();
		_query.setIgnoreChanges(ignore);
		return this;
	}

	public OpenJPAQuery<X> addFilterListener(FilterListener listener) {
		_em.assertNotCloseInvoked();
		_query.addFilterListener(listener);
		return this;
	}

	public OpenJPAQuery<X> removeFilterListener(FilterListener listener) {
		_em.assertNotCloseInvoked();
		_query.removeFilterListener(listener);
		return this;
	}

	public OpenJPAQuery<X> addAggregateListener(AggregateListener listener) {
		_em.assertNotCloseInvoked();
		_query.addAggregateListener(listener);
		return this;
	}

    public OpenJPAQuery<X> removeAggregateListener(AggregateListener listener) {
		_em.assertNotCloseInvoked();
		_query.removeAggregateListener(listener);
		return this;
	}

	public Collection<?> getCandidateCollection() {
		return _query.getCandidateCollection();
	}

	public OpenJPAQuery<X> setCandidateCollection(Collection coll) {
		_em.assertNotCloseInvoked();
		_query.setCandidateCollection(coll);
		return this;
	}

	public Class getResultClass() {
		Class res = _query.getResultType();
		if (res != null)
			return res;
		return _query.getCandidateType();
	}

	public OpenJPAQuery<X> setResultClass(Class cls) {
		_em.assertNotCloseInvoked();
		if (ImplHelper.isManagedType(_em.getConfiguration(), cls))
			_query.setCandidateType(cls, true);
		else
			_query.setResultType(cls);
		return this;
	}

	public boolean hasSubclasses() {
		return _query.hasSubclasses();
	}

	public OpenJPAQuery<X> setSubclasses(boolean subs) {
		_em.assertNotCloseInvoked();
		Class<?> cls = _query.getCandidateType();
        _query.setCandidateExtent(_query.getBroker().newExtent(cls, subs));
		return this;
	}

	public int getFirstResult() {
		return asInt(_query.getStartRange());
	}

	public OpenJPAQuery<X> setFirstResult(int startPosition) {
		_em.assertNotCloseInvoked();
		long end;
		if (_query.getEndRange() == Long.MAX_VALUE)
			end = Long.MAX_VALUE;
		else
			end = startPosition
                    + (_query.getEndRange() - _query.getStartRange());
		_query.setRange(startPosition, end);
		return this;
	}

	public int getMaxResults() {
		return asInt(_query.getEndRange() - _query.getStartRange());
	}

	public OpenJPAQuery<X> setMaxResults(int max) {
		_em.assertNotCloseInvoked();
		long start = _query.getStartRange();
		if (max == Integer.MAX_VALUE)
			_query.setRange(start, Long.MAX_VALUE);
		else
			_query.setRange(start, start + max);
		return this;
	}
	
	public OpenJPAQuery<X> compile() {
		_em.assertNotCloseInvoked();
		_query.compile();
		return this;
	}
	
	private Object execute() {
        if (!isNative() && _query.getOperation() != QueryOperations.OP_SELECT)
            throw new InvalidStateException(_loc.get("not-select-query", getQueryString()), null, null, false);
		try {
		    lock();
            Map params = getParameterValues();
            boolean registered = preExecute(params);
            Object result = _query.execute(params);
            if (registered) {
                postExecute(result);
            }
            return result;
		} catch (LockTimeoutException e) {
		    throw new QueryTimeoutException(e.getMessage(), new Throwable[]{e}, this);
		} finally {
		    unlock();
		}
	}
	
	public List getResultList() {
		_em.assertNotCloseInvoked();
		boolean queryFetchPlanUsed = pushQueryFetchPlan();
		try {
		    Object ob = execute();
		    if (ob instanceof List) {
			    List ret = (List) ob;
			    if (ret instanceof ResultList) {
			        RuntimeExceptionTranslator trans = PersistenceExceptions.getRollbackTranslator(_em);
			        if (_query.isDistinct()) {
			            return new DistinctResultList((ResultList) ret, trans);
			        } else {
			            return new DelegatingResultList((ResultList) ret, trans);
			        }
			    } else {
				    return ret;
			    }
		    }
		    return Collections.singletonList(ob);
		} finally {
			popQueryFetchPlan(queryFetchPlanUsed);
		}
	}

	/**
	 * Execute a query that returns a single result.
	 */
	public X getSingleResult() {
		_em.assertNotCloseInvoked();
        setHint(QueryHints.HINT_RESULT_COUNT, 1); // for DB2 optimization
		boolean queryFetchPlanUsed = pushQueryFetchPlan();
		try {
		    List result = getResultList();
		    if (result == null || result.isEmpty())
                throw new NoResultException(_loc.get("no-result", getQueryString())
                        .getMessage());
		    if (result.size() > 1)
                throw new NonUniqueResultException(_loc.get("non-unique-result",
                        getQueryString(), result.size()).getMessage());
		    try {
		        return (X)result.get(0);
		    } catch (Exception e) {
                throw new NoResultException(_loc.get("no-result", getQueryString())
                    .getMessage());
		    }
		} finally {
			popQueryFetchPlan(queryFetchPlanUsed);
		}
	}

	private boolean pushQueryFetchPlan() {
		boolean fcPushed = false;
		if (_hintHandler != null) {
			FetchConfiguration fc = _fetch == null ? null : ((FetchPlanImpl)_fetch).getDelegate();
			_em.pushFetchPlan(fc);
			return true;
		}
		if (_fetch != null && _hintHandler != null) {
			switch (_fetch.getReadLockMode()) {
			case PESSIMISTIC_READ:
			case PESSIMISTIC_WRITE:
			case PESSIMISTIC_FORCE_INCREMENT:
				// push query fetch plan to em if pessisimistic lock and any
				// hints are set
				_em.pushFetchPlan(((FetchPlanImpl)_fetch).getDelegate());
				fcPushed = true;
			}
		}
		return fcPushed;
	}

	private void popQueryFetchPlan(boolean queryFetchPlanUsed) {
		if (queryFetchPlanUsed) {
			_em.popFetchPlan();
		}
	}

	public int executeUpdate() {
		_em.assertNotCloseInvoked();
        Map<?,?> paramValues = getParameterValues();
		if (_query.getOperation() == QueryOperations.OP_DELETE) {
		   return asInt(paramValues.isEmpty() ? _query.deleteAll() : _query.deleteAll(paramValues));
		}
		if (_query.getOperation() == QueryOperations.OP_UPDATE) {
	       return asInt(paramValues.isEmpty() ? _query.updateAll() : _query.updateAll(paramValues));
		}
        throw new InvalidStateException(_loc.get("not-update-delete-query", getQueryString()), null, null, false);
	}

	/**
	 * Cast the specified long down to an int, first checking for overflow.
	 */
	private static int asInt(long l) {
		if (l > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
        if (l < Integer.MIN_VALUE) // unlikely, but we might as well check
			return Integer.MIN_VALUE;
		return (int) l;
	}

	public FlushModeType getFlushMode() {
		return EntityManagerImpl.fromFlushBeforeQueries(_query
                .getFetchConfiguration().getFlushBeforeQueries());
	}

	public OpenJPAQuery<X> setFlushMode(FlushModeType flushMode) {
		_em.assertNotCloseInvoked();
		_query.getFetchConfiguration().setFlushBeforeQueries(
                EntityManagerImpl.toFlushBeforeQueries(flushMode));
		return this;
	}

	/**
	 * Asserts that this query is a JPQL or Criteria Query.
	 */
	void assertJPQLOrCriteriaQuery() {
        String language = getLanguage();
        if (JPQLParser.LANG_JPQL.equals(language) 
         || QueryLanguages.LANG_PREPARED_SQL.equals(language)
         || CriteriaBuilderImpl.LANG_CRITERIA.equals(language)) {
            return;
        } else {
            throw new IllegalStateException(_loc.get("not-jpql-or-criteria-query").getMessage());
        }
	}

	public OpenJPAQuery<X> closeAll() {
		_query.closeAll();
		return this;
	}

	public String[] getDataStoreActions(Map params) {
		return _query.getDataStoreActions(params);
	}

    public LockModeType getLockMode() {
        assertJPQLOrCriteriaQuery();
        return getFetchPlan().getReadLockMode();
    }

    /**
     * Sets lock mode on the given query.
     * If the target query has been prepared and cached, then ignores the cached version.
     * @see #ignorePreparedQuery()
     */
    public TypedQuery<X> setLockMode(LockModeType lockMode) {
        String language = getLanguage();
        if (QueryLanguages.LANG_PREPARED_SQL.equals(language)) {
            ignorePreparedQuery();
        }
        assertJPQLOrCriteriaQuery();
       getFetchPlan().setReadLockMode(lockMode);
       return this;
    }

	public int hashCode() {
        return (_query == null) ? 0 : _query.hashCode();
	}

	public boolean equals(Object other) {
		if (other == this)
			return true;
        if ((other == null) || (other.getClass() != this.getClass()))
            return false;
        if (_query == null)
            return false;
		return _query.equals(((QueryImpl) other)._query);
	}

	/**
	 * Get all the active hints and their values.
	 * 
	 */
    //TODO: JPA 2.0 Hints that are not set to FetchConfiguration 
    public Map<String, Object> getHints() {
        if (_hintHandler == null)
            return Collections.emptyMap();
        return _hintHandler.getHints();
    }

    public OpenJPAQuery<X> setHint(String key, Object value) {
        _em.assertNotCloseInvoked();
        if (_hintHandler == null) {
            _hintHandler = new HintHandler(this);
        }
        _hintHandler.setHint(key, value);
        return this;
    }

    public Set<String> getSupportedHints() {
        if (_hintHandler == null) {
            _hintHandler = new HintHandler(this);
        }
        return _hintHandler.getSupportedHints();
    }

    /**
     * Unwraps this receiver to an instance of the given class, if possible.
     * 
     * @exception if the given class is null, generic <code>Object.class</code> or a class
     * that is not wrapped by this receiver.  
     * 
     * @since 2.0.0
     */
    public <T> T unwrap(Class<T> cls) {
        Object[] delegates = new Object[]{_query.getInnermostDelegate(), _query.getDelegate(), _query, this};
        for (Object o : delegates) {
            if (cls != null && cls != Object.class && cls.isInstance(o))
                return (T)o;
        }
        // Set this transaction to rollback only (as per spec) here because the raised exception 
        // does not go through normal exception translation pathways
        RuntimeException ex = new PersistenceException(_loc.get("unwrap-query-invalid", cls).toString(), null, 
                this, false);
        if (_em.isActive())
            _em.setRollbackOnly(ex);
        throw ex;
    }

    
    // =======================================================================
    // Prepared Query Cache related methods
    // =======================================================================
    
    /**
     * Invoked before a query is executed.
     * If this receiver is cached as a {@linkplain PreparedQuery prepared query}
     * then re-parameterizes the given user parameters. The given map is cleared
     * and re-parameterized values are filled in. 
     * 
     * @param params user supplied parameter key-values. Always supply a 
     * non-null map even if the user has not specified any parameter, because 
     * the same map will to be populated by re-parameterization.
     * 
     * @return true if this invocation caused the query being registered in the
     * cache. 
     */
    private boolean preExecute(Map params) {
    	
        PreparedQueryCache cache = _em.getPreparedQueryCache();
        if (cache == null) {
            return false;
        }
        FetchConfiguration fetch = _query.getFetchConfiguration();
        if (fetch.getReadLockLevel() != 0) {
            if (cache.get(_id) != null) {
                ignorePreparedQuery();
            }
            return false;
        }
        
        // Determine if the query has NULL parameters.  If so, then do not use a PreparedQuery from the cache
        for (Object val : params.values()) {
            if (val == null) {
                ignorePreparedQuery();
                return false;
            }
        }
        
        Boolean registered = cache.register(_id, _query, fetch);
        boolean alreadyCached = (registered == null);
        String lang = _query.getLanguage();
        QueryStatistics<String> stats = cache.getStatistics();
        if (alreadyCached && LANG_PREPARED_SQL.equals(lang)) {
            PreparedQuery pq = _em.getPreparedQuery(_id);
            if (pq.isInitialized()) {
                try {
                    Map rep = pq.reparametrize(params, _em.getBroker());
                    params.clear();
                    params.putAll(rep);
                } catch (UserException ue) {
                    invalidatePreparedQuery();
                    Log log = _em.getConfiguration().getLog(OpenJPAConfiguration.LOG_RUNTIME);
                    if (log.isWarnEnabled())
                        log.warn(ue.getMessage());
                    return false;
                }
            }
            stats.recordExecution(pq.getOriginalQuery());
        } else {
            stats.recordExecution(getQueryString());
        }
        return registered == Boolean.TRUE;
    }
    
    /**
     * Initialize the registered Prepared Query from the given opaque object.
     * 
     * @param result an opaque object representing execution result of a query
     * 
     * @return true if the prepared query can be initialized.
     */
    private boolean postExecute(Object result) {
        PreparedQueryCache cache = _em.getPreparedQueryCache();
        if (cache == null) {
            return false;
        }
        return cache.initialize(_id, result) != null;
    }
    
    /**
     * Remove this query from PreparedQueryCache. 
     */
    boolean invalidatePreparedQuery() {
        PreparedQueryCache cache = _em.getPreparedQueryCache();
        if (cache == null)
            return false;
        ignorePreparedQuery();
        return cache.invalidate(_id);
    }
    
    /**
     * Ignores this query from PreparedQueryCache by recreating the original
     * query if it has been cached. 
     */
    void ignorePreparedQuery() {
        PreparedQuery cached = _em.getPreparedQuery(_id);
        if (cached == null)
            return;
        Broker broker = _em.getBroker();
        // Critical assumption: Only JPQL queries are cached and more 
        // importantly, the identifier of the prepared query is the original
        // JPQL String
        String JPQL = JPQLParser.LANG_JPQL;
        String jpql = _id;
        
        org.apache.openjpa.kernel.Query newQuery = broker.newQuery(JPQL, jpql);
        newQuery.getFetchConfiguration().copy(_query.getFetchConfiguration());
        newQuery.compile();
        _query = new DelegatingQuery(newQuery, _em.getExceptionTranslator());
    }
    
    // package protected
    QueryImpl setId(String id) {
        _id = id;
        return this;
    }
    // ================ End of Prepared Query related methods =====================
    
    protected void lock() {
        if (_lock != null) 
            _lock.lock();
    }

    protected void unlock() {
        if (_lock != null)
            _lock.unlock();
    }

    @Override
    protected void assertOpen() {
        _query.assertOpen();
    }

    @Override
    public OrderedMap<Object, Class<?>> getParamTypes() {
        return _query.getOrderedParameterTypes();
    }

    public String toString() {
        String result = _query.getQueryString(); 
        return result != null ? result : _id;
    }
}
