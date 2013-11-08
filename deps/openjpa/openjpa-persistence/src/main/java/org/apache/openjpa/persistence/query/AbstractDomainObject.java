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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Domain Object is a path expression over which query is evaluated.
 * Domain object acts as a proxy for a QueryDefinition via delegation.
 * 
 * @author Pinaki Poddar
 *
 */
public abstract class AbstractDomainObject extends AbstractPath 
	implements DomainObject {
	
	protected AbstractDomainObject(QueryDefinitionImpl owner, 
		AbstractPath parent, PathOperator op, Object part2) {
		super(owner, parent, op, part2);
	}

	/**
	 * Adding a root adds a root domain to the owning query. 
	 */
	public DomainObject addRoot(Class cls) {
		return _owner.addRoot(cls);
	}

	/**
	 * Adding a query root adds a subquery to the owning query. 
	 */
	public DomainObject addSubqueryRoot(PathExpression path) {
		return _owner.addSubqueryRoot(path);
	}

	/**
	 * Derives a path from this path by navigating through the given field.
	 */
	public PathExpression get(String attr) {
		return new NavigationPath(_owner, this, attr);
	}

	/**
	 * Derives a path from this path by joining the given field.
	 * Also the joined path becomes a domain of the owning query.
	 */
	public DomainObject join(String attr) {
        return _owner.addDomain(new JoinPath(this, PathOperator.INNER, attr));
	}
	
	/**
	 * Derives a path from this path by outer joining the given field.
	 * Also the joined path becomes a domain of the owning query.
	 */
	public DomainObject leftJoin(String attr) {
        return _owner.addDomain(new JoinPath(this, PathOperator.OUTER, attr));
	}
	
	/**
	 * Derives a path from this path by fetch joining the given field.
	 */
	public FetchJoinObject joinFetch(String attr) {
        return _owner.addDomain(new FetchPath(this, PathOperator.FETCH_INNER, 
			attr));
	}

	/**
	 * Derives a path from this path by fetch joining the given field.
	 */
	public FetchJoinObject leftJoinFetch(String attr) {
        return _owner.addDomain(new FetchPath(this, PathOperator.FETCH_OUTER,
			attr));
	}

	/**
	 * Derives by KEY() operation on this path.
	 */
	public PathExpression key() {
		return new KeyExpression(this);
	}

	/**
	 * Derives by ENTRY() operation on this path.
	 */
	public SelectItem entry() {
		return new EntryExpression(this);
	}

	/**
	 * Derives by INDEX() operation on this path.
	 */
	public Expression index() {
		return new IndexExpression(this);
	}

	/**
	 * Derives a path by VALUE() operation on this path.
	 */
	public PathExpression value() {
		return new ValueExpression(this);
	}

	/**
	 * Derives this path as ALL(subquery) to its owning query.
	 */	
	public Subquery all() {
		return _owner.all();
	}

	/**
	 * Adds this path as ANY(subquery) to its owning query.
	 */	
	public Subquery any() {
		return _owner.any();
	}
	
	/**
	 * Adds this path as SOME(subquery) to its owning query.
	 */	
	public Subquery some() {
		return _owner.some();
	}

	/**
	 * Adds this path as EXISTS(subquery) to its owning query.
	*/	
	public Predicate exists() {
		return _owner.exists();
	}

	public Expression coalesce(Expression... exp) {
		throw new UnsupportedOperationException();
	}

	public Expression coalesce(String... exp) {
		throw new UnsupportedOperationException();
	}

	public Expression coalesce(Date... exp) {
		throw new UnsupportedOperationException();
	}

	public Expression coalesce(Calendar... exp) {
		throw new UnsupportedOperationException();
	}

	public Expression currentDate() {
		return _owner.currentDate();
	}

	public Expression currentTime() {
		return _owner.currentTime();
	}

	public Expression currentTimestamp() {
		return _owner.currentTimestamp();
	}

	public CaseExpression generalCase() {
		return _owner.generalCase();
	}

	public QueryDefinition groupBy(PathExpression... pathExprs) {
		return _owner.groupBy(pathExprs);
	}

	public QueryDefinition groupBy(List<PathExpression> pathExprList) {
		return _owner.groupBy(pathExprList);
	}

	public QueryDefinition having(Predicate predicate) {
		return _owner.having(predicate);
	}

	public Expression literal(String s) {
		return _owner.literal(s);
	}

	public Expression literal(Number n) {
		return _owner.literal(n);
	}

	public Expression literal(boolean b) {
		return _owner.literal(b);
	}

	public Expression literal(Calendar c) {
		return _owner.literal(c);
	}

	public Expression literal(Date d) {
		return _owner.literal(d);
	}

	public Expression literal(char c) {
		return _owner.literal(c);
	}

	public Expression literal(Class cls) {
		return _owner.literal(cls);
	}

	public Expression literal(Enum<?> e) {
		return _owner.literal(e);
	}

	public SelectItem newInstance(Class cls, SelectItem... args) {
		return _owner.newInstance(cls, args);
	}

	public Expression nullLiteral() {
		return _owner.nullLiteral();
	}

	public Expression nullif(Expression exp1, Expression exp2) {
		return _owner.nullif(exp1, exp2);
	}

	public Expression nullif(Number arg1, Number arg2) {
		return _owner.nullif(arg1, arg2);
	}

	public Expression nullif(String arg1, String arg2) {
		return _owner.nullif(arg1, arg2);
	}

	public Expression nullif(Date arg1, Date arg2) {
		return _owner.nullif(arg1, arg2);
	}

	public Expression nullif(Calendar arg1, Calendar arg2) {
		return _owner.nullif(arg1, arg2);
	}

	public Expression nullif(Class arg1, Class arg2) {
		return _owner.nullif(arg1, arg2);
	}

	public Expression nullif(Enum<?> arg1, Enum<?> arg2) {
		return _owner.nullif(arg1, arg2);
	}

	public QueryDefinition orderBy(OrderByItem... orderByItems) {
		return _owner.orderBy(orderByItems);
	}

	public QueryDefinition orderBy(List<OrderByItem> orderByItemList) {
		return _owner.orderBy(orderByItemList);
	}

	public Expression param(String name) {
		return _owner.param(name);
	}

	public Predicate predicate(boolean b) {
		return _owner.predicate(b);
	}

	public QueryDefinition select(SelectItem... selectItems) {
		return _owner.select(selectItems);
	}

	public QueryDefinition select(List<SelectItem> selectItemList) {
		return _owner.select(selectItemList);
	}

	public QueryDefinition selectDistinct(SelectItem... selectItems) {
		return _owner.selectDistinct(selectItems);
	}

	public QueryDefinition selectDistinct(List<SelectItem> selectItemList) {
		return _owner.selectDistinct(selectItemList);
	}

	public CaseExpression simpleCase(Expression caseOperand) {
		return _owner.simpleCase(caseOperand);
	}

	public CaseExpression simpleCase(Number caseOperand) {
		return _owner.simpleCase(caseOperand);
	}

	public CaseExpression simpleCase(String caseOperand) {
		return _owner.simpleCase(caseOperand);
	}

	public CaseExpression simpleCase(Date caseOperand) {
		return _owner.simpleCase(caseOperand);
	}

	public CaseExpression simpleCase(Calendar caseOperand) {
		return _owner.simpleCase(caseOperand);
	}

	public CaseExpression simpleCase(Class caseOperand) {
		return _owner.simpleCase(caseOperand);
	}

	public CaseExpression simpleCase(Enum<?> caseOperand) {
		return _owner.simpleCase(caseOperand);
	}

	public QueryDefinition where(Predicate predicate) {
		return _owner.where(predicate);
	}
}
