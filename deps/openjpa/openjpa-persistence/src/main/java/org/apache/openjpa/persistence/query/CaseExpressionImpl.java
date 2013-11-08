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
import java.util.LinkedList;

public class CaseExpressionImpl implements CaseExpression, Visitable {
	private LinkedList<WhenClause> _whens = new LinkedList<WhenClause>();
	private final Object _caseOperand;
	
	public CaseExpressionImpl() {
		this(null);
	}
	
	public CaseExpressionImpl(Object caseOperand) {
		_caseOperand = caseOperand;
	}
	
	public Expression elseCase(Expression arg) {
		return new ElseExpression(this, arg);
	}

	public Expression elseCase(String arg) {
		return new ElseExpression(this, new ConstantExpression(arg));
	}

	public Expression elseCase(Number arg) {
		return new ElseExpression(this, new ConstantExpression(arg));
	}

	public Expression elseCase(Date arg) {
		return new ElseExpression(this, new ConstantExpression(arg));
	}

	public Expression elseCase(Calendar arg) {
		return new ElseExpression(this, new ConstantExpression(arg));
	}

	public Expression elseCase(Class arg) {
		return new ElseExpression(this, new ConstantExpression(arg));
	}

	public Expression elseCase(Enum<?> arg) {
		return new ElseExpression(this, new ConstantExpression(arg));
	}

	public CaseExpression then(Expression then) {
		assertThenState();
		_whens.getLast().setThen(then);
		return this;
	}

	public CaseExpression then(Number then) {
		assertThenState();
		_whens.getLast().setThen(then);
		return this;
	}

	public CaseExpression then(String then) {
		assertThenState();
		_whens.getLast().setThen(then);
		return this;
	}

	public CaseExpression then(Date then) {
		assertThenState();
		_whens.getLast().setThen(then);
		return this;
	}

	public CaseExpression then(Calendar then) {
		assertThenState();
		_whens.getLast().setThen(then);
		return this;
	}

	public CaseExpression then(Class then) {
		assertThenState();
		_whens.getLast().setThen(then);
		return this;
	}

	public CaseExpression then(Enum<?> then) {
		assertThenState();
		_whens.getLast().setThen(then);
		return this;
	}

	public CaseExpression when(Predicate when) {
		assertWhenState();
		WhenClause clause = new WhenClause(when);
		_whens.add(clause);
		return this;
	}

	public CaseExpression when(Expression when) {
		assertWhenState();
		WhenClause clause = new WhenClause(when);
		_whens.add(clause);
		return this;
	}

	public CaseExpression when(Number when) {
		assertWhenState();
		WhenClause clause = new WhenClause(when);
		_whens.add(clause);
		return this;
	}

	public CaseExpression when(String when) {
		assertWhenState();
		WhenClause clause = new WhenClause(when);
		_whens.add(clause);
		return this;
	}

	public CaseExpression when(Date when) {
		assertWhenState();
		WhenClause clause = new WhenClause(when);
		_whens.add(clause);
		return this;
	}

	public CaseExpression when(Calendar when) {
		assertWhenState();
		WhenClause clause = new WhenClause(when);
		_whens.add(clause);
		return this;
	}

	public CaseExpression when(Class when) {
		assertWhenState();
		WhenClause clause = new WhenClause(when);
		_whens.add(clause);
		return this;
	}

	public CaseExpression when(Enum<?> when) {
		assertWhenState();
		WhenClause clause = new WhenClause(when);
		_whens.add(clause);
		return this;
	}
	
	void assertWhenState() {
		boolean ok = _whens.isEmpty() || _whens.getLast().hasThen();
		if (!ok)
            throw new IllegalStateException("when() can not be called now");
	}
	
	void assertThenState() {
		boolean ok = !_whens.isEmpty() && !_whens.getLast().hasThen();
		if (!ok)
            throw new IllegalStateException("then() can not be called now");
	}
	
	public String asExpression(AliasContext ctx) {
		StringBuffer tmp = new StringBuffer("CASE ");
		if (_caseOperand != null) {
			tmp.append(toJPQL(ctx, _caseOperand));
		}
		for (WhenClause when : _whens) {
			tmp.append(when.toJPQL(ctx));
		}
		return tmp.toString();
	}
	
	public String asProjection(AliasContext ctx) {
		return asExpression(ctx);
	}
	
	public String asJoinable(AliasContext ctx) {
		throw new UnsupportedOperationException();
	}
	
	public String getAliasHint(AliasContext ctx) {
		throw new UnsupportedOperationException();
	}
	
	

	String toJPQL(AliasContext ctx, Object o) {
		if (o instanceof Visitable) {
			return ((Visitable)o).asExpression(ctx);
		}
		return o.toString();
	}
}
