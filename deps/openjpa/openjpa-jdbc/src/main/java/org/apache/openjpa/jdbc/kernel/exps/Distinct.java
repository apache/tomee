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
 * Distinct the specified path.
 *
 * @author Marc Prud'hommeaux
 */
class Distinct
    extends UnaryOp {

    public Distinct(Val val) {
        super(val, true);
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        // join into related object if present
        return initializeValue(sel, ctx, JOIN_REL);
    }

    protected String getOperator() {
        return "DISTINCT";
    }

    @Override
    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        Val val = getValue();
        if (val instanceof PCPath) {
            boolean noParen = getNoParen();
            sql.append(getOperator());
            sql.append(noParen ? " " : "(");
            ((PCPath)val).appendTo(sel, ctx, state, sql); 
            sql.addCastForParam(getOperator(), val);
            if (!noParen)
                sql.append(")");
            
        } else
            super.appendTo(sel, ctx, state, sql, index);
    }
    
}
