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
 * An in-memory representation of a coalesce expression
 *
 * @author Catalina Wei
 */
public class Coalesce 
    extends Val {

    private final Val[] _val;

    /**
     * Constructor.
     */
    Coalesce(Val[] val) {
        _val = val;
    }

    @Override
    protected Object eval(Object candidate, Object orig, StoreContext ctx,
        Object[] params) {
        for (int i = 0; i < _val.length-1; i++) {
            Object o1 = _val[i].eval(candidate, orig, ctx, params);
            if (o1 != null)
               return o1;
           else
               continue;
        }
        return _val[_val.length-1].eval(candidate, orig, ctx, params);
    }

    protected Object eval(Object candidate, StoreContext ctx,
        Object[] params) {
        for (int i = 0; i < _val.length-1; i++) {
            Object o1 = _val[i].eval(candidate, null, ctx, params);
            if (o1 != null)
               return o1;
           else
               continue;
        }
        return _val[_val.length-1].eval(candidate, null, ctx, params);
    }

    /**
     * Compare the two values.
     */
    protected boolean compare(Object o1, Object o2) {
        return (o1 == null && o2 == null)
            || (o1 != null && o1.equals(o2));
    }

    public Val[] getVal() {
        return _val;
    }

    public Class getType() {
        Class c1 = _val[0].getType();
        for (int i = 1; i < _val.length; i++) {
            Class c2 = _val[i].getType();
            c1 = Filters.promote(c1, c2);
        }
        return c1;
    }

    public void setImplicitType(Class type) {       
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        for (int i = 0; i < _val.length; i++)
            _val[i].acceptVisit(visitor);
        visitor.exit(this);
    }
}
