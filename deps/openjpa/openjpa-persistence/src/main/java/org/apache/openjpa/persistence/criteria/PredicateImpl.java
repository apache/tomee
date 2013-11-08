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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.Literal;

/**
 * Predicate is a expression that evaluates to true or false.
 * All boolean expressions are implemented as Predicate.
 * A predicate can have zero or more predicate arguments.
 * Default predicate operator is AND (conjunction).
 * Two constant predicates are Predicate.TRUE and Predicate.FALSE.
 * AND predicate with no argument evaluates to TRUE.
 * OR predicate with no argument evaluates to FALSE.
 * Negation of a Predicate creates a new Predicate.
 * 
 * @author Pinaki Poddar
 * @author Fay Wang
 * 
 * @since 2.0.0
 */
abstract class PredicateImpl extends ExpressionImpl<Boolean> implements Predicate {
    private static Predicate TRUE;
    private static Predicate FALSE;
    
    protected final List<Predicate> _exps = Collections.synchronizedList(new ArrayList<Predicate>());
    private final BooleanOperator _op;
    private boolean _negated = false;
    
    /**
     * An AND predicate with no arguments.
     */
    protected PredicateImpl() {
        this(BooleanOperator.AND);
    }
    
    /**
     * A predicate with the given operator.
     */
    protected PredicateImpl(BooleanOperator op) {
        super(Boolean.class);
        _op = op;
    }

    /**
     * A predicate of given operator with given arguments.
     */
    protected PredicateImpl(BooleanOperator op, Predicate...restrictions) {
        this(op);
        if (restrictions == null || restrictions.length == 0) return;
        
    	for (Predicate p : restrictions) {
   			add(p);
    	}
    }

    /**
     * Adds the given predicate expression.
     */
    public PredicateImpl add(Expression<Boolean> s) {
    	synchronized (_exps) {
        	_exps.add((Predicate)s); // all boolean expressions are Predicate
		}
        return this;
    }

    public List<Expression<Boolean>> getExpressions() {
        List<Expression<Boolean>> result = new CopyOnWriteArrayList<Expression<Boolean>>();
        if (_exps.isEmpty())
            return result;
        result.addAll(_exps);
        return result;
    }

    public final BooleanOperator getOperator() {
        return _op;
    }
    
    public final boolean isEmpty() {
        return _exps.isEmpty();
    }

    /**
     * Is this predicate created by negating another predicate?
     */
    public final boolean isNegated() {
        return _negated;
    }

    /**
     * Returns a new predicate as the negation of this predicate. 
     * <br>
     * Note:
     * Default negation creates a Not expression with this receiver as delegate.
     * Derived predicates can return the inverse expression, if exists.
     * For example, NotEqual for Equal or LessThan for GreaterThanEqual etc.
     */
    public PredicateImpl not() {
        return new Expressions.Not(this).markNegated();
    }
    
    protected PredicateImpl markNegated() {
        _negated = true;
        return this;
    }
    
    public static Predicate TRUE() {
    	if (TRUE == null) {
    	    ExpressionImpl<Integer> ONE  = new Expressions.Constant<Integer>(1);
    		TRUE = new Expressions.Equal(ONE, ONE);
    	}
    	return TRUE;
    }
    
    public static Predicate FALSE() {
    	if (FALSE == null) {
    	    ExpressionImpl<Integer> ONE  = new Expressions.Constant<Integer>(1);
    		FALSE = new Expressions.NotEqual(ONE, ONE);
    	}
    	return FALSE;
    }
    
    @Override
    org.apache.openjpa.kernel.exps.Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
        if (_exps.isEmpty()) {
            return factory.newLiteral(_op == BooleanOperator.AND, Literal.TYPE_BOOLEAN);
        }
        throw new AbstractMethodError(this.getClass().getName());
    }
    
    @Override
    org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
        if (_exps.isEmpty()) {
            Predicate nil = _op == BooleanOperator.AND ? TRUE() : FALSE();
            return ((PredicateImpl)nil).toKernelExpression(factory, q);
        }
        if (_exps.size() == 1) {
            Predicate e0 = _exps.get(0);
            if (isNegated())
                e0 = e0.not();
            return ((PredicateImpl)e0).toKernelExpression(factory, q);
        }
        
        ExpressionImpl<?> e1 = (ExpressionImpl<?>)_exps.get(0);
        ExpressionImpl<?> e2 = (ExpressionImpl<?>)_exps.get(1);
        org.apache.openjpa.kernel.exps.Expression ke1 = e1.toKernelExpression(factory, q);
        org.apache.openjpa.kernel.exps.Expression ke2 = e2.toKernelExpression(factory, q);
        org.apache.openjpa.kernel.exps.Expression result = _op == BooleanOperator.AND 
            ? factory.and(ke1,ke2) : factory.or(ke1, ke2);

        for (int i = 2; i < _exps.size(); i++) {
            PredicateImpl p = (PredicateImpl)_exps.get(i);
            result = _op == BooleanOperator.AND 
              ? factory.and(result, p.toKernelExpression(factory, q))
              : factory.or(result, p.toKernelExpression(factory,q));
        }
        return _negated ? factory.not(result) : result;
    }

    @Override
    public void acceptVisit(CriteriaExpressionVisitor visitor) {
        Expressions.acceptVisit(visitor, this, _exps.toArray(new Expression<?>[_exps.size()]));
    }
    
    @Override
    public StringBuilder asValue(AliasContext q) {
        boolean braces = _exps.size() > 1;
        StringBuilder buffer =  Expressions.asValue(q, _exps.toArray(new Expression<?>[_exps.size()]), " " +_op + " ");
        if (braces) buffer.insert(0, "(").append(")");
        if (isNegated()) buffer.insert(0, "NOT ");
        return buffer;
    }

    /**
     * Concrete AND predicate.
     *
     */
    static class And extends PredicateImpl {
        public And(Expression<Boolean> x, Expression<Boolean> y) {
            super(BooleanOperator.AND);
            add(x).add(y);
        }

        public And(Predicate...restrictions) {
            super(BooleanOperator.AND, restrictions);
        }
    }

    /**
     * Concrete OR predicate.
     *
     */
    static class Or extends PredicateImpl {
        public Or(Expression<Boolean> x, Expression<Boolean> y) {
            super(BooleanOperator.OR);
            add(x).add(y);
        }

        public Or(Predicate...restrictions) {
            super(BooleanOperator.OR, restrictions);
        }
    }
}
