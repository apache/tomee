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

import java.util.Map;

import org.apache.openjpa.kernel.StoreContext;

/**
 * Returns the value of the specified key in a Map.
 *
 * @author Marc Prud'hommeaux
 */
class GetMapValue
    extends Val {

    private final Val _map;
    private final Val _arg;

    /**
     * Constructor. Provide value to upper-case.
     */
    public GetMapValue(Val map, Val arg) {
        _map = map;
        _arg = arg;
    }

    public boolean isVariable() {
        return false;
    }

    public Class getType() {
        return Object.class;
    }

    public void setImplicitType(Class type) {
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        return ((Map) _map.eval(candidate, orig, ctx, params)).
            get(_arg.eval(candidate, orig, ctx, params));
    }
}
