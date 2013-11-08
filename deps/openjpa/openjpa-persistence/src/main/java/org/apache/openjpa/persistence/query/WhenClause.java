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
 * Denotes WHEN ... THEN ... clause of a Case Statement.
 * 
 * @author Pinaki Poddar
 *
 */
public class WhenClause {
	private final Object when;
	private Object then;
	
	WhenClause(Object op) {
		when = op;
	}
	
	Object getThen() {
		return then;
	}
	
	void setThen(Object then) {
		if (hasThen())
            throw new IllegalStateException("then() is already set");
		this.then = then;
	}
	
	boolean hasThen() {
		return then != null;
	}
	
	public String toJPQL(AliasContext ctx) {
		StringBuilder tmp = new StringBuilder();
		tmp.append(" WHEN ").append(JPQLHelper.toJPQL(ctx, when))
		   .append(" THEN ").append(JPQLHelper.toJPQL(ctx, then));
		return tmp.toString();
	}
}
