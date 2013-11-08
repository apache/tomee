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
 * Path resulting by joining from a parent path via an attribute.
 * 
 * @author Pinaki Poddar
 *
 */
public class JoinPath extends AbstractDomainObject implements DomainObject {
    public JoinPath(AbstractDomainObject parent, PathOperator join, String attr)
    {
		super(parent.getOwner(), parent, join, attr);
	}
	
	@Override
	public String getAliasHint(AliasContext ctx) {
		return getLastSegment();
	}
	
	@Override
	public String getLastSegment() {
		return super.getLastSegment().toString();
	}
	
	@Override
	public AbstractDomainObject getParent() {
		return (AbstractDomainObject)super.getParent();
	}
		
	@Override
	public String asJoinable(AliasContext ctx) {
		return new StringBuilder(getOperator().toString())
		   .append(getParent().asProjection(ctx))
		   .append(NAVIGATION)
		   .append(getLastSegment())
		   .append(" ")
		   .append(ctx.getAlias(this)).toString();
	}
	
	@Override
	public String asExpression(AliasContext ctx) {
		if (ctx.hasAlias(this))
			return ctx.getAlias(this);
		return getParent().asExpression(ctx)
		       + NAVIGATION
		       + getLastSegment();
	}
	
	@Override
	public String asProjection(AliasContext ctx) {
		if (ctx.hasAlias(this))
			return ctx.getAlias(this);
		return getParent().asProjection(ctx)
		       + NAVIGATION
		       + getLastSegment();
	}
	
	public String toString() {
        return getOperator() + getParent().toString() + "*" + getLastSegment();
	}

}
