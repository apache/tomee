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

import java.io.Serializable;

/**
 * An element of query that is convertible to a JPQL String given a aliasing 
 * scheme. QueryDefinition visits each of its element and translates them.
 * 
 * @author Pinaki Poddar
 *
 */
public interface Visitable extends Serializable {
	/**
	 * Get a JPQL fragment as used in WHERE clause.
	 */
	String asExpression(AliasContext ctx);
	
	/**
	 * Gets the string representation in SELECT projection.
	 */
	String asProjection(AliasContext ctx);
	
	/**
	 * Gets the string representation in FROM clause.
	 */
	String asJoinable(AliasContext ctx);
	
	/**
	 * Gets the hint to be used while creating alias.
	 */
	String getAliasHint(AliasContext ctx);

}
