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

import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;

/**
 * Count non-null values.
 *
 * @author Abe White
 */
class Count
    extends UnaryOp {

    private boolean isCountMultiColumns = false;
    private boolean isCountDistinct = false;
    
    /**
     * Constructor. Provide the value to operate on.
     */
    public Count(Val val) {
        super(val);
        if (val instanceof Distinct)
            isCountDistinct = true;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        // join into related object if present
        ExpState expState = initializeValue(sel, ctx, JOIN_REL);
        Val val = isCountDistinct ? ((Distinct)getValue()).getValue() : getValue();
        if (val instanceof PCPath) {
            Column[] cols = ((PCPath)val).getColumns(expState);
            if (cols.length > 1) {
                isCountMultiColumns = true;
            }
        }
            
        return expState;
    }

    protected Class getType(Class c) {
        return long.class;
    }

    protected String getOperator() {
        return "COUNT";
    }

    public boolean isAggregate() {
        return true;
    }

    public boolean isCountDistinctMultiCols() {
        return isCountDistinct && isCountMultiColumns;
    }

    /**
     * Overrides SQL formation by replacing COUNT(column) by COUNT(*) when specific conditions are met and
     * DBDictionary configuration <code>useWildCardForCount</code> is set.
     */
    @Override
    public void appendTo(Select sel, ExpContext ctx, ExpState state, SQLBuffer sql, int index) {
        if (isCountDistinctMultiCols()) {
            getValue().appendTo(sel, ctx, state, sql, 0);
            sql.addCastForParam(getOperator(), getValue());
        } else
            super.appendTo(sel, ctx, state, sql, index);
        if ((ctx.store.getDBDictionary().useWildCardForCount && state.joins.isEmpty()) ||
            !isCountDistinct && isCountMultiColumns){
            String s = sql.getSQL();
            if (s.startsWith("COUNT(") && s.endsWith(")")) {
                sql.replaceSqlString("COUNT(".length(), s.length()-1, "*");
            }
        }
    }

}

