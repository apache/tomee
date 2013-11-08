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
package org.apache.openjpa.jdbc.meta.strats;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.jdbc.sql.Union;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.AbstractLRSProxyCollection;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Large result set collection.
 *
 * @author Abe White
 */
public class LRSProxyCollection
    extends AbstractLRSProxyCollection {

    private static final Localizer _loc = Localizer.forPackage
        (LRSProxyCollection.class);

    private final LRSCollectionFieldStrategy _strat;

    public LRSProxyCollection(LRSCollectionFieldStrategy strat) {
        super(strat.getFieldMapping().getElement().getDeclaredType(),
            strat.getFieldMapping().getOrderColumn() != null);
        _strat = strat;
    }

    protected int count() {
        final ClassMapping[] elems = _strat.getIndependentElementMappings
            (false);
        final OpenJPAStateManager sm = assertOwner();
        final JDBCStore store = getStore();
        Union union = store.getSQLFactory().newUnion
            (Math.max(1, elems.length));
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                ClassMapping elem = (elems.length == 0) ? null : elems[idx];
                sel.whereForeignKey(_strat.getJoinForeignKey(elem),
                    sm.getObjectId(), _strat.getFieldMapping().
                    getDefiningMapping(), store);
            }
        });

        try {
            return union.getCount(store);
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        }
    }

    protected boolean has(final Object obj) {
        final ClassMapping[] elems = _strat.getIndependentElementMappings
            (false);
        final OpenJPAStateManager sm = assertOwner();
        final JDBCStore store = getStore();
        Union union = store.getSQLFactory().newUnion
            (Math.max(1, elems.length));
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                ClassMapping elem = (elems.length == 0) ? null : elems[idx];
                sel.whereForeignKey(_strat.getJoinForeignKey(elem),
                    sm.getObjectId(), _strat.getFieldMapping().
                    getDefiningMapping(), store);

                Object val = _strat.toDataStoreValue(obj, store);
                Column[] cols = _strat.getElementColumns(elem);
                Object[] vals = (cols.length == 1) ? null : (Object[]) val;
                SQLBuffer sql = new SQLBuffer(store.getDBDictionary());
                for (int i = 0; i < cols.length; i++) {
                    if (i > 0)
                        sql.append(" AND ");

                    sql.append(sel.getColumnAlias(cols[i]));
                    if (vals == null)
                        sql.append((val == null) ? " IS " : " = ").
                            appendValue(val, cols[i]);
                    else
                        sql.append((vals[i] == null) ? " IS " : " = ").
                            appendValue(vals[i], cols[i]);
                }
                sel.where(sql);
            }
        });

        try {
            return union.getCount(store) > 0;
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        }
    }

    protected Iterator itr() {
        final ClassMapping[] elems = _strat.getIndependentElementMappings(true);
        final OpenJPAStateManager sm = assertOwner();
        final JDBCStore store = getStore();
        final JDBCFetchConfiguration fetch = store.getFetchConfiguration();
        final Joins[] resJoins = new Joins[Math.max(1, elems.length)];
        final FieldMapping fm = _strat.getFieldMapping();

        Union union = store.getSQLFactory().newUnion
            (Math.max(1, elems.length));
        if (fetch.getSubclassFetchMode(fm.getElementMapping().
            getTypeMapping()) != fetch.EAGER_JOIN)
            union.abortUnion();
        union.setLRS(true);
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                ClassMapping elem = (elems.length == 0) ? null : elems[idx];
                sel.whereForeignKey(_strat.getJoinForeignKey(elem),
                    sm.getObjectId(), fm.getDefiningMapping(), store);

                // order before select in case we're faking union with
                // multiple selects; order vals used to merge results
                fm.orderLocal(sel, elem, null);
                resJoins[idx] = _strat.joinElementRelation(sel.newJoins(),
                    elem);
                fm.orderRelation(sel, elem, resJoins[idx]);
                _strat.selectElement(sel, elem, store, fetch, fetch.EAGER_JOIN,
                    resJoins[idx]);
            }
        });

        try {
            Result res = union.execute(store, fetch);
            return new ResultIterator(sm, store, fetch, res, resJoins);
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        }
    }

    private OpenJPAStateManager assertOwner() {
        OpenJPAStateManager sm = getOwner();
        if (sm == null)
            throw new InvalidStateException(_loc.get("lrs-no-owner",
                _strat.getFieldMapping()));
        return sm;
    }

    private JDBCStore getStore() {
        return (JDBCStore) getOwner().getContext().getStoreManager().
            getInnermostDelegate();
    }

    /**
     * Closeable iterator built around a JDBC result.
     */
    private class ResultIterator
        implements Iterator, Closeable {

        private final OpenJPAStateManager _sm;
        private final JDBCStore _store;
        private final JDBCFetchConfiguration _fetch;
        private final Result _res;
        private final Joins[] _joins;
        private Boolean _next = null;

        public ResultIterator(OpenJPAStateManager sm, JDBCStore store,
            JDBCFetchConfiguration fetch, Result res, Joins[] joins) {
            _sm = sm;
            _store = store;
            _fetch = fetch;
            _res = res;
            _joins = joins;
        }

        public boolean hasNext() {
            if (_next == null) {
                try {
                    _next = (_res.next()) ? Boolean.TRUE : Boolean.FALSE;
                } catch (SQLException se) {
                    throw SQLExceptions.getStore(se, _store.getDBDictionary());
                }
            }
            return _next.booleanValue();
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();
            try {
                _next = null;
                return _strat.loadElement(_sm, _store, _fetch, _res,
                    _joins[_res.indexOf()]);
            } catch (SQLException se) {
                throw SQLExceptions.getStore(se, _store.getDBDictionary());
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void close() {
            _next = Boolean.FALSE;
            _res.close();
        }
    }
}

