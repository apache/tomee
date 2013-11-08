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

import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

/**
 * The factory for QueryDefinition.
 * 
 * 
 * @author Pinaki Poddar
 *
 */
public class QueryBuilderImpl implements OpenJPAQueryBuilder {
	private final OpenJPAEntityManagerFactorySPI _emf;
	
	public QueryBuilderImpl(OpenJPAEntityManagerFactorySPI emf) {
		_emf = emf;
	}
	
	/**
	 * Creates a QueryDefinition without a domain.
	 */
	public QueryDefinition createQueryDefinition() {
		return new QueryDefinitionImpl(this);
	}

	/**
	 * Creates a QueryDefinition with given class as domain.
	 */
	public DomainObject createQueryDefinition(Class root) {
		return new QueryDefinitionImpl(this).addRoot(root);
	}

	/**
	 * Creates a QueryDefinition that can be used a correlated subquery 
	 * with the given path as domain.
	 */
	public DomainObject createSubqueryDefinition(PathExpression path) {
		return new QueryDefinitionImpl(this).addSubqueryRoot(path);
	}
	
	public String toJPQL(QueryDefinition query) {
		MetaDataRepository repos = _emf.getConfiguration()
			.getMetaDataRepositoryInstance();
		AliasContext ctx = new AliasContext(repos);
		if (query instanceof AbstractDomainObject)
            return ((AbstractDomainObject)query).getOwner().asExpression(ctx);
		return ((QueryDefinitionImpl)query).asExpression(ctx);
	}
	
	public QueryDefinition createQueryDefinition(String jpql) {
		throw new UnsupportedOperationException();
	}
	
	public QueryDefinition createQueryDefinition(Query jpql) {
		throw new UnsupportedOperationException();		
	}
}
