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
 * An expression that OR's two others together.
 *
 * @author Abe White
 */
class OrExpression
    extends Exp {

    private final Exp _exp1;
    private final Exp _exp2;

    /**
     * Constructor. Supply expressions to combine.
     */
    public OrExpression(Exp exp1, Exp exp2) {
        _exp1 = exp1;
        _exp2 = exp2;
    }

    protected boolean eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        return _exp1.evaluate(candidate, orig, ctx, params)
            || _exp2.evaluate(candidate, orig, ctx, params);
    }

    protected boolean eval(Collection candidates, StoreContext ctx,
        Object[] params) {
        return _exp1.evaluate(candidates, ctx, params)
            || _exp2.evaluate(candidates, ctx, params);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _exp1.acceptVisit(visitor);
        _exp2.acceptVisit(visitor);
        visitor.exit(this);
    }
}

