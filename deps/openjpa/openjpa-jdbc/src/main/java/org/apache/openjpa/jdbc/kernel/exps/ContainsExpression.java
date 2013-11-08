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
 * Tests whether one value contains another.
 *
 * @author Abe White
 */
class ContainsExpression
    extends EqualExpression {

    /**
     * Constructor. Supply values to test.
     */
    public ContainsExpression(Val val1, Val val2) {
        super(val1, val2);
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        Val val1 = getValue1();
        if (contains != null && val1 instanceof PCPath) {
            PCPath sql = (PCPath) val1;
            String path = sql.getPCPathString();

            // update the count for this path
            Integer count = (Integer) contains.get(path);
            if (count == null)
                count = 0;
            else
                count = count.intValue() + 1;
            contains.put(path, count);

            sql.setContainsId(count.toString());
        }
        return super.initialize(sel, ctx, contains);
    }

    protected boolean isDirectComparison() {
        return false;
    }
}
