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
 * Take the substring of a string.
 *
 * @author Abe White
 */
class Substring
    extends Val {

    private final Val _val;
    private final Val _args;

    /**
     * Constructor. Provide value to take substring of and arguments to
     * substring method.
     */
    public Substring(Val val, Val args) {
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
        Object arg = _args.eval(candidate, orig, ctx, params);
        if (arg instanceof Object[]) {
            Object[] args = (Object[]) arg;
            int start = ((Number) args[0]).intValue() - 1;
            int length = ((Number) args[1]).intValue();
            String string = str == null ? "" : str.toString();
            return string.substring(start, Math.min(start + length, string.length()));
        }
        return str.toString().substring(((Number) arg).intValue() - 1);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        _args.acceptVisit(visitor);
        visitor.exit(this);
    }
}
