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
package org.apache.openjpa.jdbc.kernel.exps;

import java.util.Collection;
import java.util.Map;

/**
 * Tests whether a value is IN a map key set.
 *
 * @author Abe White
 */
class InKeyExpression
    extends InExpression {

    /**
     * Constructor. Supply the value to test and the constant to obtain
     * the parameters from.
     */
    public InKeyExpression(Val val, Const constant) {
        super(val, constant);
    }

    /**
     * Return the collection to test for containment with.
     */
    protected Collection getCollection(ExpContext ctx, ExpState state) {
        Map map = (Map) getConstant().getValue(ctx, state);
        return (map == null) ? null : map.keySet();
    }
}
