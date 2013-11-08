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
 * Denotes LOCATE(e1, e2, n) Expression.
 * e1 : string to be located
 * e2 : string to be searched
 * n  : starting poistion in e2, default is 1
 * 
 * @author Pinaki Poddar
 *
 */
public class LocateExpression extends BinaryOperatorExpression  {
	private final Expression _start;

	public LocateExpression(Expression key, String str, int start) {
        super(key, BinaryFunctionalOperator.LOCATE,
                new ConstantExpression(str));
		_start = new ConstantExpression(start);
	}
	
	public LocateExpression(Expression key, Expression str, int start) {
		super(key, BinaryFunctionalOperator.LOCATE, str);
		_start = new ConstantExpression(start);
	}
	
	public LocateExpression(Expression key, String str, Expression start) {
        super(key, BinaryFunctionalOperator.LOCATE,
                new ConstantExpression(str));
		_start = start;
	}
	
    public LocateExpression(Expression key, Expression str, Expression start) {
        super(key, BinaryFunctionalOperator.LOCATE, str);
		_start = start;
	}
	
	public String asExpression(AliasContext ctx) {
        String start = _start == null ? EMPTY : COMMA +
                ((Visitable)_start).asExpression(ctx);
		return new StringBuilder(_op.toString())
		    .append(OPEN_BRACE)
		    .append(((Visitable)_e1).asExpression(ctx))
		    .append(COMMA) 
		    .append(((Visitable)_e2).asExpression(ctx)) 
		    .append(start)
		    .append(CLOSE_BRACE).toString();
	}

}
