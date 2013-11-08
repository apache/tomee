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
 * Value produced by a unary operation on a value.
 *
 * @author Abe White
 */
abstract class UnaryOp
    extends AbstractVal {

    private final Val _val;
    private ClassMetaData _meta = null;
    private Class _cast = null;
    private boolean _noParen = false;

    /**
     * Constructor. Provide the value to operate on.
     */
    public UnaryOp(Val val) {
        _val = val;
    }
    
    public UnaryOp(Val val, boolean noParen) {
        _val = val;
        _noParen = noParen;
    }

    public Val getValue() {
        return _val;
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
        return getType(_val.getType());
    }

    public void setImplicitType(Class type) {
        _cast = type;
    }
    
    public boolean getNoParen() {
        return _noParen;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        return initializeValue(sel, ctx, flags);
    }

    protected ExpState initializeValue(Select sel, ExpContext ctx, int flags) {
        return _val.initialize(sel, ctx, flags);
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        sel.select(newSQLBuffer(sel, ctx, state), this);
        if (isAggregate())
            sel.setAggregate(true);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        _val.selectColumns(sel, ctx, state, true);
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
        Object value = res.getObject(this, JavaSQLTypes.JDBC_DEFAULT, null);
        Class<?> type = getType();
        if (value == null) {
            if (nullableValue(ctx, state)) {  // OPENJPA-1794
                return null;
            }
            else if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
                value = Filters.getDefaultForNull(Filters.wrap(type));
            }
        }
        return Filters.convert(value, type);
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        _val.calculateValue(sel, ctx, state, null, null);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        sql.append(getOperator());
        sql.append(_noParen ? " " : "(");
        _val.appendTo(sel, ctx, state, sql, 0);
       
        // OPENJPA-2149: If _val (Val) is an 'Arg', we need to get the Val[]
        // from it, and the single element it contains because the
        // 'addCastForParam' method gets the 'type' from the Val it receives.
        // In the case where _val is an Arg, when addCastForParam gets the
        // type, it will be getting the type of the Val (an Object) rather
        // the type of the Arg.
        sql.addCastForParam(getOperator(),
            (_val instanceof Args) ? (((Args) _val).getVals())[0]
                                   : _val);
        if (!_noParen)
            sql.append(")");
    }

    /**
     * Return the type of this value based on the argument type. Returns
     * the argument type by default.
     */
    protected Class getType(Class c) {
        return c;
    }

    /**
     * Return the name of this operator.
     */
    protected abstract String getOperator();

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
    
    // OPENJPA-1794
    protected boolean nullableValue(ExpContext ctx, ExpState state) {
        return false;
    }

}
