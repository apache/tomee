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

import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Raw;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Coalesce expression.
 *
 * @author Catalina Wei
 */
public class CoalesceExpression
    extends AbstractVal {

    private final Val[] _vals;
    private ClassMetaData _meta = null;
    private Class _cast = null;
    private Value other = null;
    private ExpState otherState = null;

    /**
     * Constructor.
     */
    public CoalesceExpression(Val[] vals) {
        _vals = vals;
    }

    public Val[] getVal() {
        return _vals;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        Class type = _vals[0].getType();
        for (int i = 1; i < _vals.length; i++)
            type = Filters.promote(type, _vals[i].getType());
        if (type == Raw.class)
            return String.class;
        return type;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        ExpState[] states = new ExpState[_vals.length];
        Joins joins = null;
        for (int i = 0; i < _vals.length; i++) {
            states[i] = _vals[i].initialize(sel, ctx, flags);
            if (joins == null)
                joins = states[i].joins;
            else
                joins = sel.and(joins, states[i].joins);
        }
        return new CoalesceExpState(joins, states);
    }

    private static class CoalesceExpState
        extends ExpState {
        
        public ExpState[] states;
        
        public CoalesceExpState(Joins joins, ExpState[] states) {
            super(joins);
            this.states = states;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf, int index) {
        CoalesceExpState cstate = (CoalesceExpState) state;
        
        buf.append(" COALESCE("); // MySQL does not like space before bracket

        for (int i = 0; i < _vals.length; i++) {
            if (i > 0)
                buf.append(",");
            _vals[i].appendTo(sel, ctx, cstate.states[i], buf, 0);
        }

        buf.append(")");
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        CoalesceExpState cstate = (CoalesceExpState) state;

        for (int i = 0; i < _vals.length; i++)
            _vals[i].selectColumns(sel, ctx, cstate.states[i], pks);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        for (int i = 0; i < _vals.length; i++)
            _vals[i].acceptVisit(visitor);
        visitor.exit(this);
    }

    public int getId() {
        return Val.COALESCE_VAL;
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state,
        Val other, ExpState otherState) {
        CoalesceExpState cstate = (CoalesceExpState) state;
        for (int i = 0; i < _vals.length; i++)   
            _vals[i].calculateValue(sel, ctx, cstate.states[i], other, otherState);
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
        sel.groupBy(newSQLBuffer(sel, ctx, state));
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    private SQLBuffer newSQLBuffer(Select sel, ExpContext ctx, ExpState state) {
        calculateValue(sel, ctx, state, (Val)other, otherState);
        SQLBuffer buf = new SQLBuffer(ctx.store.getDBDictionary());
        appendTo(sel, ctx, state, buf, 0);
        return buf;
    }

    public Object load(ExpContext ctx, ExpState state, Result res)
        throws SQLException {
        return Filters.convert(res.getObject(this,
            JavaSQLTypes.JDBC_DEFAULT, null), getType());
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state,
        boolean asc) {
        sel.orderBy(newSQLBuffer(sel, ctx, state), asc, false, getSelectAs());
    }

    public void select(Select sel, ExpContext ctx, ExpState state, boolean pks){
        sel.select(newSQLBuffer(sel, ctx, state), this);
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public void setImplicitType(Class type) {
        _cast = type;        
    }

    public void setMetaData(ClassMetaData meta) {
        _meta = meta;
    }
    
    public void setOtherPath(Value other) {
        this.other = other;
    }
    
    public Value getOtherPath() {
        return other;
    }
    
    public void setOtherState(ExpState otherState) {
        this.otherState = otherState;
    }
    
    public ExpState getOtherState() {
        return otherState;
    }
}

