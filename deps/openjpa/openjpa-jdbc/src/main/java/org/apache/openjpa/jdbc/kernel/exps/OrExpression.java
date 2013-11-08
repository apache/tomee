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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Combines two expressions.
 *
 * @author Abe White
 */
class OrExpression
    implements Exp {

    private final Exp _exp1;
    private final Exp _exp2;

    /**
     * Constructor. Supply the expressions to combine.
     */
    public OrExpression(Exp exp1, Exp exp2) {
        _exp1 = exp1;
        _exp2 = exp2;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        // when OR'ing expressions each expression gets its own copy of the
        // contains counts, cause it's OK for each to use the same aliases
        Map contains2 = null;
        if (contains != null)
            contains2 = new HashMap(contains);

        ExpState s1 = _exp1.initialize(sel, ctx, contains);
        ExpState s2 = _exp2.initialize(sel, ctx, contains2);
        ExpState ret = new BinaryOpExpState(sel.or(s1.joins, s2.joins), s1, s2);
        if (contains == null)
            return ret;

        // combine the contains counts from the copy into the main map
        Map.Entry entry;
        Integer val1, val2;
        for (Iterator itr = contains2.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            val2 = (Integer) entry.getValue();
            val1 = (Integer) contains.get(entry.getKey());
            if (val1 == null || val2.intValue() > val1.intValue())
                contains.put(entry.getKey(), val2);
        }
        return ret;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        boolean paren = bstate.joins != null && !bstate.joins.isEmpty();
        if (paren)
            buf.append("(");

        _exp1.appendTo(sel, ctx, bstate.state1, buf);
        buf.append(" OR ");
        _exp2.appendTo(sel, ctx, bstate.state2, buf);

        if (paren)
            buf.append(")");
        sel.append(buf, bstate.joins);
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
