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

import org.apache.openjpa.kernel.exps.AbstractExpressionVisitor;
import org.apache.openjpa.kernel.exps.Expression;

/**
 * Determines whether the visited expressions include a "contains" expression.
 * 
 * @author Abe White
 */
class HasContainsExpressionVisitor 
    extends AbstractExpressionVisitor {

    private boolean _found = false;

    public static boolean hasContains(Expression exp) {
        if (exp == null)
            return false;
        HasContainsExpressionVisitor v = new HasContainsExpressionVisitor();
        exp.acceptVisit(v);
        return v._found;
    }

    public void enter(Expression exp) {
        if (!_found)
            _found = exp instanceof ContainsExpression 
                || exp instanceof BindVariableAndExpression;
    }
} 
