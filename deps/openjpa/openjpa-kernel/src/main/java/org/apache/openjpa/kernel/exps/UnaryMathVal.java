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

import org.apache.openjpa.kernel.StoreContext;

/**
 * Value produced by a mathematical operation on one value.
 *
 * @author Abe White
 */
abstract class UnaryMathVal
    extends Val {

    private final Val _val;

    /**
     * Constructor. Provide the value to operate on.
     */
    public UnaryMathVal(Val val) {
        _val = val;
    }

    public Class getType() {
        return getType(_val.getType());
    }

    public void setImplicitType(Class type) {
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        Object o1 = _val.eval(candidate, orig, ctx, params);
        return operate(o1, _val.getType());
    }

    /**
     * Return the type of this value based on the numeric type being operated
     * on.
     */
    protected abstract Class getType(Class c);

    /**
     * Return the result of this mathematical operation on the given value.
     */
    protected abstract Object operate(Object o, Class c);

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}
