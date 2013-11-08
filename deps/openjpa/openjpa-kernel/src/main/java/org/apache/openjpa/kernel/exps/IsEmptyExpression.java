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
import java.util.Map;

import org.apache.openjpa.kernel.StoreContext;

/**
 * Expression to test for an empty Collection.
 *
 * @author Abe White
 */
class IsEmptyExpression
    extends Exp {

    private final Val _val;

    /**
     * Constructor. Provide collection/map value to test.
     */
    public IsEmptyExpression(Val val) {
        _val = val;
    }

    protected boolean eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        Object obj = _val.eval(candidate, orig, ctx, params);
        if (obj == null)
            return true;
        if (obj instanceof Collection)
            return ((Collection) obj).isEmpty();
        if (obj instanceof Map)
            return ((Map) obj).isEmpty();
        return false;
    }

    protected boolean eval(Collection candidates, StoreContext ctx,
        Object[] params) {
        Collection c = _val.eval(candidates, null, ctx, params);
        if (c == null || c.isEmpty())
            return false;
        Object obj = c.iterator().next();
        if (obj == null)
            return true;
        if (obj instanceof Collection)
            return ((Collection) obj).isEmpty();
        if (obj instanceof Map)
            return ((Map) obj).isEmpty();
        return false;
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}

