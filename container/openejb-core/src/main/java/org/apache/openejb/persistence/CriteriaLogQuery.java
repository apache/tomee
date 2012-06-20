/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.persistence;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class CriteriaLogQuery<T> implements TypedQuery<T> {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_JPA, CriteriaLogQuery.class);
    private static final Map<Class<?>, Method> methodsCache = new ConcurrentHashMap<Class<?>, Method>();
    private static final Map<Class<?>, Class<?>> unwrapCache = new ConcurrentHashMap<Class<?>, Class<?>>();
    private static final String GET_QUERY_STRING_MTD = "getQueryString";

    private final TypedQuery<T> delegate;
    private final String logLevel;

    public CriteriaLogQuery(final TypedQuery<T> query, final String level) {
        delegate = query;
        logLevel = level.toLowerCase();
    }

    private void logJPQLQuery() {
        final Class<?> clazz = delegate.getClass();
        Method mtd = methodsCache.get(clazz);
        Class<?> unwrapQuery = unwrapCache.get(clazz);
        if (mtd == null) {
            try { // openjpa
                mtd = clazz.getMethod(GET_QUERY_STRING_MTD);
            } catch (NoSuchMethodException e) {
                try { // hibernate
                    unwrapQuery = clazz.getClassLoader().loadClass("org.hibernate.Query");
                    unwrapCache.put(clazz, unwrapQuery);
                    mtd = unwrapQuery.getMethod(GET_QUERY_STRING_MTD);
                } catch (Exception e2) {
                    try { // fallback
                        mtd = getClass().getMethod(GET_QUERY_STRING_MTD);
                    } catch (NoSuchMethodException shouldntOccur) {
                        // ignored
                    }
                }
            }
            methodsCache.put(clazz, mtd);
        }
        logJPQLQuery(unwrapQuery, mtd);
    }

    public String getQueryString() {
        return delegate.getClass().getName() + " doesn't support getQueryString() method: '" + delegate.toString() + "'";
    }

    private void logJPQLQuery(final Class<?> unwrap, final Method mtd) {
        String query = null;
        Object realQuery = delegate;
        if (unwrap != null) {
            realQuery = delegate.unwrap(unwrap);
        }

        try {
            query = (String) mtd.invoke(realQuery);
        } catch (Exception e) {
            try {
                query = getQueryString();
            } catch (Exception ignored) {
                // no-op
            }
        }

        final String msg = "executing query '" + query + "'";
        if (logLevel.equals("info")) {
            LOGGER.info(msg);
        } else if (logLevel.equals("debug") || logLevel.equals("fine") || logLevel.equals("finest")) {
            LOGGER.debug(msg);
        } else if (logLevel.equals("error")) {
            LOGGER.error(msg);
        } else if (logLevel.equals("fatal")) {
            LOGGER.fatal(msg);
        } else if (logLevel.equals("warning") || logLevel.equals("warn")) {
            LOGGER.warning(msg);
        } else {
            LOGGER.debug(msg);
        }
    }

    @Override
    public List<T> getResultList() {
        logJPQLQuery();
        return delegate.getResultList();
    }

    @Override
    public T getSingleResult() {
        logJPQLQuery();
        return delegate.getSingleResult();
    }

    @Override
    public int executeUpdate() {
        logJPQLQuery();
        return delegate.executeUpdate();
    }

    @Override
    public TypedQuery<T> setMaxResults(int maxResult) {
        delegate.setMaxResults(maxResult);
        return this;
    }

    @Override
    public int getMaxResults() {
        return delegate.getMaxResults();
    }

    @Override
    public TypedQuery<T> setFirstResult(int startPosition) {
        delegate.setFirstResult(startPosition);
        return this;
    }

    @Override
    public int getFirstResult() {
        return delegate.getFirstResult();
    }

    @Override
    public TypedQuery<T> setHint(String hintName, Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    @Override
    public <E> TypedQuery<T> setParameter(Parameter<E> param, E value) {
        delegate.setParameter(param, value);
        return this;
    }

    @Override
    public Map<String, Object> getHints() {
        return delegate.getHints();
    }

    @Override
    public TypedQuery<T> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(String name, Object value) {
        delegate.setParameter(name, value);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(String name, Calendar value, TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(String name, Date value, TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(int position, Object value) {
        delegate.setParameter(position, value);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(int position, Calendar value, TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(int position, Date value, TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return delegate.getParameters();
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return delegate.getParameter(name);
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        return delegate.getParameter(name, type);
    }

    @Override
    public Parameter<?> getParameter(int position) {
        return delegate.getParameter(position);
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        return delegate.getParameter(position, type);
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        return delegate.isBound(param);
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        return delegate.getParameterValue(param);
    }

    @Override
    public Object getParameterValue(String name) {
        return delegate.getParameterValue(name);
    }

    @Override
    public Object getParameterValue(int position) {
        return delegate.getParameterValue(position);
    }

    @Override
    public TypedQuery<T> setFlushMode(FlushModeType flushMode) {
        return delegate.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public TypedQuery<T> setLockMode(LockModeType lockMode) {
        return delegate.setLockMode(lockMode);
    }

    @Override
    public LockModeType getLockMode() {
        return delegate.getLockMode();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return delegate.unwrap(cls);
    }
}
