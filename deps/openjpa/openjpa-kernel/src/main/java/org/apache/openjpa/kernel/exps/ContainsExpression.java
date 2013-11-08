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
import java.util.Collections;

import org.apache.openjpa.kernel.StoreContext;

/**
 * Tests that a Collection contains a value.
 *
 * @author Abe White
 */
class ContainsExpression
    extends Exp {

    private final Val _val1;
    private final Val _val2;

    /**
     * Constructor.
     *
     * @param val1 the container value
     * @param val2 the containee to test
     */
    public ContainsExpression(Val val1, Val val2) {
        _val1 = val1;
        _val2 = val2;
    }

    protected boolean eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        Object obj = _val1.eval(candidate, orig, ctx, params);
        Collection coll = getCollection(obj);
        return coll != null && !coll.isEmpty()
            && coll.contains(_val2.eval(candidate, orig, ctx, params));
    }

    protected boolean eval(Collection candidates, StoreContext ctx,
        Object[] params) {
        Collection coll = _val1.eval(candidates, null, ctx, params);
        if (coll == null || coll.isEmpty())
            return false;
        coll = getCollection(coll.iterator().next());
        if (coll == null || coll.isEmpty())
            return false;

        Collection coll2 = _val2.eval(candidates, null, ctx, params);
        if (coll2 == null || coll2.isEmpty())
            return false;
        return coll.contains(coll2.iterator().next());
    }

    /**
     * Return the container collection for the given value.
     */
    protected Collection getCollection(Object obj) {
        return obj instanceof Collection ?
            (Collection) obj : Collections.singleton(obj);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val1.acceptVisit(visitor);
        _val2.acceptVisit(visitor);
        visitor.exit(this);
    }
}

