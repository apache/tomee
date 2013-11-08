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

import java.sql.Time;
import java.util.Date;

/**
 * Denotes CURRENT_TIME(), CURRENT_DATE() and CURRENT_TIMESTAMP() expressions.
 * 
 * @author Pinaki Poddar
 *
 */
public class CurrentTimeExpression extends ExpressionImpl {
	private static enum Now {
		CURRENT_DATE,
		CURRENT_TIME,
		CURRENT_TIMESTAMP
	}
	
	private final Class _type;

	public CurrentTimeExpression(Class operand) {
		_type = operand;
	}
	
	@Override
	public String asExpression(AliasContext ctx) {
		Now now = (_type == Date.class 
				? Now.CURRENT_DATE
				: (_type == Time.class
                        ? Now.CURRENT_TIME : Now.CURRENT_TIMESTAMP));
		return now.toString();
	}
	
	@Override
	public String asProjection(AliasContext ctx) {
		throw new UnsupportedOperationException();
	}
}
