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
 * Expression that compares two others.
 *
 * @author Abe White
 */
abstract class CompareExpression
    extends Exp {

    private final Val _val1;
    private final Val _val2;

    /**
     * Constructor. Supply values to compare.
     */
    public CompareExpression(Val val1, Val val2) {
        _val1 = val1;
        _val2 = val2;
    }

    protected boolean eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        Object o1 = _val1.eval(candidate, orig, ctx, params);
        Object o2 = _val2.eval(candidate, orig, ctx, params);
        if (o1 != null && o2 != null) {
            Class c = Filters.promote(o1.getClass(), o2.getClass());
            o1 = Filters.convert(o1, c);
            o2 = Filters.convert(o2, c);
        }
        return compare(o1, o2);
    }

    protected boolean eval(Collection candidates, StoreContext ctx,
        Object[] params) {
        Collection c1 = _val1.eval(candidates, null, ctx, params);
        Collection c2 = _val2.eval(candidates, null, ctx, params);
        Object o1 = (c1 == null || c1.isEmpty()) ? null
            : c1.iterator().next();
        Object o2 = (c2 == null || c2.isEmpty()) ? null
            : c2.iterator().next();

        if (o1 != null && o2 != null) {
            Class c = Filters.promote(o1.getClass(), o2.getClass());
            o1 = Filters.convert(o1, c);
            o2 = Filters.convert(o2, c);
        }
        return compare(o1, o2);
    }

    /**
     * Compare the two values.
     */
    protected abstract boolean compare(Object o1, Object o2);

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val1.acceptVisit(visitor);
        _val2.acceptVisit(visitor);
        visitor.exit(this);
    }
}
