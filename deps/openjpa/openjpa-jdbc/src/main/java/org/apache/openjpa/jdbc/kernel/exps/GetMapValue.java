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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.meta.strats.ContainerFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.LRSMapFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.RelationStrategies;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Returns the value of the given map's key.
 *
 * @author Marc Prud'hommeaux
 */
class GetMapValue
    extends AbstractVal {

    private final Val _map;
    private final Val _key;
    private final String _alias;
    private ClassMetaData _meta = null;
    private Class _cast = null;

    /**
     * Constructor. Provide the map and key to operate on.
     */
    public GetMapValue(Val map, Val key, String alias) {
        _map = map;
        _key = key;
        _alias = alias;
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public void setMetaData(ClassMetaData meta) {
        _meta = meta;
    }

    public boolean isVariable() {
        return false;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        return _map.getType();
    }

    public void setImplicitType(Class type) {
        _cast = type;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        ExpState mapState = _map.initialize(sel, ctx, 0);
        ExpState keyState = _key.initialize(sel, ctx, 0);
        return new GetMapValueExpState(sel.and(mapState.joins, keyState.joins),
            mapState, keyState);
    }

    /**
     * Expression state.
     */
    private static class GetMapValueExpState
        extends ExpState {

        public final ExpState mapState;
        public final ExpState keyState;

        public GetMapValueExpState(Joins joins, ExpState mapState, 
            ExpState keyState) {
            super(joins);
            this.mapState = mapState;
            this.keyState = keyState;
        }
    }

    public Object toDataStoreValue(Select sel, ExpContext ctx, ExpState state, 
        Object val) {
        GetMapValueExpState gstate = (GetMapValueExpState) state;
        return _map.toDataStoreValue(sel, ctx, gstate.mapState, val);
    }


    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        sel.select(newSQLBuffer(sel, ctx, state).append(" AS ").append(_alias),
            this);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state,
        boolean pks) {
        GetMapValueExpState gstate = (GetMapValueExpState) state;
        _map.selectColumns(sel, ctx, gstate.mapState, true);
        _key.selectColumns(sel, ctx, gstate.keyState, true);
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
        sel.groupBy(newSQLBuffer(sel, ctx, state));
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state, 
        boolean asc) {
        sel.orderBy(_alias, asc, false);
    }

    private SQLBuffer newSQLBuffer(Select sel, ExpContext ctx, ExpState state) {
        calculateValue(sel, ctx, state, null, null);
        SQLBuffer buf = new SQLBuffer(ctx.store.getDBDictionary());
        appendTo(sel, ctx, state, buf, 0);
        return buf;
    }

    public Object load(ExpContext ctx, ExpState state, Result res)
        throws SQLException {
        return Filters.convert(res.getObject(this,
            JavaSQLTypes.JDBC_DEFAULT, null), getType());
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        GetMapValueExpState gstate = (GetMapValueExpState) state;
        _map.calculateValue(sel, ctx, gstate.mapState, null, null);
        _key.calculateValue(sel, ctx, gstate.keyState, null, null);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        if (!(_map instanceof PCPath))
            throw new UnsupportedOperationException();
        if (!(_key instanceof Const))
            throw new UnsupportedOperationException();

        GetMapValueExpState gstate = (GetMapValueExpState) state;
        PCPath map = (PCPath) _map;
        Object key = ((Const) _key).getValue(ctx, gstate.keyState);
        FieldMapping field = map.getFieldMapping(gstate.mapState);
        if (!(field.getStrategy() instanceof LRSMapFieldStrategy))
            throw new UnsupportedOperationException();

        LRSMapFieldStrategy strat = (LRSMapFieldStrategy) field.getStrategy();
        ClassMapping[] clss = strat.getIndependentValueMappings(true);
        if (clss != null && clss.length > 1)
            throw RelationStrategies.unjoinable(field);

        ClassMapping cls = (clss == null || clss.length == 0) ? null : clss[0];
        ForeignKey fk = strat.getJoinForeignKey(cls);

        // manually create a subselect for the Map's value
        sql.append("(SELECT ");
        Column[] values = field.getElementMapping().getColumns();
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                sql.append(", ");
            sql.append(values[i].getTable()).append(".").append(values[i]);
        }
        sql.append(" FROM ").append(values[0].getTable());
        sql.append(" WHERE ");

        // add in the joins
        ContainerFieldStrategy.appendUnaliasedJoin(sql, sel, null, 
            ctx.store.getDBDictionary(), field, fk);
        sql.append(" AND ");

        key = strat.toKeyDataStoreValue(key, ctx.store);
        Column[] cols = strat.getKeyColumns(cls);
        Object[] vals = (cols.length == 1) ? null : (Object[]) key;

        for (int i = 0; i < cols.length; i++) {
            sql.append(cols[i].getTable()).append(".").append(cols[i]);
            if (vals == null)
                sql.append((key == null) ? " IS " : " = ").
                    appendValue(key, cols[i]);
            else
                sql.append((vals[i] == null) ? " IS " : " = ").
                    appendValue(vals[i], cols[i]);
        }
        sql.append(")");
    }
}
