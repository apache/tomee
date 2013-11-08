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
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Concatenate one string with another.
 *
 * @author Marc Prud'hommeaux
 */
public class Concat
    extends AbstractVal {

    private final Val _val1;
    private final Val _val2;
    private ClassMetaData _meta = null;

    /**
     * Constructor. Provide the strings to operate on.
     */
    public Concat(Val val1, Val val2) {
        _val1 = val1;
        _val2 = val2;
    }

    public Val getVal1() {
        return _val1;
    }

    public Val getVal2() {
        return _val2;
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
        ExpState s1 = _val1.initialize(sel, ctx, 0);
        ExpState s2 = _val2.initialize(sel, ctx, 0);
        return new BinaryOpExpState(sel.and(s1.joins, s2.joins), s1, s2);
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        sel.select(newSQLBuffer(sel, ctx, state), this);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val1.selectColumns(sel, ctx, bstate.state1, true);
        _val2.selectColumns(sel, ctx, bstate.state2, true);
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
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val1.calculateValue(sel, ctx, bstate.state1, null, null);
        _val2.calculateValue(sel, ctx, bstate.state2, null, null);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val1.calculateValue(sel, ctx, bstate.state1, null, null);
        _val2.calculateValue(sel, ctx, bstate.state2, null, null);

        DBDictionary dict = ctx.store.getDBDictionary();
        String func = dict.concatenateFunction;
        dict.assertSupport(func != null, "ConcatenateFunction");
        int part1idx = func.indexOf("{0}");
        int part2idx = func.indexOf("{1}");
        String part1 = func.substring(0, Math.min(part1idx, part2idx));
        String part2 = func.substring(Math.min(part1idx, part2idx) + 3,
            Math.max(part1idx, part2idx));
        String part3 = func.substring(Math.max(part1idx, part2idx) + 3);

        sql.append(part1);
        _val1.appendTo(sel, ctx, bstate.state1, sql, 0);
        sql.append(part2);
        _val2.appendTo(sel, ctx, bstate.state2, sql, 0);
        sql.append(part3);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val1.acceptVisit(visitor);
        _val2.acceptVisit(visitor);
        visitor.exit(this);
    }

    public int getId() {
        return Val.CONCAT_VAL;
    }
}

