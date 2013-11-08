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

import java.util.Arrays;
import java.util.List;

/**
 * Denotes NEW fully.qualified.class.name(arg1, arg2,...) 
 * 
 * @author Pinaki Poddar
 *
 */
public class NewInstance extends AbstractVisitable 
	implements SelectItem, Visitable {
	private final Class _cls;
	private List<SelectItem> _args;
	NewInstance(Class cls, SelectItem...args) {
		_cls = cls;
		if (args != null) {
			_args = Arrays.asList(args);
		}
	}
	
	public OrderByItem asc() {
		throw new UnsupportedOperationException();
	}

	public OrderByItem desc() {
		throw new UnsupportedOperationException();
	}
	
	
	public String asProjection(AliasContext ctx) {
            StringBuilder tmp = new StringBuilder("NEW ").append(_cls.getName())
		    .append("(");
		if (_args == null || _args.isEmpty())
			return tmp.append(")").toString();
		int i = 0;
		int N = _args.size();
		for (SelectItem arg : _args) {
			i++;
			tmp.append(((Visitable)arg).asProjection(ctx))
			   .append(i == N ? ")" : ",");
		}
		return tmp.toString();
	}
}
