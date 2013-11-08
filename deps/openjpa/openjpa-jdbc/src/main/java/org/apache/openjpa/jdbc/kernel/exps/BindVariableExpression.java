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
 * Binds a variable to a value. Typically, the {@link #initialize} and
 * {@link #getJoins} methods of this expression are not called. They are
 * only called if the variable being bound is otherwise unused in the filter,
 * in which case we must at least make the joins to the variable because the
 * act of binding a variable should at least guarantee that an instance
 * represting the variable could exist (i.e. the binding collection is not
 * empty).
 *
 * @author Abe White
 */
class BindVariableExpression
    extends EmptyExpression {

    private final Variable _var;

    /**
     * Constructor. Supply values.
     */
    public BindVariableExpression(Variable var, PCPath val, boolean key) {
        if (key)
            val.getKey();
        var.setPCPath(val);
        _var = var;
    }

    public Variable getVariable() {
        return _var;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        return _var.initialize(sel, ctx, 0);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        buf.append("1 = 1");
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _var.acceptVisit(visitor);
        visitor.exit(this);
    }
}
