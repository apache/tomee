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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.Arguments;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * A list of arguments to a multi-argument function.
 *
 * @author Abe White
 */
public class Args
    extends AbstractVal
    implements Arguments {

    private final Val[] _args;
    private ClassMetaData _meta = null;

    /**
     * Constructor. Supply values being combined.
     */
    public Args(Val val1, Val val2) {
        this(new Val[]{val1, val2});
    }
    
    public Args (Val... values) {
        List<Val> list = new ArrayList<Val>();
        if (values != null) {
            for (Val v : values) {
                if (v instanceof Args) {
                    list.addAll(Arrays.asList(((Args)v)._args));
                } else {
                    list.add(v);
                }
            }
        }
        _args = list.toArray(new Val[list.size()]);
    }

    /**
     * Return a filter value for each argument.
     */
    public FilterValue[] newFilterValues(Select sel, ExpContext ctx, 
        ExpState state) {
        ArgsExpState astate = (ArgsExpState) state; 
        FilterValue[] filts = new FilterValue[_args.length];
        for (int i = 0; i < _args.length; i++)
            filts[i] = new FilterValueImpl(sel, ctx, astate.states[i], 
                _args[i]); 
        return filts;
    }

    public Value[] getValues() {
        return _args;
    }

    public Val[] getVals() {
        return _args;
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
        return Object[].class;
    }

    public Class[] getTypes() {
        Class[] c = new Class[_args.length];
        for (int i = 0; i < _args.length; i++)
            c[i] = _args[i].getType();
        return c;
    }

    public void setImplicitType(Class type) {
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        ExpState[] states = new ExpState[_args.length];
        Joins joins = null;
        for (int i = 0; i < _args.length; i++) {
            states[i] = _args[i].initialize(sel, ctx, flags);
            if (joins == null)
                joins = states[i].joins;
            else
                joins = sel.and(joins, states[i].joins);
        }
        return new ArgsExpState(joins, states);
    }

    /**
     * Expression state.
     */
    private static class ArgsExpState
        extends ExpState {
        
        public ExpState[] states;

        public ArgsExpState(Joins joins, ExpState[] states) {
            super(joins);
            this.states = states;
        }
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        ArgsExpState astate = (ArgsExpState) state;
        for (int i = 0; i < _args.length; i++)
            _args[i].selectColumns(sel, ctx, astate.states[i], pks);
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state, 
        boolean asc) {
    }

    public Object load(ExpContext ctx, ExpState state, Result res) {
        return null;
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        ArgsExpState astate = (ArgsExpState) state;
        for (int i = 0; i < _args.length; i++)
            _args[i].calculateValue(sel, ctx, astate.states[i], null, null);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 0;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, SQLBuffer sql, int index) {
        appendTo(sel, ctx, state, sql, index, null);
    }
    
    public void appendTo(Select sel, ExpContext ctx, ExpState state, SQLBuffer sql, int index, String operator) {
        ArgsExpState astate = (ArgsExpState) state;
        for (int i = 0; i < _args.length; i++) {
            _args[i].appendTo(sel, ctx, astate.states[i], sql, index);
            if( operator != null ) {
                sql.addCastForParam(operator, _args[i]);
            }
            if (i < _args.length-1)
                sql.append(", ");
        }
    }

    public void appendIsEmpty(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
    }

    public void appendIsNotEmpty(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql){
    }

    public void appendSize(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
    }

    public void appendIsNull(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
    }

    public void appendIsNotNull(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        for (int i = 0; i < _args.length; i++)
            _args[i].acceptVisit(visitor);
        visitor.exit(this);
    }

    public int getId() {
        return Val.ARGS_VAL;
    }
}
