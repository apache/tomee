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
import java.util.HashSet;

import org.apache.openjpa.kernel.StoreContext;

/**
 * A distinct set of the specified values.
 *
 * @author Marc Prud'hommeaux
 */
class Distinct
    extends Val {

    private final Val _val;

    /**
     * Constructor. Supply value to aggregate.
     */
    public Distinct(Val val) {
        _val = val;
    }

    public Class getType() {
        return Collection.class;
    }

    public void setImplicitType(Class type) {
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        if (candidate == null)
            candidate = Collections.EMPTY_LIST;
        Collection arg = candidate instanceof Collection
            ? (Collection) candidate : Collections.singleton(candidate);
        return eval(arg, orig, ctx, params).iterator().next();
    }

    protected Collection eval(Collection candidates, Object orig,
        StoreContext ctx, Object[] params) {
        Collection args = _val.eval(candidates, orig, ctx, params);
        return new HashSet(args);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}

