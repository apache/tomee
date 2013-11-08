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
 * A single expression that holds an array of values. Used as the second operand
 * in e1 IN(e2) expression, for example. Different than 
 * {@link VarArgsExpression} which represents multiple expressions.  
 * 
 * @author Pinaki Poddar
 *
 */
public class ArrayExpression extends ExpressionImpl {
	private final Object[] _values;
	
	public ArrayExpression(Object[] values) {
		_values = values;
	}

	@Override
	public String asExpression(AliasContext ctx) {
		StringBuilder tmp = new StringBuilder(OPEN_BRACE);
		for (int i = 0; i < _values.length; i++) {
			Object v = _values[i];
			tmp.append((v instanceof Visitable ? 
                ((Visitable)v).asExpression(ctx) : JPQLHelper.toJPQL(ctx, v)))
                .append(i == _values.length-1 ? EMPTY : COMMA + SPACE);
		}
		tmp.append(CLOSE_BRACE);
		return tmp.toString();
	}

	@Override
	public String asProjection(AliasContext ctx) {
		return asExpression(ctx);
	}
}
