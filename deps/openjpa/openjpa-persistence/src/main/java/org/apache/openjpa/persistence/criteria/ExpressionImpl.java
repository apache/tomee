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

import java.util.Collection;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.apache.openjpa.kernel.exps.ExpressionFactory;

/**
 * Expression node for Criteria query.
 * Acts a bridge pattern to equivalent kernel representation.
 * 
 * @param <X> the type of the value this expression represents.
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 */
abstract class ExpressionImpl<X> extends SelectionImpl<X> implements Expression<X> {
    /**
     * @param cls the type of the evaluated result of the expression
     */
    public ExpressionImpl(Class<X> cls) {
        super(cls);
    }

    /**
     * Creates a new expression of the given type. If the given type is same as this expression's type then
     * returns the same instance. 
     * May cause runtime cast failure if this expression's immutable type is not convertible to the given type. 
     */
    public <Y> Expression<Y> as(Class<Y> type) {
       return type == getJavaType() ? (Expression<Y>)this : new Expressions.CastAs<Y>(type, this);
    }

    /**
     * Create a predicate to test whether this expression is a member of the given argument values.
     */
   public Predicate in(Object... values) {
        In<X> result = new Expressions.In<X>(this);
        for (Object v : values)
        	result.value((X)v);
        return result;
    }

   /**
    * Create a predicate to test whether this expression is a member of the given argument expressions.
    */
    public Predicate in(Expression<?>... values) {
        In<X> result = new Expressions.In<X>(this);
        for (Expression<?> e : values)
        	result.value((Expression<? extends X>)e);
        return result;
    }

    /**
     * Create a predicate to test whether this expression is a member of the given collection element values.
     */
    public Predicate in(Collection<?> values) {
        In<X> result = new Expressions.In<X>(this);
        for (Object e : values)
        	result.value((X)e);
        return result;
    }

    /**
     * Create a predicate to test whether this expression is a member of the given expression representing a collection.
     */
    public Predicate in(Expression<Collection<?>> values) {
        In<X> result = new Expressions.In<X>(this);
        result.value((Expression<? extends X>)values);
        return result;
    }

    /**
     *  Create a predicate to test whether this expression is not null.
     */
    public Predicate isNotNull() {
    	return new Expressions.IsNotNull(this);
    }

    /**
     *  Create a predicate to test whether this expression is null.
     */
    public Predicate isNull() {
    	return new Expressions.IsNull(this);
    }
    
    //  ------------------------------------------------------------------------------------
    //  Contract for bridge pattern to convert to an equivalent kernel representation.
    //  ------------------------------------------------------------------------------------
    /**
     * Bridge contract to convert this facade expression to a kernel value.
     * @param factory creates the kernel expression
     * @param q the query definition context of this expression
     * @return an equivalent kernel value
     */
    abstract org.apache.openjpa.kernel.exps.Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q);
    
    /**
     * Bridge contract to convert this facade expression to a kernel expression.
     * @param factory creates the kernel expression
     * @param q the query definition context of this expression
     * @return an equivalent kernel expression
     */
    org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
        return factory.asExpression(toValue(factory, q));
    }
}
