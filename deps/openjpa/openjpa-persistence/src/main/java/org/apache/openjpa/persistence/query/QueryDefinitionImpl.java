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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.openjpa.lib.util.Localizer;

/**
 * Implements QueryDefinition.
 * 
 * @author Pinaki Poddar
 *
 */
public class QueryDefinitionImpl extends ExpressionImpl 
    implements QueryDefinition, Expression  {
	private final QueryBuilderImpl _builder;
	private List<AbstractDomainObject> _domains;
	private List<PathExpression> _groupBys;
	private List<OrderableItem> _orderBys;
	private List<SelectItem>  _projections;
	private boolean  _distinct;
	private Predicate _where;
	private Predicate _having;
	
	private static enum Visit {PROJECTION, EXPRESSION, JOINABLE};
	
	protected static Localizer _loc = 
		Localizer.forPackage(QueryDefinitionImpl.class);
	
	/**
	 * 
	 * @param builder
	 */
	protected QueryDefinitionImpl(QueryBuilderImpl builder) {
		_builder = builder;
	}
	
	/**
	 * Root domain object has no parent, no path but a non-null Class.
	 */
	public DomainObject addRoot(Class cls) {
		RootPath root = new RootPath(this, cls);
		addDomain(root);
		return root;
	}
	
	public DomainObject addSubqueryRoot(PathExpression path) {
		AbstractPath impl = (AbstractPath)path;
		LinkedList<AbstractPath> paths = impl.split();
		QueryDefinitionImpl owner = impl.getOwner();
		int i = 0;
		while (i < paths.size() && owner.hasDomain(paths.get(i))) {
			i++;
		}
		
		AbstractPath next = paths.get(i);
		DomainObject newRoot = new NavigationPath(this, 
                next.getParent(), next.getLastSegment().toString());
		addDomain((AbstractDomainObject)newRoot);
		i++;
		for (; i < paths.size(); i++) {
			next = paths.get(i);
            newRoot = newRoot.join(next.getLastSegment().toString());
		}
		return newRoot;
	}
	
	boolean hasDomain(PathExpression path) {
		return _domains != null && _domains.contains(path);
	}
	
	protected <T extends AbstractDomainObject> T addDomain(T path) {
		if (_domains == null)
			_domains = new ArrayList<AbstractDomainObject>();
		_domains.add(path);
		return path;
	}

	public Subquery all() {
		return new AllExpression(this);
	}

	public Subquery any() {
		return new AnyExpression(this);
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
		return new CurrentTimeExpression(Date.class);
	}

	public Expression currentTime() {
		return new CurrentTimeExpression(Time.class);
	}

	public Expression currentTimestamp() {
		return new CurrentTimeExpression(Timestamp.class);
	}

	public Predicate exists() {
		return new ExistsExpression(this);
	}

	public CaseExpression generalCase() {
		return new CaseExpressionImpl();
	}

	public QueryDefinition groupBy(PathExpression... pathExprs) {
		if (_groupBys == null) {
			_groupBys = new ArrayList<PathExpression>();
		} else {
			_groupBys.clear();
		}
		for (PathExpression e : pathExprs)
			_groupBys.add(e);
		return this;
	}

	public QueryDefinition groupBy(List<PathExpression> pathExprList) {
		if (_groupBys == null) {
			_groupBys = new ArrayList<PathExpression>();
		} else {
			_groupBys.clear();
		}
		for (PathExpression e : pathExprList)
			_groupBys.add(e);
		return this;
	}

	public QueryDefinition having(Predicate predicate) {
		_having = predicate;
		return this;
	}

	public Expression literal(String s) {
		return new LiteralExpression(s);
	}

	public Expression literal(Number n) {
		return new LiteralExpression(n);
	}

	public Expression literal(boolean b) {
		return new LiteralExpression(b);
	}

	public Expression literal(Calendar c) {
		return new LiteralExpression(c);
	}

	public Expression literal(Date d) {
		return new LiteralExpression(d);
	}

	public Expression literal(char c) {
		return new LiteralExpression(c);
	}

	public Expression literal(Class cls) {
		return new LiteralExpression(cls);
	}

	public Expression literal(Enum<?> e) {
		return new LiteralExpression(e);
	}

	public Expression nullLiteral() {
		return new LiteralExpression(null);
	}

	public SelectItem newInstance(Class cls, SelectItem... args) {
		return new NewInstance(cls, args);
	}

	public Expression nullif(Expression exp1, Expression exp2) {
		throw new UnsupportedOperationException();
	}

	public Expression nullif(Number arg1, Number arg2) {
		throw new UnsupportedOperationException();
	}

	public Expression nullif(String arg1, String arg2) {
		throw new UnsupportedOperationException();
	}

	public Expression nullif(Date arg1, Date arg2) {
		throw new UnsupportedOperationException();
	}

	public Expression nullif(Calendar arg1, Calendar arg2) {
		throw new UnsupportedOperationException();
	}

	public Expression nullif(Class arg1, Class arg2) {
		throw new UnsupportedOperationException();
	}

	public Expression nullif(Enum<?> arg1, Enum<?> arg2) {
		throw new UnsupportedOperationException();
	}

	public QueryDefinition orderBy(OrderByItem... orderByItems) {
		if (_orderBys == null)
			_orderBys = new ArrayList<OrderableItem>();
		else
			_orderBys.clear();
		for (OrderByItem i : orderByItems) {
			if (i instanceof OrderableItem)
				_orderBys.add((OrderableItem)i);
			else
                _orderBys.add(new OrderableItem((ExpressionImpl)i));
		}
		return this;
	}

	public QueryDefinition orderBy(List<OrderByItem> orderByItemList) {
		if (_orderBys == null)
			_orderBys = new ArrayList<OrderableItem>();
		else
			_orderBys.clear();
		for (OrderByItem i : orderByItemList) {
			if (i instanceof OrderableItem)
				_orderBys.add((OrderableItem)i);
			else
                _orderBys.add(new OrderableItem((ExpressionImpl)i, null));
		}
		return this;
	}

	public Expression param(String name) {
		return new ParameterExpression(name);
	}

	public Predicate predicate(boolean b) {
		return null;
	}

	public QueryDefinition select(SelectItem... items) {
        return select(items == null ? null : Arrays.asList(items), false);
	}

	public QueryDefinition select(List<SelectItem> items) {
		return select(items, false);
	}

	public QueryDefinition selectDistinct(SelectItem... items) {
        return select(items == null ? null : Arrays.asList(items), true);
	}

	public QueryDefinition selectDistinct(List<SelectItem> items) {
		return select(items, true);
	}
	
    private QueryDefinition select(List<SelectItem> items, boolean isDistinct) {
		if (_projections == null) {
			_projections = new ArrayList<SelectItem>();
		} else {
			_projections.clear();
		}
		_distinct = isDistinct;
		for (SelectItem item : items)
			_projections.add(item);
		return this;
	}

	public CaseExpression simpleCase(Expression caseOperand) {
		return new CaseExpressionImpl(caseOperand);
	}

	public CaseExpression simpleCase(Number caseOperand) {
		return new CaseExpressionImpl(caseOperand);
	}

	public CaseExpression simpleCase(String caseOperand) {
		return new CaseExpressionImpl(caseOperand);
	}

	public CaseExpression simpleCase(Date caseOperand) {
		return new CaseExpressionImpl(caseOperand);
	}

	public CaseExpression simpleCase(Calendar caseOperand) {
		return new CaseExpressionImpl(caseOperand);
	}

	public CaseExpression simpleCase(Class caseOperand) {
		return new CaseExpressionImpl(caseOperand);
	}

	public CaseExpression simpleCase(Enum<?> caseOperand) {
		return new CaseExpressionImpl(caseOperand);
	}

	public Subquery some() {
		return new SomeExpression(this);
	}

	public QueryDefinition where(Predicate predicate) {
		_where = predicate;
		return this;
	}
	
	private List<SelectItem> getProjections() {
		if (_projections == null) {
            List<SelectItem> defaultProjection = new ArrayList<SelectItem>();
			defaultProjection.add(_domains.get(0));
			return defaultProjection;
		}
		return _projections;
	}

	@Override
	public String asExpression(AliasContext ctx) {
		ctx.push(this);
		StringBuilder buffer = new StringBuilder();
		registerDomains(ctx);
		String select = _distinct ? "SELECT DISTINCT " : "SELECT ";
        fillBuffer(select, buffer, ctx, getProjections(), Visit.PROJECTION);
		fillBuffer(" FROM ", buffer, ctx, _domains, Visit.JOINABLE);
		fillBuffer(" WHERE ", buffer, ctx, _where);
        fillBuffer(" GROUP BY ", buffer, ctx, _groupBys, Visit.EXPRESSION);
		fillBuffer(" HAVING ", buffer, ctx, _having);
        fillBuffer(" ORDER BY ", buffer, ctx, _orderBys, Visit.EXPRESSION);
		
		return buffer.toString();
	}
	
	public String asProjection(AliasContext ctx) {
		return asExpression(ctx);
	}
	
    public void fillBuffer(String header, StringBuilder buffer, AliasContext ctx,
		List list, Visit visit) {
		if (list == null || list.isEmpty())
			return;
		buffer.append(header);
		for (int i = 0; i < list.size(); i++) {
			Visitable v = (Visitable)list.get(i);
			switch(visit) {
			case PROJECTION : buffer.append(v.asProjection(ctx))
                       .append(i != list.size()-1 ? ", " : " ");
				break;
			case EXPRESSION : buffer.append(v.asExpression(ctx))
                        .append(i != list.size()-1 ? ", " : " ");
				break;
            case JOINABLE   : buffer.append(i > 0 && v instanceof RootPath ? 
                        ", " : " ").append(v.asJoinable(ctx));
				break;
			}
		}
	}
	
    public void fillBuffer(String header, StringBuilder buffer, AliasContext ctx,
			Predicate p) {
		if (p == null)
			return;
		Visitable v = (Visitable)p;
		buffer.append(header);
		buffer.append(v.asExpression(ctx));
	}
	
	/**
     * Registers each domain with an alias. Also set alias for order by items
	 * that are projected.
	 */
	private void registerDomains(AliasContext ctx) {
		if (_domains != null) {
			Collections.sort(_domains, new DomainSorter());
			for (AbstractDomainObject domain : _domains) {
				ctx.setAlias(domain);
			}
		}
		if (_orderBys != null) {
			for (OrderableItem o : _orderBys) {
				ExpressionImpl e = o.getExpression();
                if (_projections != null && _projections.contains(e))
					ctx.setAlias(e);
			}
		}
	}
	
	static class DomainSorter implements Comparator<AbstractDomainObject> {
		static List<Class> _order = Arrays.asList(new Class[] {
                RootPath.class, NavigationPath.class, OperatorPath.class, 
				JoinPath.class, FetchPath.class, } );
		
        public int compare(AbstractDomainObject a, AbstractDomainObject b) {
            return _order.indexOf(a.getClass()) - _order.indexOf(b.getClass());
		}
	}
}
