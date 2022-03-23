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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The JtaQuery is a wrapper around a query and and entity manager that automatically closes the entity managers
 * when the query is finished.  This implementation is only for non-transaction queryies
 */
public class JtaQuery implements Query {
    private EntityManager entityManager;
    private final Object[] args;
    private final Method method;
    private final JtaEntityManager jtaEntityManager;
    private final Collection<QueryOperation> appliedOperations = new ArrayList<>();

    private boolean underTx;
    private boolean unwrap;
    private Query query;

    public JtaQuery(final EntityManager entityManager, final JtaEntityManager jtaEntityManager, final Method method, final Object... args) {
        this.entityManager = entityManager;
        this.jtaEntityManager = jtaEntityManager;
        this.method = method;
        this.args = args;
        this.underTx = jtaEntityManager.isTransactionActive();
        createQuery();
    }

    private Query createQuery() {
        if (!unwrap) {
            query = jtaEntityManager.createQuery(queryType(), entityManager, method, args);
        }
        if (!underTx) {
            for (final QueryOperation op : appliedOperations) {
                query = op.apply(query);
            }
        }
        return query;
    }

    protected Class<? extends Query> queryType() {
        return Query.class;
    }

    private EntityManager getEntityManager() {
        if (!underTx) {
            entityManager = jtaEntityManager.getEntityManager();
            this.underTx = jtaEntityManager.isTransactionActive();
            createQuery();
        }
        return entityManager;
    }

    public List getResultList() {
        final EntityManager em = getEntityManager();
        try {
            return query.getResultList();
        } finally {
            jtaEntityManager.closeIfNoTx(em);
        }
    }

    public Object getSingleResult() {
        final EntityManager em = getEntityManager();
        try {
            return query.getSingleResult();
        } finally {
            jtaEntityManager.closeIfNoTx(em);
        }
    }

    public int executeUpdate() {
        final EntityManager em = getEntityManager();
        try {
            return query.executeUpdate();
        } finally {
            jtaEntityManager.closeIfNoTx(em);
        }
    }

    public Query setMaxResults(final int i) {
        query.setMaxResults(i);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setMaxResults(i);
                }
            });
        }
        return this;
    }

    public Query setFirstResult(final int i) {
        query.setFirstResult(i);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setFirstResult(i);
                }
            });
        }
        return this;
    }

    public Query setFlushMode(final FlushModeType flushModeType) {
        query.setFlushMode(flushModeType);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setFlushMode(flushModeType);
                }
            });
        }
        return this;
    }

    public Query setHint(final String s, final Object o) {
        query.setHint(s, o);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setHint(s, o);
                }
            });
        }
        return this;
    }

    public Query setParameter(final String s, final Object o) {
        query.setParameter(s, o);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(s, o);
                }
            });
        }
        return this;
    }

    public Query setParameter(final String s, final Date date, final TemporalType temporalType) {
        query.setParameter(s, date, temporalType);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(s, date, temporalType);
                }
            });
        }
        return this;
    }

    public Query setParameter(final String s, final Calendar calendar, final TemporalType temporalType) {
        query.setParameter(s, calendar, temporalType);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(s, calendar, temporalType);
                }
            });
        }
        return this;
    }

    public Query setParameter(final int i, final Object o) {
        query.setParameter(i, o);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(i, o);
                }
            });
        }
        return this;
    }

    public Query setParameter(final int i, final Date date, final TemporalType temporalType) {
        query.setParameter(i, date, temporalType);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(i, date, temporalType);
                }
            });
        }
        return this;
    }

    public Query setParameter(final int i, final Calendar calendar, final TemporalType temporalType) {
        query.setParameter(i, calendar, temporalType);
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(i, calendar, temporalType);
                }
            });
        }
        return this;
    }

    // JPA 2.0
    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getFirstResult()
     */
    public int getFirstResult() {
        return query.getFirstResult();
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getFlushMode()
     */
    public FlushModeType getFlushMode() {
        return query.getFlushMode();
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getHints()
     */
    public Map<String, Object> getHints() {
        return query.getHints();
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getLockMode()
     */
    public LockModeType getLockMode() {
        return query.getLockMode();
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getMaxResults()
     */
    public int getMaxResults() {
        return query.getMaxResults();
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getParameter(java.lang.String)
     */
    public Parameter<?> getParameter(final String name) {
        return query.getParameter(name);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getParameter(int)
     */
    public Parameter<?> getParameter(final int position) {
        return query.getParameter(position);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getParameter(java.lang.String, java.lang.Class)
     */
    public <T> Parameter<T> getParameter(final String name, final Class<T> type) {
        return query.getParameter(name, type);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getParameter(int, java.lang.Class)
     */
    public <T> Parameter<T> getParameter(final int position, final Class<T> type) {
        return query.getParameter(position, type);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getParameterValue(jakarta.persistence.Parameter)
     */
    public <T> T getParameterValue(final Parameter<T> param) {
        return query.getParameterValue(param);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getParameterValue(java.lang.String)
     */
    public Object getParameterValue(final String name) {
        return query.getParameterValue(name);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getParameterValue(int)
     */
    public Object getParameterValue(final int position) {
        return query.getParameterValue(position);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#getParameters()
     */
    public Set<Parameter<?>> getParameters() {
        return query.getParameters();
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#isBound(jakarta.persistence.Parameter)
     */
    public boolean isBound(final Parameter<?> param) {
        return query.isBound(param);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#setLockMode(jakarta.persistence.LockModeType)
     */
    public Query setLockMode(final LockModeType lockMode) {
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setLockMode(lockMode);
                }
            });
        }
        return query.setLockMode(lockMode);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#setParameter(jakarta.persistence.Parameter, java.lang.Object)
     */
    public <T> Query setParameter(final Parameter<T> param, final T value) {
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(param, value);
                }
            });
        }
        return query.setParameter(param, value);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#setParameter(jakarta.persistence.Parameter, java.util.Calendar, jakarta.persistence.TemporalType)
     */
    public Query setParameter(final Parameter<Calendar> param, final Calendar value, final TemporalType temporalType) {
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(param, value, temporalType);
                }
            });
        }
        return query.setParameter(param, value, temporalType);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#setParameter(jakarta.persistence.Parameter, java.util.Date, jakarta.persistence.TemporalType)
     */
    public Query setParameter(final Parameter<Date> param, final Date value, final TemporalType temporalType) {
        if (!underTx) {
            appliedOperations.add(new QueryOperation() {
                @Override
                public Query apply(final Query query) {
                    return query.setParameter(param, value, temporalType);
                }
            });
        }
        return query.setParameter(param, value, temporalType);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.Query#unwrap(java.lang.Class)
     */
    public <T> T unwrap(final Class<T> cls) {
        unwrap = true;

        if (getClass() == cls) {
            return cls.cast(this);
        }
        return query.unwrap(cls);
    }
}
