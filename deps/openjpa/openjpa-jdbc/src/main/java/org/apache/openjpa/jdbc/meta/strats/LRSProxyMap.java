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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
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
import org.apache.openjpa.util.AbstractLRSProxyMap;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Large result set map.
 *
 * @author Abe White
 */
class LRSProxyMap
    extends AbstractLRSProxyMap {

    private static final Localizer _loc = Localizer.forPackage
        (LRSProxyMap.class);

    private final LRSMapFieldStrategy _strat;

    public LRSProxyMap(LRSMapFieldStrategy strat) {
        super(strat.getFieldMapping().getKey().getDeclaredType(),
            strat.getFieldMapping().getElement().getDeclaredType());
        _strat = strat;
    }

    protected synchronized int count() {
        boolean derivedVal = _strat.getFieldMapping().getElement().
            getValueMappedBy() != null;
        final ClassMapping[] clss = (derivedVal)
            ? _strat.getIndependentKeyMappings(false)
            : _strat.getIndependentValueMappings(false);
        final OpenJPAStateManager sm = assertOwner();
        final JDBCStore store = getStore();
        Union union = store.getSQLFactory().newUnion
            (Math.max(1, clss.length));
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                ClassMapping cls = (clss.length == 0) ? null : clss[idx];
                sel.whereForeignKey(_strat.getJoinForeignKey(cls),
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

    protected boolean hasKey(Object key) {
        return has(key, true);
    }

    protected boolean hasValue(Object value) {
        return has(value, false);
    }

    private boolean has(final Object obj, final boolean key) {
        final boolean derivedKey = key && _strat.getFieldMapping().
            getKey().getValueMappedBy() != null;
        final boolean derivedVal = !key && _strat.getFieldMapping().
            getElement().getValueMappedBy() != null;

        final ClassMapping[] clss = ((key && !derivedKey) || derivedVal)
            ? _strat.getIndependentKeyMappings(derivedVal)
            : _strat.getIndependentValueMappings(derivedKey);
        final OpenJPAStateManager sm = assertOwner();
        final JDBCStore store = getStore();

        Union union = store.getSQLFactory().newUnion
            (Math.max(1, clss.length));
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                ClassMapping cls = (clss.length == 0) ? null : clss[idx];
                sel.whereForeignKey(_strat.getJoinForeignKey(cls),
                    sm.getObjectId(), _strat.getFieldMapping().
                    getDefiningMapping(), store);

                Joins joins = null;
                Column[] cols;
                Object val;
                if (key) {
                    if (derivedKey)
                        joins = _strat.joinValueRelation(sel.newJoins(), cls);
                    val = _strat.toKeyDataStoreValue(obj, store);
                    cols = _strat.getKeyColumns(cls);
                } else {
                    if (derivedVal)
                        joins = _strat.joinKeyRelation(sel.newJoins(), cls);
                    val = _strat.toDataStoreValue(obj, store);
                    cols = _strat.getValueColumns(cls);
                }
                Object[] vals = (cols.length == 1) ? null : (Object[]) val;
                SQLBuffer sql = new SQLBuffer(store.getDBDictionary());
                for (int i = 0; i < cols.length; i++) {
                    if (i > 0)
                        sql.append(" AND ");

                    sql.append(sel.getColumnAlias(cols[i], joins));
                    if (vals == null)
                        sql.append((val == null) ? " IS " : " = ").
                            appendValue(val, cols[i]);
                    else
                        sql.append((vals[i] == null) ? " IS " : " = ").
                            appendValue(vals[i], cols[i]);
                }
                sel.where(sql, joins);
            }
        });

        try {
            return union.getCount(store) > 0;
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        }
    }

    protected Collection keys(final Object obj) {
        final OpenJPAStateManager sm = assertOwner();
        final JDBCStore store = getStore();
        if (_strat.getFieldMapping().getKey().getValueMappedBy() != null) {
            Object key = _strat.deriveKey(store, obj);
            if (hasKey(key))
                return Collections.singleton(key);
            return Collections.EMPTY_LIST;
        }

        final ClassMapping[] clss = _strat.getIndependentKeyMappings(true);
        final JDBCFetchConfiguration fetch = store.getFetchConfiguration();
        final Joins[] resJoins = new Joins[Math.max(1, clss.length)];

        Union union = store.getSQLFactory().newUnion
            (Math.max(1, clss.length));
        if (fetch.getSubclassFetchMode(_strat.getFieldMapping().
            getKeyMapping().getTypeMapping()) != fetch.EAGER_JOIN)
            union.abortUnion();
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                ClassMapping cls = (clss.length == 0) ? null : clss[idx];
                sel.whereForeignKey(_strat.getJoinForeignKey(cls),
                    sm.getObjectId(), _strat.getFieldMapping().
                    getDefiningMapping(), store);
                if (_strat.getFieldMapping().getElement().getValueMappedBy()
                    != null)
                    resJoins[idx] = _strat.joinKeyRelation(sel.newJoins(),
                        cls);

                Object val = _strat.toDataStoreValue(obj, store);
                Column[] cols = _strat.getValueColumns(cls);
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

                if (resJoins[idx] == null)
                    resJoins[idx] = _strat.joinKeyRelation(sel.newJoins(),
                        cls);
                _strat.selectKey(sel, cls, sm, store, fetch, resJoins[idx]);
            }
        });

        Result res = null;
        Collection keys = new ArrayList(3);
        try {
            res = union.execute(store, fetch);
            while (res.next())
                keys.add(_strat.loadKey(sm, store, fetch, res,
                    resJoins[res.indexOf()]));
            return keys;
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        } finally {
            if (res != null)
                res.close();
        }
    }

    protected Object value(final Object obj) {
        final OpenJPAStateManager sm = assertOwner();
        final JDBCStore store = getStore();
        if (_strat.getFieldMapping().getElement().getValueMappedBy() != null) {
            Object val = _strat.deriveValue(store, obj);
            if (hasValue(val))
                return val;
            return null;
        }

        final JDBCFetchConfiguration fetch = store.getFetchConfiguration();
        final ClassMapping[] clss = _strat.getIndependentValueMappings(true);
        final Joins[] resJoins = new Joins[Math.max(1, clss.length)];
        Union union = store.getSQLFactory().newUnion(Math.max(1, clss.length));
        union.setExpectedResultCount(1, false);
        if (fetch.getSubclassFetchMode(_strat.getFieldMapping().
            getElementMapping().getTypeMapping())
            != JDBCFetchConfiguration.EAGER_JOIN)
            union.abortUnion();
        union.select(new Union.Selector() {
            public void select(Select sel, int idx) {
                ClassMapping cls = (clss.length == 0) ? null : clss[idx];
                sel.whereForeignKey(_strat.getJoinForeignKey(cls),
                    sm.getObjectId(), _strat.getFieldMapping().
                    getDefiningMapping(), store);
                if (_strat.getFieldMapping().getKey().getValueMappedBy()
                    != null)
                    resJoins[idx] = _strat.joinValueRelation(sel.newJoins(),
                        cls);

                Object key = _strat.toKeyDataStoreValue(obj, store);
                Column[] cols = _strat.getKeyColumns(cls);
                Object[] vals = (cols.length == 1) ? null : (Object[]) key;
                SQLBuffer sql = new SQLBuffer(store.getDBDictionary());
                for (int i = 0; i < cols.length; i++) {
                    if (i > 0)
                        sql.append(" AND ");

                    sql.append(sel.getColumnAlias(cols[i], resJoins[idx]));
                    if (vals == null)
                        sql.append((key == null) ? " IS " : " = ").
                            appendValue(key, cols[i]);
                    else
                        sql.append((vals[i] == null) ? " IS " : " = ").
                            appendValue(vals[i], cols[i]);
                }
                sel.where(sql, resJoins[idx]);

                if (resJoins[idx] == null)
                    resJoins[idx] = _strat.joinValueRelation(sel.newJoins(),
                        cls);
                _strat.selectValue(sel, cls, sm, store, fetch, resJoins[idx]);
            }
        });

        Result res = null;
        try {
            res = union.execute(store, fetch);
            if (res.next())
                return _strat.loadValue(sm, store, fetch, res,
                    resJoins[res.indexOf()]);
            return null;
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        } finally {
            if (res != null)
                res.close();
        }
    }

    protected Iterator itr() {
        OpenJPAStateManager sm = assertOwner();
        JDBCStore store = getStore();
        JDBCFetchConfiguration fetch = store.getFetchConfiguration();
        try {
            Joins[] joins = new Joins[2];
            Result[] res = _strat.getResults(sm, store, fetch, fetch.EAGER_JOIN,
                joins, true);
            return new ResultIterator(sm, store, fetch, res, joins);
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
     * Closeable iterator built around key and value JDBC results.
     */
    private class ResultIterator
        implements Iterator, Closeable {

        private final OpenJPAStateManager _sm;
        private final JDBCStore _store;
        private final JDBCFetchConfiguration _fetch;
        private final Result[] _res;
        private final Joins[] _joins;
        private Boolean _next = null;

        public ResultIterator(OpenJPAStateManager sm, JDBCStore store,
            JDBCFetchConfiguration fetch, Result[] res, Joins[] joins) {
            _sm = sm;
            _store = store;
            _fetch = fetch;
            _res = res;
            _joins = joins;
        }

        public boolean hasNext() {
            if (_next == null) {
                try {
                    _next = (_res[0].next()) ? Boolean.TRUE : Boolean.FALSE;
                    if (_next.booleanValue() && _res[1] != _res[0])
                        _res[1].next();
                } catch (SQLException se) {
                    throw SQLExceptions.getStore(se, _store.getDBDictionary());
                }
            }
            return _next.booleanValue();
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();
            _next = null;

            boolean keyDerived = _strat.getFieldMapping().getKey().
                getValueMappedBy() != null;
            boolean valDerived = _strat.getFieldMapping().getElement().
                getValueMappedBy() != null;
            Entry entry = new Entry();
            try {

                if (!keyDerived)
                    entry.key = _strat.loadKey(_sm, _store, _fetch, _res[0], 
                        _joins[0]);
                if (!valDerived)
                    entry.val = _strat.loadValue(_sm, _store, _fetch, _res[1], 
                        _joins[1]);
                if (keyDerived)
                    entry.key = _strat.deriveKey(_store, entry.val);
                if (valDerived)
                    entry.val = _strat.deriveValue(_store, entry.key);
                return entry;
            } catch (SQLException se) {
                throw SQLExceptions.getStore(se, _store.getDBDictionary());
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void close() {
            _next = Boolean.FALSE;
            _res[0].close();
            if (_res[1] != _res[0])
                _res[1].close();
        }
    }

    /**
     * Map.Entry struct.
     */
    private static class Entry
        implements Map.Entry {

        public Object key;
        public Object val;

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return val;
        }

        public Object setValue(Object val) {
            throw new UnsupportedOperationException();
        }
    }
}

