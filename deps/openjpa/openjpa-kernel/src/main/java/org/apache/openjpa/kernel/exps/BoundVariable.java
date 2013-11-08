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
 * Represents a bound variable. Variables are aliased to the
 * collection that is stated to contain them in the query.
 * When the query is evaluated, expressions containing variables will be
 * executed once for every element in the collection.
 *
 * @author Abe White
 */
class BoundVariable
    extends Val {

    private Class _type = null;
    private Object _val = null;

    /**
     * Constructor. Provide variable name and type.
     */
    public BoundVariable(Class type) {
        _type = type;
    }

    /**
     * Set the value this variable should take for the current iteration.
     *
     * @return false if the type is incompatible with the variable's
     * declared type
     */
    public boolean setValue(Object value) {
        if (value != null && !_type.isAssignableFrom(value.getClass()))
            return false;
        _val = value;
        return true;
    }

    public boolean isVariable() {
        return true;
    }

    public Class getType() {
        return _type;
    }

    public void setImplicitType(Class type) {
        _type = type;
    }

    /**
     * Cast this value to the given type.
     */
    public void castTo(Class type) {
        // incompatible types?
        if (!_type.isAssignableFrom(type)
            && !type.isAssignableFrom(_type))
            throw new ClassCastException(_type.getName());
        _type = type;
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        return _val;
	}
}

