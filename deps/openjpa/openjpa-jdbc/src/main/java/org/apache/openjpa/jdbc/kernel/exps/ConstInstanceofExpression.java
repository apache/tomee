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
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Tests whether a value is an instance of a class.
 *
 * @author Abe White
 */
class ConstInstanceofExpression
    implements Exp {

    private final Const _const;
    private final Class _cls;

    /**
     * Constructor. Supply the constant to test and the class.
     */
    public ConstInstanceofExpression(Const val, Class cls) {
        _const = val;
        _cls = Filters.wrap(cls);
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        return _const.initialize(sel, ctx, 0);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        _const.calculateValue(sel, ctx, state, null, null);
        if (_cls.isInstance(_const.getValue(ctx, state)))
            buf.append("1 = 1");
        else
            buf.append("1 <> 1");
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        _const.selectColumns(sel, ctx, state, pks);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _const.acceptVisit(visitor);
        visitor.exit(this);
    }
}
