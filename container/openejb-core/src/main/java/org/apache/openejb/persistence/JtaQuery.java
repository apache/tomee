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

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The JtaQuery is a wrapper around a query and and entity manager that automatically closes the entity managers
 * when the query is finished.  This implementation is only for non-transaction queryies
 */
public class JtaQuery implements Query {
    private final EntityManager entityManager;
    private final Query query;

    public JtaQuery(EntityManager entityManager, Query query) {
        this.entityManager = entityManager;
        this.query = query;
    }

    public List getResultList() {
        try {
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    public Object getSingleResult() {
        try {
            return query.getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    public int executeUpdate() {
        try {
            return query.executeUpdate();
        } finally {
            entityManager.close();
        }
    }

    public Query setMaxResults(int i) {
        query.setMaxResults(i);
        return this;
    }

    public Query setFirstResult(int i) {
        query.setFirstResult(i);
        return this;
    }

    public Query setFlushMode(FlushModeType flushModeType) {
        query.setFlushMode(flushModeType);
        return this;
    }

    public Query setHint(String s, Object o) {
        query.setHint(s, o);
        return this;
    }

    public Query setParameter(String s, Object o) {
        query.setParameter(s, o);
        return this;
    }

    public Query setParameter(String s, Date date, TemporalType temporalType) {
        query.setParameter(s, date, temporalType);
        return this;
    }

    public Query setParameter(String s, Calendar calendar, TemporalType temporalType) {
        query.setParameter(s, calendar, temporalType);
        return this;
    }

    public Query setParameter(int i, Object o) {
        query.setParameter(i, o);
        return this;
    }

    public Query setParameter(int i, Date date, TemporalType temporalType) {
        query.setParameter(i, date, temporalType);
        return this;
    }

    public Query setParameter(int i, Calendar calendar, TemporalType temporalType) {
        query.setParameter(i, calendar, temporalType);
        return this;
    }

    // JPA 2.0
    /* (non-Javadoc)
     * @see javax.persistence.Query#getFirstResult()
     */
    public int getFirstResult() {
        return query.getFirstResult();
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getFlushMode()
     */
    public FlushModeType getFlushMode() {
        return query.getFlushMode();
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getHints()
     */
    public Map<String, Object> getHints() {
        return query.getHints();
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getLockMode()
     */
    public LockModeType getLockMode() {
        return query.getLockMode();
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getMaxResults()
     */
    public int getMaxResults() {
        return query.getMaxResults();
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getParameter(java.lang.String)
     */
    public Parameter<?> getParameter(String name) {
        return query.getParameter(name);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getParameter(int)
     */
    public Parameter<?> getParameter(int position) {
        return query.getParameter(position);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getParameter(java.lang.String, java.lang.Class)
     */
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        return query.getParameter(name, type);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getParameter(int, java.lang.Class)
     */
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        return query.getParameter(position, type);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getParameterValue(javax.persistence.Parameter)
     */
    public <T> T getParameterValue(Parameter<T> param) {
        return query.getParameterValue(param);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getParameterValue(java.lang.String)
     */
    public Object getParameterValue(String name) {
        return query.getParameterValue(name);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getParameterValue(int)
     */
    public Object getParameterValue(int position) {
        return query.getParameterValue(position);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#getParameters()
     */
    public Set<Parameter<?>> getParameters() {
        return query.getParameters();
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#isBound(javax.persistence.Parameter)
     */
    public boolean isBound(Parameter<?> param) {
        return query.isBound(param);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setLockMode(javax.persistence.LockModeType)
     */
    public Query setLockMode(LockModeType lockMode) {
        return query.setLockMode(lockMode);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(javax.persistence.Parameter, java.lang.Object)
     */
    public <T> Query setParameter(Parameter<T> param, T value) {
        return query.setParameter(param, value);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(javax.persistence.Parameter, java.util.Calendar, javax.persistence.TemporalType)
     */
    public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        return query.setParameter(param, value, temporalType);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#setParameter(javax.persistence.Parameter, java.util.Date, javax.persistence.TemporalType)
     */
    public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        return query.setParameter(param, value, temporalType);
    }

    /* (non-Javadoc)
     * @see javax.persistence.Query#unwrap(java.lang.Class)
     */
    public <T> T unwrap(Class<T> cls) {
        return query.unwrap(cls);
    }
}
