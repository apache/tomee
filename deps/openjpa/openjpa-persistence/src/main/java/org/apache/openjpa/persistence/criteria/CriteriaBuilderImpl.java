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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.apache.openjpa.kernel.ExpressionStoreQuery;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.meta.MetamodelImpl;

/**
 * Factory for Criteria query expressions.
 * 
 * Acts as an adapter to OpenJPA ExpressionFactory.
 * 
 * @author Pinaki Poddar
 * @author Fay Wang
 * 
 * @since 2.0.0
 *
 */
@SuppressWarnings("serial")
public class CriteriaBuilderImpl implements OpenJPACriteriaBuilder, ExpressionParser {

    private MetamodelImpl _model;

    public OpenJPACriteriaBuilder setMetaModel(MetamodelImpl model) {
        _model = model;
        return this;
    }
    
    public Metamodel getMetamodel() {
        return _model;
    }

    public QueryExpressions eval(Object parsed, ExpressionStoreQuery query,
        ExpressionFactory factory, ClassMetaData candidate) {
        CriteriaQueryImpl<?> c = (CriteriaQueryImpl<?>) parsed;
        return c.getQueryExpressions(factory);
    }
    
    public Value[] eval(String[] vals, ExpressionStoreQuery query,
        ExpressionFactory factory, ClassMetaData candidate) {
        return null;
    }

    public String getLanguage() {
        return LANG_CRITERIA;
    }
    
    /**
     *  Create a Criteria query object with the specified result type.
     *  @param resultClass  type of the query result
     *  @return query object
     */
    public <T> OpenJPACriteriaQuery<T> createQuery(Class<T> resultClass) {
        return new CriteriaQueryImpl<T>(_model, resultClass);
    }

    /**
     *  Create a Criteria query object that returns a tuple of 
     *  objects as its result.
     *  @return query object
     */
    public OpenJPACriteriaQuery<Tuple> createTupleQuery() {
        return new CriteriaQueryImpl<Tuple>(_model, Tuple.class);
    }

    public Object parse(String ql, ExpressionStoreQuery query) {
        throw new AbstractMethodError();
    }

    public void populate(Object parsed, ExpressionStoreQuery query) {
        CriteriaQueryImpl<?> c = (CriteriaQueryImpl<?>) parsed;
        query.invalidateCompilation();
        query.getContext().setCandidateType(c.getRoot().getJavaType(), true);
        query.setQuery(parsed);
    }

    public <N extends Number> Expression<N> abs(Expression<N> x) {
        return new Expressions.Abs<N>(x);
    }

    public <Y> Expression<Y> all(Subquery<Y> subquery) {
        return new Expressions.All<Y>(subquery);
    }

    public Predicate and(Predicate... restrictions) {
    	return new PredicateImpl.And(restrictions);
    }

    public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
    	return new PredicateImpl.And(x,y);
    }

    public <Y> Expression<Y> any(Subquery<Y> subquery) {
        return new Expressions.Any<Y>(subquery);
    }

    public Order asc(Expression<?> x) {
        return new OrderImpl(x, true);
    }

    public <N extends Number> Expression<Double> avg(Expression<N> x) {
        return new Expressions.Avg(x);
    }

    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Expression<? extends Y> x,
        Expression<? extends Y> y) {
        return new Expressions.Between(v,x,y);
    }

    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Y x, Y y) {
        return new Expressions.Between(v,x,y);
    }

    public <T> Coalesce<T> coalesce() {
        return new Expressions.Coalesce(Object.class);
    }

    public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Expression<? extends Y> y) {
    	return new Expressions.Coalesce(x.getJavaType()).value(x).value(y);
    }

    public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Y y) {
    	return new Expressions.Coalesce(x.getJavaType()).value(x).value(y);
   }

    public Expression<String> concat(Expression<String> x, Expression<String> y) {
    	return new Expressions.Concat(x, y);
    }

    public Expression<String> concat(Expression<String> x, String y) {
    	return new Expressions.Concat(x, y);
    }

    public Expression<String> concat(String x, Expression<String> y) {
    	return new Expressions.Concat(x, y);
    }

    public Predicate conjunction() {
        return new PredicateImpl.And();
    }

    public Expression<Long> count(Expression<?> x) {
        return new Expressions.Count(x);
    }

    public Expression<Long> countDistinct(Expression<?> x) {
        return new Expressions.Count(x, true);
    }

    public OpenJPACriteriaQuery<Object> createQuery() {
        return new CriteriaQueryImpl<Object>(_model, Object.class);
    }

    public Expression<Date> currentDate() {
    	return new Expressions.CurrentDate();
    }

    public Expression<Time> currentTime() {
    	return new Expressions.CurrentTime();
    }

    public Expression<Timestamp> currentTimestamp() {
    	return new Expressions.CurrentTimestamp();
    }

    public Order desc(Expression<?> x) {
    	return new OrderImpl(x, false);
    }

    public <N extends Number> Expression<N> diff(Expression<? extends N> x,
        Expression<? extends N> y) {
        return new Expressions.Diff<N>(x, y);
    }

    public <N extends Number> Expression<N> diff(
        Expression<? extends N> x, N y) {
        return new Expressions.Diff<N>(x, y);
    }

    public <N extends Number> Expression<N> diff(N x, 
        Expression<? extends N> y) {
        return new Expressions.Diff<N>(x, y);
    }

    public Predicate disjunction() {
        return new PredicateImpl.Or();
    }

    public Predicate equal(Expression<?> x, Expression<?> y) {
        if (y == null)
            return new Expressions.IsNull((ExpressionImpl<?> )x);
        return new Expressions.Equal(x, y);
    }

    public Predicate equal(Expression<?> x, Object y) {
        if (y == null)
            return new Expressions.IsNull((ExpressionImpl<?> )x);
        return new Expressions.Equal(x, y);
    }

    public Predicate exists(Subquery<?> subquery) {
        return new Expressions.Exists(subquery);
    }

    public <T> Expression<T> function(String name, Class<T> type,
        Expression<?>... args) {
        return new Expressions.DatabaseFunction(name, type, args);
    }

    public Predicate ge(Expression<? extends Number> x,
        Expression<? extends Number> y) {
        return new Expressions.GreaterThanEqual(x,y);
    }

    public Predicate ge(Expression<? extends Number> x, Number y) {
        return new Expressions.GreaterThanEqual(x,y);
    }

    public <Y extends Comparable<? super Y>> Predicate greaterThan(
        Expression<? extends Y> x, Expression<? extends Y> y) {
        return new Expressions.GreaterThan(x,y);
    }

    public <Y extends Comparable<? super Y>> Predicate greaterThan(
        Expression<? extends Y> x, Y y) {
        return new Expressions.GreaterThan(x, y);
    }

    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(
        Expression<? extends Y> x, Expression<? extends Y> y) {
        return new Expressions.GreaterThanEqual(x,y);
    }

    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(
        Expression<? extends Y> x, Y y) {
        return new Expressions.GreaterThanEqual(x,y);
    }

    public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x) {
    	return new Expressions.Max<X>(x);
    }

    public Predicate gt(Expression<? extends Number> x,
        Expression<? extends Number> y) {
        return new Expressions.GreaterThan(x,y);
    }

    public Predicate gt(Expression<? extends Number> x, Number y) {
        return new Expressions.GreaterThan(x,y);
    }

    public <T> In<T> in(Expression<? extends T> expression) {
        return new Expressions.In<T>(expression);
    }

    public <C extends Collection<?>> Predicate isEmpty(Expression<C> collection) {
        return new Expressions.IsEmpty(collection);
    }

    public Predicate isFalse(Expression<Boolean> x) {
        return new Expressions.Equal(x, false);
    }

    public <E, C extends Collection<E>> Predicate isMember(E e, Expression<C> c) {
        return new Expressions.IsMember<E>(e, c);
    }

    public <E, C extends Collection<E>> Predicate isMember(Expression<E> e, Expression<C> c) {
        return new Expressions.IsMember<E>(e, c);
    }

    public <C extends Collection<?>> Predicate isNotEmpty(Expression<C> collection) {
        return new Expressions.IsNotEmpty(collection);
    }

    public <E, C extends Collection<E>> Predicate isNotMember(E e, Expression<C> c) {
        return isMember(e, c).not();
    }

    public <E, C extends Collection<E>> Predicate isNotMember(Expression<E> e, Expression<C> c) {
        return isMember(e, c).not();
    }

    public Predicate isTrue(Expression<Boolean> x) {
        if (x instanceof PredicateImpl) {
            PredicateImpl predicate = (PredicateImpl)x; 
            if (predicate.isEmpty()) {
                return predicate.getOperator() == BooleanOperator.AND ? PredicateImpl.TRUE() : PredicateImpl.FALSE();
            }
        }
        return new Expressions.Equal(x, true);
    }

    public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map) {
        return new Expressions.Constant<Set<K>>(map == null ? Collections.EMPTY_SET : map.keySet());
    }

    public Predicate le(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new Expressions.LessThanEqual(x,y);
    }

    public Predicate le(Expression<? extends Number> x, Number y) {
        return new Expressions.LessThanEqual(x,y);
    }

    public <X extends Comparable<? super X>> Expression<X> least(Expression<X> x) {
        return new Expressions.Min<X>(x);
    }

    public Expression<Integer> length(Expression<String> x) {
        return new Expressions.Length(x);

    }

    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return new Expressions.LessThan(x,y);
    }

    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Y y) {
        return new Expressions.LessThan(x,y);

    }

    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, 
        Expression<? extends Y> y) {
        return new Expressions.LessThanEqual(x,y);
    }

    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return new Expressions.LessThanEqual(x,y);
    }

    public Predicate like(Expression<String> x, Expression<String> pattern) {
        return new Expressions.Like(x,pattern);
    }

    public Predicate like(Expression<String> x, String pattern) {
        return new Expressions.Like(x,pattern);
    }

    public Predicate like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
        return new Expressions.Like(x,pattern,escapeChar);
    }

    public Predicate like(Expression<String> x, Expression<String> pattern, char escapeChar) {
        return new Expressions.Like(x,pattern,escapeChar);
    }

    public Predicate like(Expression<String> x, String pattern, Expression<Character> escapeChar) {
        return new Expressions.Like(x,pattern,escapeChar);
    }

    public Predicate like(Expression<String> x, String pattern, char escapeChar) {
        return new Expressions.Like(x,pattern,escapeChar);
    }

    public <T> Expression<T> literal(T value) {
        if (Boolean.TRUE.equals(value))
            return (Expression<T>)PredicateImpl.TRUE();
        if (Boolean.FALSE.equals(value))
            return (Expression<T>)PredicateImpl.FALSE();
        return new Expressions.Constant<T>(value);
    }

    public Expression<Integer> locate(Expression<String> x, Expression<String> pattern) {
        return new Expressions.Locate(x, pattern);
    }

    public Expression<Integer> locate(Expression<String> x, String pattern) {
        return new Expressions.Locate(x, pattern);

    }

    public Expression<Integer> locate(Expression<String> x, Expression<String> pattern, Expression<Integer> from) {
        return new Expressions.Locate(x, pattern, from);

    }

    public Expression<Integer> locate(Expression<String> x, String pattern, int from) {
        return new Expressions.Locate(x, pattern, from);

    }

    public Expression<String> lower(Expression<String> x) {
        return new Expressions.Lower(x);

    }

    public Predicate lt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new Expressions.LessThan(x,y);
    }

    public Predicate lt(Expression<? extends Number> x, Number y) {
        return new Expressions.LessThan(x,y);
    }

    public <N extends Number> Expression<N> max(Expression<N> x) {
        return new Expressions.Max<N>(x);
    }

    public <N extends Number> Expression<N> min(Expression<N> x) {
        return new Expressions.Min<N>(x);
    }

    public Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y) {
        return new Expressions.Mod(x,y);
    }

    public Expression<Integer> mod(Expression<Integer> x, Integer y) {
        return new Expressions.Mod(x,y);
    }

    public Expression<Integer> mod(Integer x, Expression<Integer> y) {
        return new Expressions.Mod(x,y);
    }

    public <N extends Number> Expression<N> neg(Expression<N> x) {
        return new Expressions.Diff<N>(0, x);
    }

    public Predicate not(Expression<Boolean> restriction) {
        return ((Predicate)restriction).not();
    }

    public Predicate notEqual(Expression<?> x, Expression<?> y) {
        return new Expressions.NotEqual(x, y);
    }

    public Predicate notEqual(Expression<?> x, Object y) {
        return new Expressions.NotEqual(x, y);
    }

    public Predicate notLike(Expression<String> x, Expression<String> pattern) {
        return like(x, pattern).not();
    }

    public Predicate notLike(Expression<String> x, String pattern) {
        return like(x, pattern).not();
    }

    public Predicate notLike(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
        return like(x, pattern, escapeChar).not();
    }

    public Predicate notLike(Expression<String> x, Expression<String> pattern, char escapeChar) {
        return like(x, pattern, escapeChar).not();
    }

    public Predicate notLike(Expression<String> x, String pattern, Expression<Character> escapeChar) {
        return like(x, pattern, escapeChar).not();
    }

    public Predicate notLike(Expression<String> x, String pattern, char escapeChar) {
        return like(x, pattern, escapeChar).not();
    }

    public <Y> Expression<Y> nullif(Expression<Y> x, Expression<?> y) {
        return new Expressions.Nullif(x, y);
    }

    public <Y> Expression<Y> nullif(Expression<Y> x, Y y) {
        return new Expressions.Nullif(x, y);
    }

    public Predicate or(Predicate... restrictions) {
        return new PredicateImpl.Or(restrictions);
    }

    public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
    	return new PredicateImpl.Or(x,y);
    }

    /**
     * Construct a ParameterExpression with a null name as key.
     * The name of this parameter will be assigned automatically
     * when this parameter expression is 
     * {@linkplain CriteriaQueryImpl#registerParameter(ParameterExpressionImpl)
     * registered} in a Criteriaquery during tree traversal.
     * 
     * @see ParameterExpressionImpl#assignAutoName(String)
     */
    public <T> ParameterExpression<T> parameter(Class<T> paramClass) {
        return new ParameterExpressionImpl<T>(paramClass, null);
    }

    public <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
        return new ParameterExpressionImpl<T>(paramClass, name);
    }

    public <N extends Number> Expression<N> prod(Expression<? extends N> x, Expression<? extends N> y) {
        return new Expressions.Product<N>(x,y);
    }

    public <N extends Number> Expression<N> prod(Expression<? extends N> x, N y) {
        return new Expressions.Product<N>(x,y);
    }

    public <N extends Number> Expression<N> prod(N x, Expression<? extends N> y) {
        return new Expressions.Product<N>(x,y);
    }

    public Expression<Number> quot(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new Expressions.Quotient<Number>(x,y);
    }

    public Expression<Number> quot(Expression<? extends Number> x, Number y) {
        return new Expressions.Quotient<Number>(x,y);
    }

    public Expression<Number> quot(Number x, Expression<? extends Number> y) {
        return new Expressions.Quotient<Number>(x,y);
    }

    /**
     * Define a select list item corresponding to a constructor.
     * @param result  class whose instance is to be constructed
     * @param selections  arguments to the constructor
     * @return selection item
     */
    public <Y> CompoundSelection<Y> construct(Class<Y> result, Selection<?>... selections) {
        return new CompoundSelections.NewInstance<Y>(result, selections);
    }

    public <R> Case<R> selectCase() {
        return new Expressions.Case(Object.class);
    }

    public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
        return new Expressions.SimpleCase(expression);
    }

    public <C extends Collection<?>> Expression<Integer> size(C collection) {
        return new Expressions.Size(collection);
    }

    public <C extends Collection<?>> Expression<Integer> size(Expression<C> collection) {
        return new Expressions.Size(collection);
    }

    public <Y> Expression<Y> some(Subquery<Y> subquery) {
        //some and any are synonymous
        return new Expressions.Any<Y>(subquery);
    }

    public Expression<Double> sqrt(Expression<? extends Number> x) {
        return new Expressions.Sqrt(x);
    }

    public Expression<String> substring(Expression<String> x, Expression<Integer> from) {
    	return new Expressions.Substring(x, from);
    }

    public Expression<String> substring(Expression<String> x, int from) {
        return new Expressions.Substring(x, from);
    }

    public Expression<String> substring(Expression<String> x, Expression<Integer> from, Expression<Integer> len) {
        return new Expressions.Substring(x, from, len);
    }

    public Expression<String> substring(Expression<String> x, int from, int len) {
        return new Expressions.Substring(x, from, len);
    }

    public <N extends Number> Expression<N> sum(Expression<N> x) {
        return new Expressions.Sum<N>(x);
    }

    public <N extends Number> Expression<N> sum(Expression<? extends N> x, Expression<? extends N> y) {
        return new Expressions.Sum<N>(x,y);
    }

    public <N extends Number> Expression<N> sum(Expression<? extends N> x, N y) {
        return new Expressions.Sum<N>(x,y);
    }

    public <N extends Number> Expression<N> sum(N x, Expression<? extends N> y) {
        return new Expressions.Sum<N>(x,y);
    }
    
    public Expression<Long> sumAsLong(Expression<Integer> x) {
        return sum(x).as(Long.class);
    }
    
    public Expression<Double> sumAsDouble(Expression<Float> x) {
        return sum(x).as(Double.class);
    }

    public Expression<BigDecimal> toBigDecimal(Expression<? extends Number> number) {
        return new Expressions.Cast<BigDecimal>(number, BigDecimal.class);
    }

    public Expression<BigInteger> toBigInteger(Expression<? extends Number> number) {
        return new Expressions.Cast<BigInteger>(number, BigInteger.class);
    }

    public Expression<Double> toDouble(Expression<? extends Number> number) {
        return new Expressions.Cast<Double>(number, Double.class);
    }

    public Expression<Float> toFloat(Expression<? extends Number> number) {
        return new Expressions.Cast<Float>(number, Float.class);
    }

    public Expression<Integer> toInteger(Expression<? extends Number> number) {
        return new Expressions.Cast<Integer>(number, Integer.class);
    }

    public Expression<Long> toLong(Expression<? extends Number> number) {
        return new Expressions.Cast<Long>(number, Long.class);
    }

    public Expression<String> toString(Expression<Character> character) {
        return new Expressions.Cast<String>(character, String.class);
    }

    public Expression<String> trim(Expression<String> x) {
        return new Expressions.Trim(x);
    }

    public Expression<String> trim(Trimspec ts, Expression<String> x) {
        return new Expressions.Trim(x, ts);
    }

    public Expression<String> trim(Expression<Character> t, Expression<String> x) {
        return new Expressions.Trim(x, t);
    }

    public Expression<String> trim(char t, Expression<String> x) {
        return new Expressions.Trim(x, t);
    }

    public Expression<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x) {
        return new Expressions.Trim(x, t, ts);
    }

    public Expression<String> trim(Trimspec ts, char t, Expression<String> x) {
        return new Expressions.Trim(x, t, ts);
    }

    public Expression<String> upper(Expression<String> x) {
        return new Expressions.Upper(x);

    }

    public <V, M extends Map<?, V>> Expression<Collection<V>> values(M map) {
        return new Expressions.Constant<Collection<V>>(map == null ? Collections.EMPTY_LIST : map.values());
    }

    public CompoundSelection<Object[]> array(Selection<?>... terms) {
        return new CompoundSelections.Array<Object[]>(Object[].class, terms);
    }

    public Predicate isNotNull(Expression<?> x) {
        return new Expressions.IsNotNull((ExpressionImpl<?>)x);
    }

    public Predicate isNull(Expression<?> x) {
        return new Expressions.IsNull((ExpressionImpl<?> )x);
    }
    
    public <T> Expression<T> nullLiteral(Class<T> t) {
        return new Expressions.Constant<T>(t, (T)null);
    }


    /**
     * Define a tuple-valued selection item
     * @param selections  selection items
     * @return tuple-valued compound selection
     * @throws IllegalArgumentException if an argument is a tuple- or
     *          array-valued selection item
     */
    public CompoundSelection<Tuple> tuple(Selection<?>... selections) {
        return new CompoundSelections.Tuple(selections);
    }
    
    /**
     * Create a predicate based upon the attribute values of a given
     * "example" entity instance. The predicate is the conjunction 
     * or disjunction of predicates for subset of attribute of the entity.
     * <br>
     * By default, all the singular entity attributes (the basic, embedded
     * and uni-cardinality relations) that have a non-null or non-default
     * value for the example instance and are not an identity or version
     * attribute are included. The comparable attributes can be further
     * pruned by specifying variable list of attributes as the final argument.
     * 
     * @param example an instance of an entity class
     * 
     * @param style specifies various aspects of comparison such as whether
     * non-null attribute values be included, how string-valued attribute be 
     * compared, whether the individual attribute based predicates are ANDed
     * or ORed etc.
     * 
     * @param excludes list of attributes that are excluded from comparison.
     *  
     * @return a predicate 
     */
    public <T> Predicate qbe(From<?, T> from, T example, ComparisonStyle style, Attribute<?,?>... excludes) {
        if (from == null)
            throw new NullPointerException();
        if (example == null) {
            return from.isNull();
        }
        ManagedType<T> type = (ManagedType<T>)_model.managedType(example.getClass());
        return new CompareByExample<T>(this, type, from, example, 
            style == null ? qbeStyle() : style, excludes);
    }
    
    public <T> Predicate qbe(From<?, T> from, T example, ComparisonStyle style) {
        return qbe(from, example, style, null);
    }
    
    public <T> Predicate qbe(From<?, T> from, T example, Attribute<?,?>... excludes) {
        return qbe(from, example, qbeStyle(), excludes);
    }
    
    public <T> Predicate qbe(From<?, T> from, T example) {
        return qbe(from, example, qbeStyle(), null);
    }
    
    /**
     * Create a style to tune different aspects of comparison by example. 
     */
    public ComparisonStyle qbeStyle() {
        return new ComparisonStyle.Default();
    }
}
