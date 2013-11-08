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

import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;

/**
 * Compares two values.
 *
 * @author Abe White
 */
class EqualExpression
    extends CompareEqualExpression {

    /**
     * Constructor. Supply values to compare.
     */
    public EqualExpression(Val val1, Val val2) {
        super(val1, val2);
    }

    public void appendTo(Select sel, ExpContext ctx, BinaryOpExpState bstate, 
        SQLBuffer buf, boolean val1Null, boolean val2Null) {
        if (val1Null && val2Null)
            buf.append("1 = 1");
        else if (val1Null || val2Null) {
            Val val = (val1Null) ? getValue2() : getValue1();
            ExpState state = (val1Null) ? bstate.state2 : bstate.state1;
            if (!isDirectComparison()) {
                int len = val.length(sel, ctx, state);
                for (int i = 0; i < len; i++) {
                    if (i > 0)
                        buf.append(" AND ");
                    val.appendTo(sel, ctx, state, buf, i);
                    buf.append(" IS ").appendValue(null);
                }
            } else
                val.appendIsNull(sel, ctx, state, buf);
        } else {
            Val val1 = getValue1();
            Val val2 = getValue2();
            if (val1.length(sel, ctx, bstate.state1) == 1 
                && val2.length(sel, ctx, bstate.state2) == 1) {
                ctx.store.getDBDictionary().comparison(buf, "=",
                    new FilterValueImpl(sel, ctx, bstate.state1, val1),
                    new FilterValueImpl(sel, ctx, bstate.state2, val2));
            } else {
                int len = java.lang.Math.min(val1.length(sel, ctx, 
                    bstate.state1), val2.length(sel, ctx, bstate.state2));
                for (int i = 0; i < len; i++) {
                    if (i > 0)
                        buf.append(" AND ");

                    val1.appendTo(sel, ctx, bstate.state1, buf, i);
                    buf.append(" = ");
                    val2.appendTo(sel, ctx, bstate.state2, buf, i);
                }
            }
        }
    }
}
