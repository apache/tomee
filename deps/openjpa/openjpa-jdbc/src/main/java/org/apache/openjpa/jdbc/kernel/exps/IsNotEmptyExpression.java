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

import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Tests whether the given value is not empty.
 *
 * @author Marc Prud'hommeaux
 */
class IsNotEmptyExpression
    implements Exp {

    private final Val _val;

    /**
     * Constructor. Supply value to test.
     */
    public IsNotEmptyExpression(Val val) {
        _val = val;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        return _val.initialize(sel, ctx, Val.NULL_CMP);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        _val.calculateValue(sel, ctx, state, null, null);
        _val.appendIsNotEmpty(sel, ctx, state, buf);
        sel.append(buf, state.joins);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        _val.selectColumns(sel, ctx, state, true);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        visitor.exit(this);
    }
}
