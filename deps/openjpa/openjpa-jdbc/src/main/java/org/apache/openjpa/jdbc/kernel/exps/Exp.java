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
import org.apache.openjpa.kernel.exps.Expression;

/**
 * An Expression represents a query ready for execution. Generally, it is
 * a set of conditions that must be met for the query to be true.
 *
 * @author Abe White
 */
interface Exp
    extends Expression {

    /**
     * Initialize the expression. This method should recursively
     * initialize any sub-expressions or values.
     *
     * @param contains map of relation paths to the number of times
     * the paths appear in a contains() expression;
     * used to ensure paths used for contains() within
     * the same AND expression used different aliases
     */
    public ExpState initialize(Select sel, ExpContext ctx, Map contains);

    /**
     * Append the SQL for this expression to the given buffer. The SQL
     * should optionally include any joins this expression needs.
     */
    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf);

    /**
     * Select just the columns for this expression.
     */
    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks);
}
