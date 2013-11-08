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
 * General case expression.
 *
 * @author Catalina Wei
 */
public class GeneralCaseExpression
    extends AbstractVal {

    private final Exp[] _exp;
    private final Val _val;
    private ClassMetaData _meta = null;
    private Class _cast = null;
    private Value other = null;
    private ExpState otherState = null;

    /**
     * Constructor.
     */
    public GeneralCaseExpression(Exp[] exp, Val val) {
        _exp = exp;
        _val = val;
    }

    public Exp[] getExp() {
        return _exp;
    }

    public Val getVal() {
        return _val;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        Class type = _val.getType();
        for (int i = 0; i < _exp.length; i++)
            type = Filters.promote(type,
                ((WhenCondition) _exp[i]).getVal().getType());
        if (type == Raw.class)
            return String.class;
        return type;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        ExpState[] states = new ExpState[_exp.length+1];
        Joins joins = null;
        int i = 0;
        for (; i < _exp.length; i++) {
            states[i] = _exp[i].initialize(sel, ctx, null);
            if (joins == null)
                joins = states[i].joins;
            else
                joins = sel.and(joins, states[i].joins);
        }
        states[i] = _val.initialize(sel, ctx, 0);
        if (joins == null)
            joins = states[i].joins;
        else
            joins = sel.and(joins, states[i].joins);
        return new GeneralCaseExpState(joins, states);
    }

    private static class GeneralCaseExpState
        extends ExpState {
        
        public ExpState[] states;
        
        public GeneralCaseExpState(Joins joins, ExpState[] states) {
            super(joins);
            this.states = states;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf, int index) {
        GeneralCaseExpState cstate = (GeneralCaseExpState) state;

        buf.append(" CASE ");
        int i = 0;
        for (; i < _exp.length; i++)
            _exp[i].appendTo(sel, ctx, cstate.states[i], buf);

        buf.append(" ELSE ");
        _val.appendTo(sel, ctx, cstate.states[i], buf, 0);

        buf.append(" END ");
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        GeneralCaseExpState cstate = (GeneralCaseExpState) state;
        int i = 0;
        for (; i < _exp.length; i++)
            _exp[i].selectColumns(sel, ctx, cstate.states[i], pks);
        _val.selectColumns(sel, ctx, cstate.states[i], pks);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        for (int i = 0; i < _exp.length; i++)
            _exp[i].acceptVisit(visitor);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }

    public int getId() {
        return Val.SIMPLECASE_VAL;
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state,
        Val other, ExpState otherState) {
        GeneralCaseExpState gstate = (GeneralCaseExpState) state;
        for (int i = 0; i < _exp.length; i++) {   
            BinaryOpExpState bstate = (BinaryOpExpState) gstate.states[i];
            ((WhenCondition) _exp[i]).getVal().calculateValue(sel, ctx,
                bstate.state2, other, otherState);
        }
        _val.calculateValue(sel, ctx, gstate.states[_exp.length], other, 
            otherState);
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

