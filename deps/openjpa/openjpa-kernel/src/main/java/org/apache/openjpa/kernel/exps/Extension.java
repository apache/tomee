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
 * A value produced from evaluating a custom extension.
 *
 * @author Abe White
 */
class Extension
    extends Val {

    private final FilterListener _listener;
    private final Val _target;
    private final Val _arg;

    /**
     * Constructor. Supply filter listener, its target value, and
     * its argument value, if any.
     */
    public Extension(FilterListener listener, Val target, Val arg) {
        _listener = listener;
        _target = target;
        _arg = arg;
    }

    public Class getType() {
        Class targetClass = (_target == null) ? null : _target.getType();
        return _listener.getType(targetClass, getArgTypes());
    }

    public void setImplicitType(Class type) {
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        Object target = null;
        Class targetClass = null;
        if (_target != null) {
            target = _target.eval(candidate, orig, ctx, params);
            targetClass = _target.getType();
        }
        Object arg = null;
        if (_arg != null)
            arg = _arg.eval(candidate, orig, ctx, params);
        return _listener.evaluate(target, targetClass, getArgs(arg),
            getArgTypes(), candidate, ctx);
    }

    private Class[] getArgTypes() {
        if (_arg == null)
            return null;
        if (_arg instanceof Args)
            return ((Args) _arg).getTypes();
        return new Class[]{ _arg.getType() };
    }

    private Object[] getArgs(Object arg) {
        if (arg == null)
            return null;
        if (_arg instanceof Args)
            return (Object[]) arg;
        return new Object[]{ arg };
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        if (_target != null)
            _target.acceptVisit(visitor);
        if (_arg != null)
            _arg.acceptVisit(visitor);
        visitor.exit(this);
    }
}

