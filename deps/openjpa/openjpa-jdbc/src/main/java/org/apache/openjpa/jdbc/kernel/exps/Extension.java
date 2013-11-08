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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Filter listener that evaluates to a value.
 *
 * @author Abe White
 */
class Extension
    extends AbstractVal
    implements Val, Exp {

    private final JDBCFilterListener _listener;
    private final Val _target;
    private final Val _arg;
    private final ClassMapping _candidate;
    private ClassMetaData _meta = null;
    private Class _cast = null;

    /**
     * Constructor.
     */
    public Extension(JDBCFilterListener listener, Val target,
        Val arg, ClassMapping candidate) {
        _listener = listener;
        _target = target;
        _arg = arg;
        _candidate = candidate;
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

    public boolean isAggregate() {
        return false;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        Class targetClass = (_target == null) ? null : _target.getType();
        return _listener.getType(targetClass, getArgTypes());
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
        // note that we tell targets and args to extensions that are sql
        // paths to go ahead and join to their related object (if any),
        // because we assume that, unlike most operations, if a relation
        // field like a 1-1 is given as the target of an extension, then
        // the extension probably acts on some field or column in the
        // related object, not the 1-1 field itself
        ExpState targetState = null;
        ExpState argState = null;
        if (_target != null)
            targetState = _target.initialize(sel, ctx, JOIN_REL);
        if (_arg != null)
            argState = _arg.initialize(sel, ctx, JOIN_REL);
        Joins j1 = (targetState == null) ? null : targetState.joins;
        Joins j2 = (argState == null) ? null : argState.joins;
        return new ExtensionExpState(sel.and(j1, j2), targetState, 
            argState);
    }

    /**
     * Expression state.
     */
    private static class ExtensionExpState
        extends ExpState {

        public final ExpState targetState;
        public final ExpState argState;

        public ExtensionExpState(Joins joins, ExpState targetState,
            ExpState argState) {
            super(joins);
            this.targetState = targetState;
            this.argState = argState;
        }
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        sel.select(newSQLBuffer(sel, ctx, state), this);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        ExtensionExpState estate = (ExtensionExpState) state;
        if (_target != null)
            _target.selectColumns(sel, ctx, estate.targetState, true);
        if (_arg != null)
            _arg.selectColumns(sel, ctx, estate.argState, true);
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
        ExtensionExpState estate = (ExtensionExpState) state;
        if (_target != null)
            _target.calculateValue(sel, ctx, estate.targetState, null, null);
        if (_arg != null)
            _arg.calculateValue(sel, ctx, estate.argState, null, null);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 1;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        ExtensionExpState estate = (ExtensionExpState) state;
        FilterValue target = (_target == null) ? null
            : new FilterValueImpl(sel, ctx, estate.targetState, _target);
        _listener.appendTo(sql, target, getArgs(sel, ctx, estate.argState),
            _candidate, ctx.store);
        sel.append(sql, state.joins);
    }

    private FilterValue[] getArgs(Select sel, ExpContext ctx, ExpState state) {
        if (_arg == null)
            return null;
        if (_arg instanceof Args)
            return ((Args) _arg).newFilterValues(sel, ctx, state);
        return new FilterValue[] {
            new FilterValueImpl(sel, ctx, state, _arg)
        };
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter((Exp) this);
        if (_target != null)
            _target.acceptVisit(visitor);
        if (_arg != null)
            _arg.acceptVisit(visitor);
        visitor.exit((Exp) this);
    }

    //////////////////////
    // Exp implementation
    //////////////////////

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        return initialize(sel, ctx, 0);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        calculateValue(sel, ctx, state, null, null);
        appendTo(sel, ctx, state, sql, 0);
        sel.append(sql, state.joins);
    }
}
