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
package org.apache.openjpa.persistence.criteria;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

/**
 * Ordering clause of a criteria query.
 * 
 * @author Pinaki Poddar
 *
 */
class OrderImpl implements Order, CriteriaExpression {
	private boolean _ascending;
	private final ExpressionImpl<?> e;
	
	public OrderImpl(Expression<?> e, boolean asc) {
		this.e = (ExpressionImpl<?>) e;
		_ascending = asc;
	}
	
	public OrderImpl(Expression<?> e) {
		this(e, true);
	}
	
	public ExpressionImpl<?> getExpression() {
		return e;
	}
	
	public boolean isAscending() {
		return _ascending;
	}

	public Order reverse() {
		_ascending = !_ascending;
		return this;
	}
	
    public void acceptVisit(CriteriaExpressionVisitor visitor) {
        if (!visitor.isVisited(this)) {
            visitor.enter(this);
            visitor.exit(this);
        }
    }
    
    public StringBuilder asValue(AliasContext q) {
        return (e.isAutoAliased() ? e.asValue(q) : new StringBuilder(e.getAlias()))
            .append(_ascending ? "" : " DESC");
    }
    
    public StringBuilder asProjection(AliasContext q) {
        throw new IllegalStateException(this + " can not be rendered as projection");
    }
    
    public StringBuilder asVariable(AliasContext q) {
        throw new IllegalStateException(this + " can not be rendered as variable");
    }
}
