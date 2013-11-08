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
package org.apache.openjpa.persistence.query;

/**
 * Logical Predicate combines two predicates with a logical operator.
 * 
 * @author Pinaki Poddar
 *
 */
public class LogicalPredicate extends AbstractVisitable 
    implements Predicate, Visitable {
    private final Predicate _p1;
    private final Predicate _p2;
    private final ConditionalOperator _op;
    private final ConditionalOperator _nop;
    
    public LogicalPredicate(Predicate p1, ConditionalOperator op, 
            ConditionalOperator nop, Predicate p2) {
        _p1  = p1;
        _p2  = p2;
        _op  = op;
        _nop = nop;
    }
    
    public Predicate and(Predicate predicate) {
        return new AndPredicate(this, predicate);
    }

    public Predicate or(Predicate predicate) {
        return new OrPredicate(this, predicate);
    }
    
    public Predicate not() {
        return new LogicalPredicate(_p1.not(), _nop, _op, _p2.not());
    }

    public String asExpression(AliasContext ctx) {
        return OPEN_BRACE + ((Visitable)_p1).asExpression(ctx) + SPACE + _op +
               SPACE + ((Visitable)_p2).asExpression(ctx) + CLOSE_BRACE;
    }
}
