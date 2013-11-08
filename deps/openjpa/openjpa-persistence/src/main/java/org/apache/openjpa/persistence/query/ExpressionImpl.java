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

/**
 * An abstract expression acts as a factory for concrete unary or binary 
 * expressions such as ABS() or PLUS().
 * 
 * @author Pinaki Poddar
 *
 */
abstract class ExpressionImpl extends AbstractVisitable 
   implements Expression, Visitable {
	
	public Expression abs() {
		return new AbsExpression(this);
	}

	public Expression concat(String... str) {
		ConstantExpression[] exprs = new ConstantExpression[str.length];
		for (int i = 0; i < str.length; i++)
			exprs[i] = new ConstantExpression(str[i]);
		return new ConcatExpression(new VarArgsExpression(exprs));
	}

	public Expression concat(Expression... exprs) {
		return new ConcatExpression(new VarArgsExpression(exprs));
	}

	public Expression dividedBy(Number num) {
        return new DividedByExpression(this, new ConstantExpression(num));
	}

	public Expression dividedBy(Expression expr) {
		return new DividedByExpression(this, expr);
	}

	public Predicate in(String... strings) {
		return new InExpression(this, new ArrayExpression(strings));
	}

	public Predicate in(Number... nums) {
		return new InExpression(this, new ArrayExpression(nums));
	}

	public Predicate in(Enum<?>... enums) {
		return new InExpression(this, new ArrayExpression(enums));
	}

	public Predicate in(Class... classes) {
		return new InExpression(this, new ArrayExpression(classes));
	}

	public Predicate in(Expression... params) {
		return new InExpression(this, new ArrayExpression(params));
	}

	public Predicate in(Subquery subquery) {
		return new InExpression(this, (Expression)subquery);
	}

	public Predicate isNull() {
		return new IsNullExpression(this);
	}

	public Expression length() {
		return new LengthExpression(this);
	}

	public Expression locate(String str) {
		return new LocateExpression(this, str, 0);
	}

	public Expression locate(Expression expr) {
		return new LocateExpression(this, expr, 1);
	}

	public Expression locate(String str, int position) {
		return new LocateExpression(this, str, position);
	}

	public Expression locate(String str, Expression position) {
		return new LocateExpression(this, str, position);
	}

	public Expression locate(Expression str, int position) {
		return new LocateExpression(this, str, position);
	}

	public Expression locate(Expression str, Expression position) {
		return new LocateExpression(this, str, position);
	}

	public Expression lower() {
		return new LowerExpression(this);
	}

	public Predicate member(PathExpression arg) {
		return new MemberOfExpression(this, arg);
	}

	public Expression minus() {
		return new UnaryMinusExpression(this);
	}

	public Expression minus(Number num) {
		return new MinusExpression(this, new ConstantExpression(num));
	}

	public Expression minus(Expression expr) {
		return new MinusExpression(this, expr);
	}

	public Expression mod(int num) {
		return new ModExpression(this, new ConstantExpression(num));
	}

	public Expression mod(Expression expr) {
		return new MinusExpression(this, expr);
	}

	public Expression plus(Number num) {
		return new PlusExpression(this, new ConstantExpression(num));
	}

	public Expression plus(Expression expr) {
		return new PlusExpression(this, expr);
	}

	public Expression sqrt() {
		return new SquareRootExpression(this);
	}

	public Expression substring(int start) {
		return new SubStringExpression(this, start);
	}

	public Expression substring(Expression start) {
		return new SubStringExpression(this, start);
	}

	public Expression substring(int start, int len) {
		return new SubStringExpression(this, start, len);
	}

	public Expression substring(int start, Expression len) {
        return new SubStringExpression(this, new ConstantExpression(start),
                len);
	}

	public Expression substring(Expression start, int len) {
        return new SubStringExpression(this, start,
                new ConstantExpression(len));
	}

	public Expression substring(Expression start, Expression len) {
		return new SubStringExpression(this, start, len);
	}

	public Expression times(Number num) {
		return new TimesExpression(this, new ConstantExpression(num));
	}

	public Expression times(Expression expr) {
		return new TimesExpression(this, expr);
	}

	public Expression trim() {
		return new TrimExpression(this, null, null);
	}

	public Expression trim(TrimSpec spec) {
		return new TrimExpression(this, null, spec);
	}

	public Expression trim(char c) {
		return new TrimExpression(this, c, null);
	}

	public Expression trim(char c, TrimSpec spec) {
		return new TrimExpression(this, c, spec);
	}

	public Expression trim(Expression expr) {
		return new TrimExpression(this, expr, null);
	}

	public Expression trim(Expression expr, TrimSpec spec) {
		return new TrimExpression(this, expr, spec);
	}

	public Expression upper() {
		return new UpperExpression(this);
	}

	public OrderByItem asc() {
		return new OrderableItem(this, true);
	}

	public OrderByItem desc() {
		return new OrderableItem(this, false);
	}

	public Predicate between(PredicateOperand arg1, PredicateOperand arg2) {
		return new BetweenExpression(this, new RangeExpression(
			(Expression)arg1, (Expression)arg2));
	}

	public Predicate between(PredicateOperand arg1, Number arg2) {
		return new BetweenExpression(this, new RangeExpression(
			(Expression)arg1, new ConstantExpression(arg2)));
	}

	public Predicate between(Number arg1, PredicateOperand arg2) {
		return new BetweenExpression(this, new RangeExpression(
			new ConstantExpression(arg1), (Expression)arg2));
	}

	public Predicate between(Number arg1, Number arg2) {
		return new BetweenExpression(this, new RangeExpression(
            new ConstantExpression(arg1), new ConstantExpression(arg2)));
	}

	public Predicate between(PredicateOperand arg1, String arg2) {
        return new BetweenExpression(this, new RangeExpression((Expression)arg1,
			new ConstantExpression(arg2)));
	}

	public Predicate between(String arg1, PredicateOperand arg2) {
		return new BetweenExpression(this, new RangeExpression(
			new ConstantExpression(arg1), (Expression)arg2));
	}

	public Predicate between(String arg1, String arg2) {
		return new BetweenExpression(this, new RangeExpression(
            new ConstantExpression(arg1), new ConstantExpression(arg2)));
	}

	public Predicate between(PredicateOperand arg1, Date arg2) {
		return new BetweenExpression(this, new RangeExpression(
            (Expression)arg1, new ConstantExpression(arg2)));
	}

	public Predicate between(Date arg1, PredicateOperand arg2) {
		return new BetweenExpression(this, new RangeExpression(
            new ConstantExpression(arg1), (Expression)arg2));
	}

	public Predicate between(Date arg1, Date arg2) {
		return new BetweenExpression(this, new RangeExpression(
            new ConstantExpression(arg1), new ConstantExpression(arg2)));
	}

	public Predicate between(PredicateOperand arg1, Calendar arg2) {
		return new BetweenExpression(this, new RangeExpression(
			(Expression)arg1, new ConstantExpression(arg2)));
	}

	public Predicate between(Calendar arg1, PredicateOperand arg2) {
		return new BetweenExpression(this, new RangeExpression(
			new ConstantExpression(arg1), (Expression)arg2));
	}

	public Predicate between(Calendar arg1, Calendar arg2) {
		return new BetweenExpression(this, new RangeExpression(
            new ConstantExpression(arg1), new ConstantExpression(arg2)));
	}

	public Predicate equal(PredicateOperand arg) {
		return new EqualExpression(this, (Expression)arg);
	}

	public Predicate equal(Class cls) {
		return new EqualExpression(this, new ConstantExpression(cls));
	}

	public Predicate equal(Number arg) {
		return new EqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate equal(String arg) {
		return new EqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate equal(boolean arg) {
		return new EqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate equal(Date arg) {
		return new EqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate equal(Calendar arg) {
		return new EqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate equal(Enum<?> e) {
		return new EqualExpression(this, new ConstantExpression(e));
	}

	public Predicate greaterEqual(PredicateOperand arg) {
        return new GreaterEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate greaterEqual(Number arg) {
        return new GreaterEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate greaterEqual(String arg) {
        return new GreaterEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate greaterEqual(Date arg) {
        return new GreaterEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate greaterEqual(Calendar arg) {
        return new GreaterEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate greaterThan(PredicateOperand arg) {
        return new GreaterThanExpression(this, (Expression)arg);
	}

	public Predicate greaterThan(Number arg) {
        return new GreaterThanExpression(this, new ConstantExpression(arg));
	}

	public Predicate greaterThan(String arg) {
        return new GreaterThanExpression(this, new ConstantExpression(arg));
	}

	public Predicate greaterThan(Date arg) {
        return new GreaterThanExpression(this, new ConstantExpression(arg));
	}

	public Predicate greaterThan(Calendar arg) {
        return new GreaterThanExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessEqual(PredicateOperand arg) {
        return new LessEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessEqual(Number arg) {
        return new LessEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessEqual(String arg) {
        return new LessEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessEqual(Date arg) {
        return new LessEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessEqual(Calendar arg) {
        return new LessEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessThan(PredicateOperand arg) {
        return new LessThanExpression(this, (Expression)arg);
	}

	public Predicate lessThan(Number arg) {
        return new LessThanExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessThan(String arg) {
        return new LessThanExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessThan(Date arg) {
        return new LessThanExpression(this, new ConstantExpression(arg));
	}

	public Predicate lessThan(Calendar arg) {
        return new LessThanExpression(this, new ConstantExpression(arg));
	}

	public Predicate like(PredicateOperand pattern) {
		return new LikeExpression(this, (Expression)pattern);
	}

    public Predicate like(PredicateOperand pattern, PredicateOperand escChar) {
		return new LikeExpression(this, (Expression)pattern, escChar);
	}

	public Predicate like(PredicateOperand pattern, char escapeChar) {
        return new LikeExpression(this, (Expression)pattern, escapeChar);
	}

	public Predicate like(String pattern) {
        return new LikeExpression(this, new ConstantExpression(pattern));
	}

	public Predicate like(String pattern, PredicateOperand escapeChar) {
        return new LikeExpression(this, new ConstantExpression(pattern), 
			escapeChar);
	}

	public Predicate like(String pattern, char escChar) {
        return new LikeExpression(this, new ConstantExpression(pattern), 
			escChar);
	}

	public Predicate notEqual(PredicateOperand arg) {
		return new NotEqualExpression(this, (Expression)arg);
	}

	public Predicate notEqual(Class cls) {
        return new NotEqualExpression(this, new ConstantExpression(cls));
	}

	public Predicate notEqual(Number arg) {
        return new NotEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate notEqual(String arg) {
        return new NotEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate notEqual(boolean arg) {
        return new NotEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate notEqual(Date arg) {
        return new NotEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate notEqual(Calendar arg) {
        return new NotEqualExpression(this, new ConstantExpression(arg));
	}

	public Predicate notEqual(Enum<?> e) {
		return new NotEqualExpression(this, new ConstantExpression(e));
	}
	
	//
	// Visitable/Selectable implementation
	//
	public String getAliasHint(AliasContext ctx) {
		return "o";
	}
}
