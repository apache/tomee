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
import org.apache.openjpa.kernel.exps.Arguments;

/**
 * A unary operator that executes a datastore specific function with zero or more arguments.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class DatastoreFunction extends UnaryOp {
    private final String _functionName;
    
    public DatastoreFunction(String name, Class<?> resultType, Arguments args) {
        super((Val)args);
        _functionName = name;
        setImplicitType(resultType);
    }

    @Override
    protected String getOperator() {
        return _functionName;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        Args args = (Args) getValue();
        if (!ctx.store.getDBDictionary().requiresCastForMathFunctions || args.getValues().length == 1) {
            super.appendTo(sel, ctx, state, sql, index);
        } else {
            sql.append(getOperator());
            sql.append("(");            
            args.appendTo(sel, ctx, state, sql, 0, getOperator());
            sql.append(")");
        }            
    }
}
