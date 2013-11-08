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

import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Negates a contains expression using a subselect to make sure no
 * elements meet the criteria.
 *
 * @author Abe White
 */
class NotContainsExpression
    implements Exp {

    private final Exp _exp;

    /**
     * Constructor. Supply the expression to negate.
     */
    public NotContainsExpression(Exp exp) {
        _exp = exp;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        return new NotContainsExpState(contains);
    }

    /**
     * Expression state.
     */
    private static class NotContainsExpState
        extends ExpState {
        
        public final Map contains;

        public NotContainsExpState(Map contains) {
            this.contains = contains;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        DBDictionary dict = ctx.store.getDBDictionary();
        dict.assertSupport(dict.supportsSubselect, "SupportsSubselect");

        Select sub = ctx.store.getSQLFactory().newSelect();
        sub.setParent(sel, null);
        // this subselect has the same context as its parent
        sub.setContext(sel.ctx());
        // the context select should still belong to parent
        sub.ctx().setSelect(sel);
        ExpState estate = _exp.initialize(sub, ctx, ((NotContainsExpState) 
            state).contains);
        sub.where(sub.and(null, estate.joins));

        SQLBuffer where = new SQLBuffer(dict).append("(");
        _exp.appendTo(sub, ctx, estate, where);
        if (where.getSQL().length() > 1)
            sub.where(where.append(")"));

        buf.append("0 = ");
        buf.appendCount(sub, ctx.fetch);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        ExpState estate = _exp.initialize(sel, ctx, ((NotContainsExpState) 
            state).contains);
        _exp.selectColumns(sel, ctx, estate, true);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _exp.acceptVisit(visitor);
        visitor.exit(this);
    }
}
