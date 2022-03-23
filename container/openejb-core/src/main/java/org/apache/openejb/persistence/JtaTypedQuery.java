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

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class is the same as {@link JtaQuery} but wraps TypedQuery instead of Query
 */
public class JtaTypedQuery<X> extends JtaQuery implements TypedQuery<X> {

    public JtaTypedQuery(final EntityManager entityManager, final JtaEntityManager jtaEm, final Method method, final Object... args) {
        super(entityManager, jtaEm, method, args);
    }

    @Override
    protected Class<? extends Query> queryType() {
        return TypedQuery.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<X> getResultList() {
        return (List<X>) super.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public X getSingleResult() {
        return (X) super.getSingleResult();
    }

    @Override
    public TypedQuery<X> setFirstResult(final int i) {
        super.setFirstResult(i);
        return this;
    }

    @Override
    public TypedQuery<X> setFlushMode(final FlushModeType flushModeType) {
        super.setFlushMode(flushModeType);
        return this;
    }

    @Override
    public TypedQuery<X> setHint(final String s, final Object o) {
        super.setHint(s, o);
        return this;
    }

    @Override
    public TypedQuery<X> setLockMode(final LockModeType lockMode) {
        super.setLockMode(lockMode);
        return this;
    }

    @Override
    public TypedQuery<X> setMaxResults(final int i) {
        super.setMaxResults(i);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(final int i, final Calendar calendar,
                                      final TemporalType temporalType) {
        super.setParameter(i, calendar, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(final int i, final Date date, final TemporalType temporalType) {
        super.setParameter(i, date, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(final int i, final Object o) {
        super.setParameter(i, o);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(final Parameter<Calendar> param, final Calendar value,
                                      final TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(final Parameter<Date> param, final Date value,
                                      final TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public <T> TypedQuery<X> setParameter(final Parameter<T> param, final T value) {
        super.setParameter(param, value);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(final String s, final Calendar calendar,
                                      final TemporalType temporalType) {
        super.setParameter(s, calendar, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(final String s, final Date date, final TemporalType temporalType) {
        super.setParameter(s, date, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(final String s, final Object o) {
        super.setParameter(s, o);
        return this;
    }

}
