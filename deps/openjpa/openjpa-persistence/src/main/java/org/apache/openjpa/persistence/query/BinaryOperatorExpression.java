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
 * An expression resulting from a binary operation on two expressions.
 *  
 * @author Pinaki Poddar
 *
 */
public class BinaryOperatorExpression extends ExpressionImpl {
	protected final Expression _e1;
	protected final Expression _e2;
	protected final BinaryFunctionalOperator   _op;
	
    public BinaryOperatorExpression(Expression e1, BinaryFunctionalOperator op,
		Expression e2) {
		_e1 = e1;
		_e2 = e2;
		_op = op;
	}
	
	public Expression getOperand1() {
		return _e1;
	}
	
	public Expression getOperand2() {
		return _e2;
	}
	
	public BinaryFunctionalOperator getOperator() {
		return _op;
	}
	
	public String asExpression(AliasContext ctx) {
		return ((Visitable)_e1).asExpression(ctx)
			+ _op 
		    + ((Visitable)_e2).asExpression(ctx);
	}
	
	public String asProjection(AliasContext ctx) {
		return ((Visitable)_e1).asProjection(ctx)
			 + _op 
		     + (((Visitable)_e2).asProjection(ctx))
		     + (ctx.hasAlias(this) ? " as " + ctx.getAlias(this) : "");

	}
}
