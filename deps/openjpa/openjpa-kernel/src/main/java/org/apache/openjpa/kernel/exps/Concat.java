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
 * Concatenate two strings together.
 *
 * @author Marc Prud'hommeaux
 */
class Concat
    extends Val {

    private final Val _val;
    private final Val _args;

    /**
     * Constructor. Provide target string and the arguments to the
     * indexOf method.
     */
    public Concat(Val val, Val args) {
        _val = val;
        _args = args;
    }

    public Class getType() {
        return String.class;
    }

    public void setImplicitType(Class type) {
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        Object str = _val.eval(candidate, orig, ctx, params);
        StringBuilder cat = new StringBuilder(str.toString());

        Object arg = _args.eval(candidate, orig, ctx, params);
        if (arg instanceof Object[]) {
            for (int i = 0; i < ((Object[]) arg).length; i++)
                cat.append((((Object[]) arg)[i]).toString());
        } else
            cat.append(arg.toString());

        return cat.toString();
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        _args.acceptVisit(visitor);
        visitor.exit(this);
    }
}

