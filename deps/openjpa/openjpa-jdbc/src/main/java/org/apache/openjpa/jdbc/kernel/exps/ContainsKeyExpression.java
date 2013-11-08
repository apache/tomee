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

import java.util.Map;

import org.apache.openjpa.jdbc.sql.Select;

/**
 * Tests whether a map value contains a key.
 *
 * @author Abe White
 */
class ContainsKeyExpression
    extends ContainsExpression {

    /**
     * Constructor. Supply values to test.
     */
    public ContainsKeyExpression(Val val1, Val val2) {
        super(val1, val2);
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        Val val1 = getValue1();
        if (val1 instanceof PCPath)
            ((PCPath) val1).getKey();
        return super.initialize(sel, ctx, contains);
    }
}
