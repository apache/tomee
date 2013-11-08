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
 * Binary predicate combines two expressions with an operator. 
 * 
 * @author Pinaki Poddar
 *
 */
class BinaryExpressionPredicate extends AbstractVisitable 
    implements Predicate, Visitable {
	protected final Expression _e1;
	protected final Expression _e2;
	protected final BinaryConditionalOperator   _op;
	private final BinaryConditionalOperator   _nop;
	private static final StringBuffer SPACE = new StringBuffer(" ");
	
	BinaryExpressionPredicate(Expression e1, BinaryConditionalOperator op, 
		BinaryConditionalOperator nop, Expression e2) {
		_e1 = e1;
		_e2 = e2;
		_op = op;
		_nop = nop;
	}
	
	public final Expression getOperand() {
		return _e1;
	}
	
	public final Expression getOperand2() {
		return _e2;
	}
	
	public final BinaryConditionalOperator getOperator() {
		return _op;
	}
	
	public Predicate and(Predicate predicate) {
		return new AndPredicate(this, predicate);
	}

	public Predicate or(Predicate predicate) {
		return new OrPredicate(this, predicate);
	}
	
	public Predicate not() {
		if (  _nop == null)
            throw new UnsupportedOperationException(this.toString());
		return new BinaryExpressionPredicate(_e1, _nop, _op, _e2);
	}

	public String asExpression(AliasContext ctx) {
		return asExpression((Visitable)_e1, ctx)  
		     + SPACE + _op + SPACE
		     + asExpression((Visitable)_e2, ctx);
	}
	
	String asExpression(Visitable v, AliasContext ctx) {
		String result = v.asExpression(ctx);
		if (v instanceof QueryDefinitionImpl)
		    return "(" + result + ")";
		return result;
	}
}
