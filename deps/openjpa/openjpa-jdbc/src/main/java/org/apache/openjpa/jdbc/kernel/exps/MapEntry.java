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
package org.apache.openjpa.jdbc.kernel.exps;

import java.sql.SQLException;
import java.util.Map;

import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Returns the Map.Entry<K,V> of a map value.
 *
 * @author Catalina Wei
 * @since 2.0.0
 */
public class MapEntry
    extends AbstractVal {

    private final Val _key;
    private final Val _val;
    private ClassMetaData _meta = null;
    private Class _cast = null;
    private Class _type = null;

    /**
     * Constructor. Provide the map value to operate on.
     */
    public MapEntry(Val key, Val val) {
        ((PCPath) key).getKey();
        _key = key;
        _val = val;
    }

    /**
     * Expression state.
     */
    public static class EntryExpState
        extends ExpState {
        public ExpState key;
        public ExpState val;
    
        EntryExpState(ExpState key, ExpState val) {
            this.key = key;
            this.val = val;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state,
        SQLBuffer sql, int index) {
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state,
        Val other, ExpState otherState) {
        _val.calculateValue(sel, ctx, state, other, otherState);
        _key.calculateValue(sel, ctx, state, other, otherState);
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        ExpState val = _val.initialize(sel, ctx, flags);
        ExpState key = _key.initialize(sel, ctx, flags);
        return new EntryExpState(key, val);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public Object load(ExpContext ctx, ExpState state, Result res)
        throws SQLException {
        EntryExpState estate = (EntryExpState) state;
        Object key = _key.load(ctx, estate.key, res);
        Object val = _val.load(ctx, estate.val, res);
        if (key == null || val == null)
            return null;
        return new Entry(key, val);
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state, boolean asc)
    {
    }

    public void select(Select sel, ExpContext ctx, ExpState state, boolean pks)
    {
        selectColumns(sel, ctx, state, pks);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state,
        boolean pks) {
        EntryExpState estate = (EntryExpState) state;
        _key.selectColumns(sel, ctx, estate.key, pks);
        _val.selectColumns(sel, ctx, estate.val, pks);
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public Class getType() {
        return Map.Entry.class;
    }

    public void setImplicitType(Class type) {
    }

    public void setMetaData(ClassMetaData meta) {
        _meta = meta;        
    }

    private static class Entry<K,V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;

        public Entry(K k, V v) {
            key = k;
            value = v;
        }
        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V v) {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object other) {
            if (other instanceof Map.Entry == false)
                return false;
            Map.Entry that = (Map.Entry)other;
            return (this.key == null ?
                that.getKey() == null : key.equals(that.getKey())) &&
                (value == null ?
                that.getValue() == null : value.equals(that.getValue()));
        }

        public int hashCode() {
            return  (key == null   ? 0 : key.hashCode()) ^
            (value == null ? 0 : value.hashCode());
        }
    }
}
