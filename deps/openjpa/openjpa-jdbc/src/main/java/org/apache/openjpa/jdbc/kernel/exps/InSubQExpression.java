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

import java.util.Map;

import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Tests whether a value is IN a subquery.
 *
 * @author Abe White
 */
class InSubQExpression
    implements Exp {

    private final Val _val;
    private final SubQ _sub;

    /**
     * Constructor. Supply the value to test and the subquery.
     */
    public InSubQExpression(Val val, SubQ sub) {
        _val = val;
        _sub = sub;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        ExpState subqState = _sub.initialize(sel, ctx, 0);
        ExpState valueState = _val.initialize(sel, ctx, 0);
        return new InSubQExpState(valueState.joins, subqState, valueState);
    }

    /**
     * Expression state.
     */
    private static class InSubQExpState
        extends ExpState {

        public final ExpState subqState;
        public final ExpState valueState;

        public InSubQExpState(Joins joins, ExpState subqState, 
            ExpState valueState) {
            super(joins);
            this.subqState = subqState;
            this.valueState = valueState;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        InSubQExpState istate = (InSubQExpState) state;
        _sub.calculateValue(sel, ctx, istate.subqState, null, null);
        _val.calculateValue(sel, ctx, istate.valueState, null, null);
        _val.appendTo(sel, ctx, istate.valueState, buf, 0);
        buf.append(" IN ");
        _sub.appendTo(sel, ctx, istate.valueState, buf, 0);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        InSubQExpState istate = (InSubQExpState) state;
        _sub.selectColumns(sel, ctx, istate.subqState, pks);
        _val.selectColumns(sel, ctx, istate.valueState, true);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        _sub.acceptVisit(visitor);
        visitor.exit(this);
    }
}
