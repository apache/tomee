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
package org.apache.openjpa.kernel.exps;

import java.util.Collection;
import java.util.Iterator;

import org.apache.openjpa.kernel.StoreContext;

/**
 * Any contains(var) expression must be followed by at least one
 * AND clause using the variable 'var'. This expression type represents
 * one of those and AND clauses. It is responsible for evaluating the
 * right subexpression for every possible value of the variable.
 *
 * @author Abe White
 */
class BindVariableAndExpression
    extends AndExpression {

    /**
     * Constructor. Provide expression binding the variable and the
     * expression it is AND'd with.
     */
    public BindVariableAndExpression(BindVariableExpression var, Exp exp) {
        super(var, exp);
    }

    protected boolean eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        // execute the tree for every element in the variable's collection;
        // the variable is aliased to the current element before each
        // iteration so that variable paths within the tree can
        // use the current value; the expression is true if true for any
        // value of the collection

        // if the collection is empty it cannot contain any variable
        BindVariableExpression bind = (BindVariableExpression)
            getExpression1();
        Collection vals = bind.getVariableValues(candidate, orig, ctx,
            params);
        if (vals == null || vals.isEmpty())
            return false;

        // the subtree is true if true for any variable in the collection
        BoundVariable var = bind.getVariable();
        for (Iterator itr = vals.iterator(); itr.hasNext();) {
            if (!var.setValue(itr.next()))
                continue;
            if (getExpression2().evaluate(candidate, orig, ctx, params))
                return true;
        }
        return false;
    }

    protected boolean eval(Collection candidates, StoreContext ctx,
        Object[] params) {
        if (candidates == null || candidates.isEmpty())
            return false;
        Object obj = candidates.iterator().next();
        return eval(obj, obj, ctx, params);
    }
}
