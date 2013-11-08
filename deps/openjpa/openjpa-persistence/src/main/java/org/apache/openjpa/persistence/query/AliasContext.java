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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.openjpa.meta.MetaDataRepository;

class AliasContext {
	private Stack<Object> _operating = new Stack<Object>();
	private Map<ExpressionImpl, String> _aliases = 
		new HashMap<ExpressionImpl, String>();
	private final MetaDataRepository _repos;
	
	public AliasContext(MetaDataRepository repos) {
		_repos = repos;
	}
	
	/**
	 * Sets alias for the given Expression or gets the alias if the given
	 * path has already been assigned an alias.
	 * The given expression must provide a hint on what should be the 
	 * alias name. If the alias name is assigned by this context, then a
	 * different alias is generated.
	 * @param path
	 * @return the alias name
	 */
	public String getAlias(ExpressionImpl path) {
		String alias = _aliases.get(path);
		if (alias != null)
			return alias;
		return setAlias(path);
	}
	
	public String setAlias(ExpressionImpl path) {
		if (_aliases.containsKey(path))
			return _aliases.get(path);
        String alias = path.getAliasHint(this).substring(0,1).toLowerCase();
		int i = 2;
		while (_aliases.containsValue(alias)) {
			alias = alias.substring(0,1) + i;
			i++;
		}
		_aliases.put(path, alias);
		return alias;
	}
	
	/**
	 * Affirms if the given Expression has been assigned an alias by this
	 * context.
	 */
	public boolean hasAlias(Expression path) {
		return _aliases.containsKey(path);
	}
	
	public AliasContext push(Object e) {
		if (_operating.contains(e))
            throw new RuntimeException(e + " is already in this ctx");
		_operating.add(e);
		return this;
	}
	
	public String getEntityName(Class cls) {
		return cls.getSimpleName();
//		return _repos.getMetaData(cls, null, true).getTypeAlias();
	}
}
