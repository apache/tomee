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
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * An aggregate of some value.
 *
 * @author Abe White
 */
abstract class AggregateVal
    extends Val {

    private static final Localizer _loc = Localizer.forPackage
        (AggregateVal.class);

    private final Val _val;

    /**
     * Constructor. Supply value to aggregate.
     */
    public AggregateVal(Val val) {
        _val = val;
    }

    public boolean isAggregate() {
        return true;
    }

    public Class getType() {
        return getType(_val.getType());
    }

    public void setImplicitType(Class type) {
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        if (candidate == null)
            candidate = Collections.EMPTY_LIST;

        // allow aggregates to be used in filter expressions so long as a
        // collection is passed in
        if (candidate instanceof Collection)
            return eval((Collection) candidate, orig, ctx, params).
                iterator().next();
        throw new UserException(_loc.get("agg-in-filter"));
    }

    protected Collection eval(Collection candidates, Object orig,
        StoreContext ctx, Object[] params) {
        Collection args = _val.eval(candidates, orig, ctx, params);
        return Collections.singleton(operate(args, _val.getType()));
    }

    /**
     * Return the type of this aggregate based on the value type.
     */
    protected abstract Class getType(Class c);

    /**
     * Aggregate the given values.
     */
    protected abstract Object operate(Collection os, Class c);

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}
