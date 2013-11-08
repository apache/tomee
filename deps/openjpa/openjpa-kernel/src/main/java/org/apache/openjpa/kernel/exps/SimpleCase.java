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
 * An in-memory representation of a simple case expression
 *
 * @author Catalina Wei
 */
public class SimpleCase extends Val {
    private final Val _caseOperand;
    private final Exp[] _exp;
    private final Val _val;

    SimpleCase(Val caseOperand, Exp[] exp, Val val) {
        _caseOperand = caseOperand;
        _exp = exp;
        _val = val;
    }

    @Override
    protected Object eval(Object candidate, Object orig, StoreContext ctx,
        Object[] params) {
        Object o1 = _caseOperand.eval(candidate, orig, ctx, params);
        for (int i = 0; i < _exp.length; i++) {
            Object o2 = ((WhenScalar) _exp[i]).getVal1().
                eval(candidate, orig, ctx, params);
            if (o1 != null && o2 != null) {
                Class c = Filters.promote(o1.getClass(), o2.getClass());
                o1 = Filters.convert(o1, c);
                o2 = Filters.convert(o2, c);
            }
            if (compare(o1, o2))
                return ((WhenScalar) _exp[i]).getVal2().
                    eval(candidate, orig, ctx, params);
            else
                continue;
        }
        return _val.eval(candidate, orig, ctx, params);
    }

    protected Object eval(Object candidate,StoreContext ctx,
            Object[] params) {
        Object o1 = _caseOperand.eval(candidate, null, ctx, params);
        for (int i = 0; i < _exp.length; i++) {
            Object o2 = ((WhenScalar) _exp[i]).getVal1().
                eval(candidate, null, ctx, params);
            if (o1 != null && o2 != null) {
                Class c = Filters.promote(o1.getClass(), o2.getClass());
                o1 = Filters.convert(o1, c);
                o2 = Filters.convert(o2, c);
            }
            if (compare(o1, o2))
                return ((WhenScalar) _exp[i]).getVal2().
                    eval(candidate, null, ctx, params);
            else
                continue;
        }
        return _val.eval(candidate, null, ctx, params);
    }

    /**
     * Compare the two values.
     */
    protected boolean compare(Object o1, Object o2) {
        return (o1 == null && o2 == null)
            || (o1 != null && o1.equals(o2));
    }

    public Class getType() {
        Class c1 = _val.getType();
        for (int i = 0; i < _exp.length; i++) {
            Class c2 = ((WhenScalar) _exp[i]).getVal1().getType();
            c1 = Filters.promote(c1, c2);
        }
        return c1;
    }

    public void setImplicitType(Class type) {       
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _caseOperand.acceptVisit(visitor);
        for (int i = 0; i < _exp.length; i++)
            _exp[i].acceptVisit(visitor);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}
