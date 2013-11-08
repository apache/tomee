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
 * Represents an unbound variable. When the query is evaluated,
 * expressions containing unbound variables will be
 * executed once for every element in the type's extent.
 *
 * @author Abe White
 */
class UnboundVariable
    extends Val {

    private Class _type = null;
    private Object _val = null;

    /**
     * Constructor. Provide variable name and type.
     */
    public UnboundVariable(Class type) {
        _type = type;
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
     * Set the variable's current value. Expressions can be evaluated
     * for every possible object in the extent of each unbound variable
     * when looking for a match.
     */
    public void setValue(Object value) {
        _val = value;
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        return _val;
    }
}

