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

import static org.apache.openjpa.persistence.query.PathOperator.NAVIGATION;
/**
 * Represents a path resulted by navigation.
 * 
 * @author Pinaki Poddar
 *
 */
class NavigationPath extends AbstractDomainObject implements PathExpression {
    protected NavigationPath(QueryDefinitionImpl owner, AbstractPath parent, 
		String attr) {
		super(owner, parent, NAVIGATION, attr);
	}
	
	@Override
	public String getLastSegment() {
		return (String)super.getLastSegment();
	}
	
	@Override
	public String getAliasHint(AliasContext ctx) {
		return getLastSegment();
	}

	@Override
	public String asProjection(AliasContext ctx) {
		AbstractPath parent = getParent();
		if (ctx.hasAlias(parent))
            return ctx.getAlias(parent) + NAVIGATION + getLastSegment();
        return getParent().asProjection(ctx) + NAVIGATION + getLastSegment();
	}
		
	@Override
	public String asExpression(AliasContext ctx) {
        return getParent().asExpression(ctx) + NAVIGATION + getLastSegment();
	}
	
	/**
	 * A navigation path is joinable only when it represents domain of a  
	 * subquery.
	 * @see QueryDefinitionImpl#addSubqueryRoot(PathExpression)
	 */
	@Override
	public String asJoinable(AliasContext ctx) {
		return asProjection(ctx) + SPACE + ctx.getAlias(this);
	}
	
	public String toString() {
		return getParent().toString()+ NAVIGATION +getLastSegment();
	}

}
