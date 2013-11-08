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
 * Enumeration of operator that operates on a single expression to generate 
 * a predicate.
 * 
 * @see UnaryOperatorExpression
 * 
 * @author Pinaki Poddar
 *
 */
public enum UnaryConditionalOperator {
	EXISTS("EXISTS"),
	EXISTS_NOT("NOT EXISTS"),
	
	ISEMPTY("IS EMPTY"),
	ISEMPTY_NOT("IS NOT EMPTY"),
	ISNULL("IS NULL"),
	ISNULL_NOT("IS NOT NULL"),
	SOME("SOME");
	
	private final String _symbol;
	
	UnaryConditionalOperator(String symbol) {
		_symbol = symbol;
	}
	
	public String toString() {
		return _symbol;
	}
}
