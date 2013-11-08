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
 * An abstract implementation that throws UnsupportedOperationException on
 * every method.
 * 
 * @author Pinaki Poddar
 *
 */
abstract class AbstractVisitable implements Visitable {
	public static final String SPACE = " ";
	public static final String OPEN_BRACE = "(";
	public static final String CLOSE_BRACE = ")";
	public static final String COMMA = ",";
	public static final String EMPTY = "";
	
	public String asExpression(AliasContext ctx) {
        throw new UnsupportedOperationException(this.getClass().getName());
	}

	public String asProjection(AliasContext ctx) {
        throw new UnsupportedOperationException(this.getClass().getName());
	}

	public String getAliasHint(AliasContext ctx) {
        throw new UnsupportedOperationException(this.getClass().getName());
	}

	public String asJoinable(AliasContext ctx) {
        throw new UnsupportedOperationException(this.getClass().getName());
	}

}
