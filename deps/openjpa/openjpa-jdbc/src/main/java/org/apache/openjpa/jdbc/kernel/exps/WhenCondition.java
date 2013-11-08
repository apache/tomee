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
 * Value produced by a when_clause of a case expression.
 *
 * @author Catalina Wei
 */
public class WhenCondition
    implements Exp {

    private final Exp _exp;
    private final Val _val;

    /**
     * Constructor.
     */
    public WhenCondition(Exp exp, Val val) {
        _exp = exp;
        _val = val;
    }

    public Exp getExp() {
        return _exp;
    }

    public Val getVal() {
        return _val;
    }

    public Class getType() {
        return _val.getType();
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        ExpState s1 = _exp.initialize(sel, ctx, contains);
        ExpState s2 = _val.initialize(sel, ctx, 0);
        return new BinaryOpExpState(sel.and(s1.joins, s2.joins), s1, s2);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;

        buf.append(" WHEN ");

        _exp.appendTo(sel, ctx, bstate.state1, buf);
        buf.append(" THEN ");
        _val.appendTo(sel, ctx, bstate.state2, buf, 0);

    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _exp.selectColumns(sel, ctx, bstate.state1, pks);
        _val.selectColumns(sel, ctx, bstate.state2, pks);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _exp.acceptVisit(visitor);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }

    public int getId() {
        return Val.WHENCONDITION_VAL;
    }
}

