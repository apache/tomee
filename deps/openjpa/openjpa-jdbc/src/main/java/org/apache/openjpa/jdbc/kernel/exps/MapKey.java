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

import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Returns the key of a map value.
 *
 * @author Catalina Wei
 * @since 2.0.0
 */
public class MapKey
    extends AbstractVal {

    private final Val _key;
    private ClassMetaData _meta = null;
    private Class _cast = null;
    private Class _type = null;

    /**
     * Constructor. Provide the map value to operate on.
     */
    public MapKey(Val key) {
        ((PCPath) key).getKey();
        _key = key;
    }

    /**
     * Expression state.
     */
    public static class KeyExpState
        extends ExpState {
        public ExpState key;
        public ExpState val;
    
        KeyExpState(ExpState key) {
            this.key = key;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state,
        SQLBuffer sql, int index) {
        KeyExpState estate = (KeyExpState) state;
        _key.appendTo(sel, ctx, estate.key, sql, index);
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state,
        Val other, ExpState otherState) {
        KeyExpState estate = (KeyExpState) state;
        _key.calculateValue(sel, ctx, estate.key, other, otherState);
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
        KeyExpState estate = (KeyExpState) state;
        _key.groupBy(sel, ctx, estate.key);
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state,
        boolean asc) {
        KeyExpState estate = (KeyExpState) state;
        _key.orderBy(sel, ctx, estate.key, asc);
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        ExpState key = _key.initialize(sel, ctx, flags);
        return new KeyExpState(key);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public Object load(ExpContext ctx, ExpState state, Result res)
        throws SQLException {
        KeyExpState estate = (KeyExpState) state;
        Object key = _key.load(ctx, estate.key, res);
        return key;
    }

    public void select(Select sel, ExpContext ctx, ExpState state, boolean pks)
    {
        selectColumns(sel, ctx, state, pks);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state,
        boolean pks) {
        KeyExpState estate = (KeyExpState) state;
        _key.selectColumns(sel, ctx, estate.key, pks);
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public Class getType() {
        return _key.getType();
    }

    public void setImplicitType(Class type) {
    }

    public void setMetaData(ClassMetaData meta) {
        _meta = meta;        
    }

    public Object toDataStoreValue(Select sel, ExpContext ctx, ExpState state, 
        Object val) {
        KeyExpState estate = (KeyExpState) state;
        return _key.toDataStoreValue(sel, ctx, 
            estate.key, val);
    }
}
