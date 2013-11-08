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
import org.apache.openjpa.util.InternalException;

/**
 * Size.
 *
 * @author Marc Prud'hommeaux
 */
class Size
    extends UnaryOp {

    public Size(Val val) {
        super(val);
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        // initialize the value with a null test
        return initializeValue(sel, ctx, NULL_CMP);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        getValue().calculateValue(sel, ctx, state, null, null);
        getValue().appendSize(sel, ctx, state, sql);
        sel.append(sql, state.joins);
    }

    protected Class getType(Class c) {
        return long.class;
    }

    protected String getOperator() {
        // since we override appendTo(), this method should never be called
        throw new InternalException();
    }
}
