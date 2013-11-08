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
 * Else clause in a Case Statement.
 * 
 * @author Pinaki Poddar
 *
 */
public class ElseExpression extends ExpressionImpl {
	private final CaseExpressionImpl _caseClause;
	private final Expression _elseClause;
	
	public ElseExpression(CaseExpressionImpl owner, Expression op) {
		_caseClause = owner;
		_elseClause = op;
	}
	
	@Override
	public String asExpression(AliasContext ctx) {
		return ((Visitable)_caseClause).asExpression(ctx) 
		    + " ELSE " + ((Visitable)_elseClause).asExpression(ctx) 
		    + " END";
	}
	
	@Override
	public String asProjection(AliasContext ctx) {
		return ((Visitable)_caseClause).asProjection(ctx) 
	    + " ELSE " + ((Visitable)_elseClause).asProjection(ctx) 
	    + " END";
	}
}
