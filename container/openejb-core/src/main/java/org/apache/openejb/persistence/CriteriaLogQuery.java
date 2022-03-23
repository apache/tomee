/*
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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
            } catch (final NoSuchMethodException e) {
                try { // hibernate
                    unwrapQuery = clazz.getClassLoader().loadClass("org.hibernate.Query");
                    unwrapCache.put(clazz, unwrapQuery);
                    mtd = unwrapQuery.getMethod(GET_QUERY_STRING_MTD);
                } catch (final Exception e2) {
                    try { // fallback
                        mtd = getClass().getMethod(GET_QUERY_STRING_MTD);
                    } catch (final NoSuchMethodException shouldntOccur) {
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
        } catch (final Exception e) {
            try {
                query = getQueryString();
            } catch (final Exception ignored) {
                // no-op
            }
        }

        final String msg = "executing query '" + query + "'";
        switch (logLevel) {
            case "info":
                LOGGER.info(msg);
                break;
            case "debug":
            case "fine":
            case "finest":
                LOGGER.debug(msg);
                break;
            case "error":
                LOGGER.error(msg);
                break;
            case "fatal":
                LOGGER.fatal(msg);
                break;
            case "warning":
            case "warn":
                LOGGER.warning(msg);
                break;
            default:
                LOGGER.debug(msg);
                break;
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
    public TypedQuery<T> setMaxResults(final int maxResult) {
        delegate.setMaxResults(maxResult);
        return this;
    }

    @Override
    public int getMaxResults() {
        return delegate.getMaxResults();
    }

    @Override
    public TypedQuery<T> setFirstResult(final int startPosition) {
        delegate.setFirstResult(startPosition);
        return this;
    }

    @Override
    public int getFirstResult() {
        return delegate.getFirstResult();
    }

    @Override
    public TypedQuery<T> setHint(final String hintName, final Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    @Override
    public <E> TypedQuery<T> setParameter(final Parameter<E> param, final E value) {
        delegate.setParameter(param, value);
        return this;
    }

    @Override
    public Map<String, Object> getHints() {
        return delegate.getHints();
    }

    @Override
    public TypedQuery<T> setParameter(final Parameter<Calendar> param, final Calendar value, final TemporalType temporalType) {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(final Parameter<Date> param, final Date value, final TemporalType temporalType) {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(final String name, final Object value) {
        delegate.setParameter(name, value);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(final String name, final Calendar value, final TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(final String name, final Date value, final TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(final int position, final Object value) {
        delegate.setParameter(position, value);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(final int position, final Calendar value, final TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(final int position, final Date value, final TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return delegate.getParameters();
    }

    @Override
    public Parameter<?> getParameter(final String name) {
        return delegate.getParameter(name);
    }

    @Override
    public <T> Parameter<T> getParameter(final String name, final Class<T> type) {
        return delegate.getParameter(name, type);
    }

    @Override
    public Parameter<?> getParameter(final int position) {
        return delegate.getParameter(position);
    }

    @Override
    public <T> Parameter<T> getParameter(final int position, final Class<T> type) {
        return delegate.getParameter(position, type);
    }

    @Override
    public boolean isBound(final Parameter<?> param) {
        return delegate.isBound(param);
    }

    @Override
    public <T> T getParameterValue(final Parameter<T> param) {
        return delegate.getParameterValue(param);
    }

    @Override
    public Object getParameterValue(final String name) {
        return delegate.getParameterValue(name);
    }

    @Override
    public Object getParameterValue(final int position) {
        return delegate.getParameterValue(position);
    }

    @Override
    public TypedQuery<T> setFlushMode(final FlushModeType flushMode) {
        return delegate.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public TypedQuery<T> setLockMode(final LockModeType lockMode) {
        return delegate.setLockMode(lockMode);
    }

    @Override
    public LockModeType getLockMode() {
        return delegate.getLockMode();
    }

    @Override
    public <T> T unwrap(final Class<T> cls) {
        return delegate.unwrap(cls);
    }
}
