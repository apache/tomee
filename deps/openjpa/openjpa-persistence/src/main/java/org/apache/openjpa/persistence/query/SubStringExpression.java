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
 * Denotes SUBSTR(a,i1,i2) Expression.
 * 
 * @author Pinaki Poddar
 *
 */
public class SubStringExpression extends UnaryOperatorExpression {
	private final Expression _start;
	private final Expression _length;
	public SubStringExpression(Expression op, Expression start) {
		super(op, UnaryFunctionalOperator.SUBSTR);
		_start  = start;
		_length = null;
	}
	
	public SubStringExpression(Expression op, int start) {
		super(op, UnaryFunctionalOperator.SUBSTR);
		_start  = new ConstantExpression(start);
		_length = null;
	}
	
	public SubStringExpression(Expression op, int start, int len) {
		super(op, UnaryFunctionalOperator.SUBSTR);
		_start  = new ConstantExpression(start);
		_length = new ConstantExpression(len);
	}
	
    public SubStringExpression(Expression op, Expression start, Expression l) {
		super(op, UnaryFunctionalOperator.SUBSTR);
		_start  = start;
		_length = new ConstantExpression(l);
	}
	
	public String asExpression(AliasContext ctx) {
		return _op + "(" + ((Visitable)_e).asExpression(ctx)  
			 + "," + ((Visitable)_start).asExpression(ctx)
             + (_length == null ? "" : ","
             + ((Visitable)_length).asExpression(ctx)) + ")";
	}
}
