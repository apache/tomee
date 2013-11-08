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
 * Represents a parameter.
 *
 * @author Abe White
 */
class Param
    extends Val
    implements Parameter {

    private Object _key = null;
    private Class _type = null;
    private int _index = -1;

    /**
     * Constructor. Provide parameter name and type.
     */
    public Param(Object key, Class type) {
        _key = key;
        _type = type;
    }

    public Object getParameterKey() {
        return _key;
    }

    public Class getType() {
        return _type;
    }

    public void setImplicitType(Class type) {
        _type = type;
    }

    public void setIndex(int index) {
        _index = index;
    }

    public Object getValue(Object[] params) {
        return params[_index];
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        return getValue(params);
    }
}

