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

import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Combines two expressions.
 *
 * @author Abe White
 */
class AndExpression
    implements Exp {

    private final Exp _exp1;
    private final Exp _exp2;

    /**
     * Constructor. Supply the expressions to combine.
     */
    public AndExpression(Exp exp1, Exp exp2) {
        _exp1 = exp1;
        _exp2 = exp2;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        ExpState s1 = _exp1.initialize(sel, ctx, contains);
        ExpState s2 = _exp2.initialize(sel, ctx, contains);
        return new BinaryOpExpState(sel.and(s1.joins, s2.joins), s1, s2);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        boolean paren1 = _exp1 instanceof OrExpression;
        boolean paren2 = _exp2 instanceof OrExpression;
        if (paren1)
            buf.append("(");
        _exp1.appendTo(sel, ctx, bstate.state1, buf);
        if (paren1)
            buf.append(")");
        buf.append(" AND ");
        if (paren2)
            buf.append("(");
        _exp2.appendTo(sel, ctx, bstate.state2, buf);
        if (paren2)
            buf.append(")");
        sel.append(buf, state.joins);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _exp1.selectColumns(sel, ctx, bstate.state1, pks);
        _exp2.selectColumns(sel, ctx, bstate.state2, pks);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _exp1.acceptVisit(visitor);
        _exp2.acceptVisit(visitor);
        visitor.exit(this);
    }
}
