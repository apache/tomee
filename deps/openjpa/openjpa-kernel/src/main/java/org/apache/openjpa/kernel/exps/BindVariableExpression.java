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

import org.apache.openjpa.kernel.StoreContext;

/**
 * Binds a variable to a collection.
 *
 * @author Abe White
 */
class BindVariableExpression
    extends Exp {

    private final BoundVariable _var;
    private final Val _val;

    /**
     * Constructor.
     *
     * @param var the bound variable
     * @param val the value the variable is bound to
     */
    public BindVariableExpression(BoundVariable var, Val val) {
        _var = var;
        _val = val;
    }

    public BoundVariable getVariable() {
        return _var;
    }

    /**
     * Return the possible values that variable can take.
     */
    public Collection getVariableValues(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        Object values = _val.eval(candidate, orig, ctx, params);
        return getCollection(values);
    }

    /**
     * Return a collection for the given object, produced by
     * <code>eval</code>'ing the value of this expression. Simply casts
     * the object to a collection by default.
     */
    protected Collection getCollection(Object values) {
        return (Collection) values;
    }

    protected boolean eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        // if the collection is empty no possible variable evals to true
        Collection vals = getVariableValues(candidate, orig, ctx, params);
        if (vals == null || vals.isEmpty())
            return false;
        return true;
    }

    protected boolean eval(Collection candidates, StoreContext ctx,
        Object[] params) {
        if (candidates == null || candidates.isEmpty())
            return false;
        Object obj = candidates.iterator().next();
        return eval(obj, obj, ctx, params);
	}

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _var.acceptVisit(visitor);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}

