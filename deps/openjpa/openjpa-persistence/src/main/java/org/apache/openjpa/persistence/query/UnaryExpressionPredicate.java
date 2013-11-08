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
 * Unary Predicate results from an operator on an Expression.
 * 
 * @author Pinaki Poddar
 *
 */
class UnaryExpressionPredicate extends AbstractVisitable 
	implements Predicate, Visitable {
	protected final Expression _e;
	protected final UnaryConditionalOperator _op;
	private final UnaryConditionalOperator _nop;

    public UnaryExpressionPredicate(Expression e, UnaryConditionalOperator op,
		UnaryConditionalOperator nop) {
		this._e   = e;
		this._op  = op;
		this._nop = nop;
	}
	
	public Expression getOperand() {
		return _e;
	}
	
	public UnaryConditionalOperator getOperator() {
		return _op;
	}

	public Predicate and(Predicate predicate) {
		return new AndPredicate(this, predicate);
	}
	
	public Predicate or(Predicate predicate) {
		return new OrPredicate(this, predicate);
	}
	
	public Predicate not() {
		if (_nop == null)
            throw new UnsupportedOperationException(this.toString());
		return new UnaryExpressionPredicate(_e, _nop, _op);
	}

	public String asExpression(AliasContext ctx) {
		return _op + "(" + ((ExpressionImpl)_e).asExpression(ctx) + ")";
	}
}
