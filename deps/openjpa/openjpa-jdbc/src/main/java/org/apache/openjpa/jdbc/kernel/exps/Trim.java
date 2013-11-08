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

import java.lang.Math;
import java.sql.SQLException;

import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Returns the number of characters in a string.
 *
 * @author Marc Prud'hommeaux
 */
public class Trim
    extends AbstractVal {

    private final Val _val;
    private final Val _trimChar;
    private final Boolean _where;
    private ClassMetaData _meta = null;

    /**
     * Constructor. Provide the string to operate on.
     */
    public Trim(Val val, Val trimChar, Boolean where) {
        _val = val;
        _trimChar = trimChar;
        _where = where;
    }

    public Val getVal() {
        return _val;
    }

    public Val getTrimChar() {
        return _trimChar;
    }

    public Boolean getWhere(){
        return _where;
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public void setMetaData(ClassMetaData meta) {
        _meta = meta;
    }

    public Class getType() {
        return String.class;
    }

    public void setImplicitType(Class type) {
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        ExpState valueState =  _val.initialize(sel, ctx, 0);
        ExpState charState = _trimChar.initialize(sel, ctx, 0);
        return new TrimExpState(sel.and(valueState.joins, charState.joins), 
            valueState, charState);
    }

    /**
     * Expression state.
     */
    private static class TrimExpState
        extends ExpState {

        public final ExpState valueState;
        public final ExpState charState;

        public TrimExpState(Joins joins, ExpState valueState, 
            ExpState charState) {
            super(joins);
            this.valueState = valueState;
            this.charState = charState;
        }
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        sel.select(newSQLBuffer(sel, ctx, state), this);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        TrimExpState tstate = (TrimExpState) state;
        _val.selectColumns(sel, ctx, tstate.valueState, true);
        _trimChar.selectColumns(sel, ctx, tstate.charState, true);
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
        sel.groupBy(newSQLBuffer(sel, ctx, state));
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state, 
        boolean asc) {
        sel.orderBy(newSQLBuffer(sel, ctx, state), asc, false, getSelectAs());
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
        TrimExpState tstate = (TrimExpState) state;
        _val.calculateValue(sel, ctx, tstate.valueState, null, null);
        _trimChar.calculateValue(sel, ctx, tstate.charState, null, null);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        DBDictionary dict = ctx.store.getDBDictionary();
        String func;
        if (_where == null) {
            func = dict.trimBothFunction;
            dict.assertSupport(func != null, "TrimBothFunction");
        } else if (_where.booleanValue()) {
            func = dict.trimLeadingFunction;
            dict.assertSupport(func != null, "TrimLeadingFunction");
        } else {
            func = dict.trimTrailingFunction;
            dict.assertSupport(func != null, "TrimTrailingFunction");
        }        
        func = dict.getCastFunction(_val, func);
        
        int fromPart = func.indexOf("{0}");
        int charPart = func.indexOf("{1}");
        if (charPart == -1)
            charPart = func.length();
        String part1 = func.substring(0, Math.min(fromPart, charPart));
        String part2 = func.substring(Math.min(fromPart, charPart) + 3,
            Math.max(fromPart, charPart));
        String part3 = null;
        if (charPart != func.length())
            part3 = func.substring(Math.max(fromPart, charPart) + 3);

        TrimExpState tstate = (TrimExpState) state;
        sql.append(part1);
        if (fromPart < charPart)
            _val.appendTo(sel, ctx, tstate.valueState, sql, 0);
        else 
            _trimChar.appendTo(sel, ctx, tstate.charState, sql, 0);
        sql.append(part2);

        if (charPart != func.length()) {
            if (fromPart > charPart)
                _val.appendTo(sel, ctx, tstate.valueState, sql, 0);
            else
                _trimChar.appendTo(sel, ctx, tstate.charState, sql, 0);
            sql.append(part3);
        } else {
            // since the trim statement did not specify the token for
            // where to specify the trim char (denoted by "{1}"),
            // we do not have the ability to trim off non-whitespace
            // characters; throw an exception when we attempt to do so
            if (!(_trimChar instanceof Const) || String.valueOf(((Const) 
                _trimChar).getValue(ctx,tstate.charState)).trim().length() != 0)
                dict.assertSupport(false, "TrimNonWhitespaceCharacters");
        }
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        _trimChar.acceptVisit(visitor);
        visitor.exit(this);
    }

    public int getId() {
        return Val.TRIM_VAL;
    }
}

