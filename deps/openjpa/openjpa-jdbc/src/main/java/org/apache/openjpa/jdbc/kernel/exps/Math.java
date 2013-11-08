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
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Value produced by a mathematical operation on two values.
 *
 * @author Abe White
 */
public class Math
    extends AbstractVal {

    public static final String ADD = "+";
    public static final String SUBTRACT = "-";
    public static final String MULTIPLY = "*";
    public static final String DIVIDE = "/";
    public static final String MOD = "MOD";

    private final Val _val1;
    private final Val _val2;
    private final String _op;
    private ClassMetaData _meta = null;
    private Class _cast = null;

    /**
     * Constructor. Provide the values to operate on, and the operator.
     */
    public Math(Val val1, Val val2, String op) {
        _val1 = val1;
        _val2 = val2;
        _op = op;
    }

    public Val getVal1() {
        return _val1;
    }

    public Val getVal2() {
        return _val2;
    }

    public String getOperation() {
        return _op;
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public void setMetaData(ClassMetaData meta) {
        _meta = meta;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        Class c1 = _val1.getType();
        Class c2 = _val2.getType();
        return Filters.promote(c1, c2);
    }

    public void setImplicitType(Class type) {
        _cast = type;
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
        return Filters.convert(res.getObject(this, JavaSQLTypes.JDBC_DEFAULT, 
            null), getType());
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val1.calculateValue(sel, ctx, bstate.state1, _val2, bstate.state2);
        _val2.calculateValue(sel, ctx, bstate.state2, _val1, bstate.state1);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        ctx.store.getDBDictionary().mathFunction(sql, _op,
            new FilterValueImpl(sel, ctx, bstate.state1, _val1),
            new FilterValueImpl(sel, ctx, bstate.state2, _val2));
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val1.acceptVisit(visitor);
        _val2.acceptVisit(visitor);
        visitor.exit(this);
    }

    public int getId() {
        return Val.MATH_VAL;
    }
}

