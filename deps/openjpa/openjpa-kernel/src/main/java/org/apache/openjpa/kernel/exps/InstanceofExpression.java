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

import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.StoreContext;

/**
 * Tests whether a value is an instance of a class.
 *
 * @author Abe White
 */
class InstanceofExpression
    extends Exp {

    private final Val _val;
    private final Class _cls;

    /**
     * Constructor; supply value and class.
     */
    public InstanceofExpression(Val val, Class cls) {
        _val = val;
        _cls = Filters.wrap(cls);
    }

    /**
     * Evaluate the expression for the given context candidate and original
     * candidate.
     */
    protected boolean eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        return _cls.isInstance(_val.eval(candidate, orig, ctx, params));
    }

    /**
     * Evaluate the expression for the given group.
     */
    protected boolean eval(Collection candidates, StoreContext ctx,
        Object[] params) {
        Collection c = _val.eval(candidates, null, ctx, params);
        Object o = (c == null || c.isEmpty()) ? null : c.iterator().next();
        return _cls.isInstance(o);
	}

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}
