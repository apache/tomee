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
 * Negates an expression.
 *
 * @author Abe White
 */
class NotExpression
    implements Exp {

    private final Exp _exp;

    /**
     * Constructor. Supply the expression to negate.
     */
    public NotExpression(Exp exp) {
        _exp = exp;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) { 
        ExpState state = _exp.initialize(sel, ctx, contains);
        return new NotExpState(sel.or(state.joins, null), state);
    }

    /**
     * Expression state.
     */
    private static class NotExpState 
        extends ExpState {

        public final ExpState state;

        public NotExpState(Joins joins, ExpState state) {
            super(joins);
            this.state = state;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        buf.append("NOT (");
        _exp.appendTo(sel, ctx, ((NotExpState) state).state, buf);
        buf.append(")");
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        _exp.selectColumns(sel, ctx, ((NotExpState) state).state, pks);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _exp.acceptVisit(visitor);
        visitor.exit(this);
    }
}
