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
import org.apache.openjpa.lib.util.Localizer;

/**
 * An in-memory representation of a general case expression
 *
 * @author Catalina Wei
 */
class GeneralCase
    extends Val {

    private static final Localizer _loc = Localizer.forPackage(
        GeneralCase.class);

    private final Exp[] _exp;
    private final Val _val;

    public GeneralCase(Exp[] exp, Val val) {
        _exp = exp;
        _val = val;
    }

    protected Object eval(Object candidate, Object orig, StoreContext ctx,
        Object[] params) {
        for (int i = 0; i < _exp.length; i++) {
            boolean compare = ((WhenCondition) _exp[i]).getExp().
                eval(candidate, orig, ctx, params);
            
            if (compare)
                return ((WhenCondition) _exp[i]).getVal().
                    eval(candidate, orig, ctx, params);
            else
                continue;
        }
        return _val.eval(candidate, orig, ctx, params);
    }

    protected Object eval(Object candidate,StoreContext ctx,
        Object[] params) {

        for (int i = 0; i < _exp.length; i++) {
            boolean compare = ((WhenCondition) _exp[i]).getExp().
                eval(candidate, null, ctx, params);
                
            if (compare)
                return ((WhenCondition) _exp[i]).getVal().
                    eval(candidate, null, ctx, params);
            else
                continue;
        }
        return _val.eval(candidate, null, ctx, params);
    }

    public Class getType() {
        Class c1 = _val.getType();
        for (int i = 0; i < _exp.length; i++) {
            Class c2 = ((WhenCondition) _exp[i]).getVal().getType();
            c1 = Filters.promote(c1, c2);
        }
        return c1;
    }

    public void setImplicitType(Class type) {
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        for (int i = 0; i < _exp.length; i++)
            _exp[i].acceptVisit(visitor);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}
