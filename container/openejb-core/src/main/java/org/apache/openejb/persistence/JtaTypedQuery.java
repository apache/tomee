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
import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Thibaut Robert
 * 
 * This class is the same as {@link JtaQuery} but wraps TypedQuery instead of Query
 */
public class JtaTypedQuery<X> extends JtaQuery implements TypedQuery<X> {

    public JtaTypedQuery(EntityManager entityManager, JtaEntityManager jtaEm, Query query) {
        super(entityManager, jtaEm, query);
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
    public TypedQuery<X> setFirstResult(int i) {
        super.setFirstResult(i);
        return this;
    }

    @Override
    public TypedQuery<X> setFlushMode(FlushModeType flushModeType) {
        super.setFlushMode(flushModeType);
        return this;
    }

    @Override
    public TypedQuery<X> setHint(String s, Object o) {    
        super.setHint(s, o);
        return this;
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lockMode) {
        super.setLockMode(lockMode);
        return this;
    }

    @Override
    public TypedQuery<X> setMaxResults(int i) {
        super.setMaxResults(i);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Calendar calendar,
            TemporalType temporalType) {
        super.setParameter(i, calendar, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Date date, TemporalType temporalType) {
        super.setParameter(i, date, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Object o) {
        super.setParameter(i, o);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value,
            TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value,
            TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
        super.setParameter(param, value);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String s, Calendar calendar,
            TemporalType temporalType) {
        super.setParameter(s, calendar, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String s, Date date, TemporalType temporalType) {
        super.setParameter(s, date, temporalType);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String s, Object o) {
        super.setParameter(s, o);
        return this;
    }

}
