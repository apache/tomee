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

import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.StoreContext;

/**
 * An in-memory representation of a nullif expression
 *
 * @author Catalina Wei
 */
public class NullIf 
    extends Val {

    private final Val _val1;
    private final Val _val2;

    /**
     * Constructor.
     */
    NullIf(Val val1, Val val2) {
        _val1 = val1;
        _val2 = val2;
    }

    @Override
    protected Object eval(Object candidate, Object orig, StoreContext ctx,
        Object[] params) {
        Object o1 = _val1.eval(candidate, orig, ctx, params);
        Object o2 = _val2.eval(candidate, orig, ctx, params);
        if (o1 != null && o2 != null) {
            Class c = Filters.promote(o1.getClass(), o2.getClass());
            o1 = Filters.convert(o1, c);
            o2 = Filters.convert(o2, c);
        }
        if (compare(o1, o2))
            return null;
        else
            return o1;        
    }

    protected Object eval(Object candidate, StoreContext ctx,
            Object[] params) {
        Object o1 = _val1.eval(candidate, null, ctx, params);
        Object o2 = _val2.eval(candidate, null, ctx, params);
        if (o1 != null && o2 != null) {
            Class c = Filters.promote(o1.getClass(), o2.getClass());
            o1 = Filters.convert(o1, c);
            o2 = Filters.convert(o2, c);
        }
        if (compare(o1, o2))
            return null;
        else
            return o1;        
    }

    /**
     * Compare the two values.
     */
    protected boolean compare(Object o1, Object o2) {
        return (o1 == null && o2 == null)
            || (o1 != null && o1.equals(o2));
    }

    public Val getVal1() {
        return _val1;
    }

    public Val getVal2() {
        return _val2;
    }

    public Class getType() {
        return _val1.getType();
    }

    public void setImplicitType(Class type) {       
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val1.acceptVisit(visitor);
        _val2.acceptVisit(visitor);
        visitor.exit(this);
    }
}
