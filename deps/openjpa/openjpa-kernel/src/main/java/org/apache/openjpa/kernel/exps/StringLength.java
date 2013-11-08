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
 * Returns the number of characters in the String.
 *
 * @author Marc Prud'hommeaux
 */
class StringLength
    extends Val {

    private final Val _val;
    private Class _cast = null;

    /**
     * Constructor. Provide value to upper-case.
     */
    public StringLength(Val val) {
        _val = val;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        return int.class;
    }

    public void setImplicitType(Class type) {
        _cast = type;
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        Object eval = _val.eval(candidate, orig, ctx, params);
        if (eval == null)
            return 0;

        return eval.toString().length();
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}

