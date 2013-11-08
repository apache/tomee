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
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Aggregate listener that evaluates to a value.
 *
 * @author Abe White
 */
class Aggregate
    extends AbstractVal {

    private final JDBCAggregateListener _listener;
    private final Val _arg;
    private final ClassMapping _candidate;
    private ClassMetaData _meta = null;
    private Class _cast = null;

    /**
     * Constructor.
     */
    public Aggregate(JDBCAggregateListener listener, Val arg,
        ClassMapping candidate) {
        _listener = listener;
        _arg = arg;
        _candidate = candidate;
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public void setMetaData(ClassMetaData meta) {
        _meta = meta;
    }

    public boolean isAggregate() {
        return true;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        return _listener.getType(getArgTypes());
    }

    private Class[] getArgTypes() {
        if (_arg == null)
            return null;
        if (_arg instanceof Args)
            return ((Args) _arg).getTypes();
        return new Class[]{ _arg.getType() };
    }

    public void setImplicitType(Class type) {
        _cast = type;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        if (_arg == null)
            return ExpState.NULL;

        // note that we tell targets and args to extensions that are sql
        // paths to go ahead and join to their related object (if any),
        // because we assume that, unlike most operations, if a relation
        // field like a 1-1 is given as the target of an extension, then
        // the extension probably acts on some field or column in the
        // related object, not the 1-1 field itself
        return _arg.initialize(sel, ctx, JOIN_REL);
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        sel.select(newSQLBuffer(sel, ctx, state), this);
        sel.setAggregate(true);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        if (_arg != null)
            _arg.selectColumns(sel, ctx, state, true);
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
        sel.groupBy(newSQLBuffer(sel, ctx, state));
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state, 
        boolean asc) {
        sel.orderBy(newSQLBuffer(sel, ctx, state), asc, false,
            getSelectAs());
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
        if (_arg != null)
            _arg.calculateValue(sel, ctx, state, null, null);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        _listener.appendTo(sql, getArgs(sel, ctx, state), _candidate, 
            ctx.store);
        sel.append(sql, state.joins);
    }

    private FilterValue[] getArgs(Select sel, ExpContext ctx, ExpState state) {
        if (_arg == null)
            return null;
        if (_arg instanceof Args)
            return ((Args) _arg).newFilterValues(sel, ctx, state);
        return new FilterValue[]{
            new FilterValueImpl(sel, ctx, state, _arg)
        };
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        if (_arg != null)
            _arg.acceptVisit(visitor);
        visitor.exit(this);
    }
}
