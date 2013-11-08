/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
 * Interface for construction of query definitions
 */
public interface QueryDefinition extends Subquery {
    /**
     * Add a query root corresponding to the given entity, forming a cartesian
     * product with any existing roots. The domain object that is returned is
     * bound as a component of the given query. The argument must be an entity
     * class.
     *
     * @param cls -
     *            an entity class
     * @return DomainObject corresponding to the specified entity class.
     */
    DomainObject addRoot(Class cls);

    /**
     * Add a root derived from a domain object of the containing query
     * definition to a query definition used as a subquery. Provides support for
     * correlated subqueries. Joins against the resulting domain object do not
     * affect the query domain of the containing query. The path expression must
     * correspond to an entity class. The path expression must not be a domain
     * object of the containing query.
     *
     * @param path -
     *             path expression corresponding to the domain object used to
     *             derive the subquery root.
     * @return the subquery DomainObject
     */
    DomainObject addSubqueryRoot(PathExpression path);

    /**
     * Specify the objects / values to be returned. Replaces the previous select
     * list, if any. If no select items are specified and there is only one
     * query root, the root entity is assumed to be the result.
     *
     * @param selectItems -
     *                    one or more SelectItem instances
     * @return the modified query definition instance
     */
    QueryDefinition select(SelectItem... selectItems);

    /**
     * Specify the objects / values to be returned. Replaces the previous select
     * list, if any. If no select items are specified and there is only one
     * query root, the root entity is assumed to be the result.
     *
     * @param selectItemList -
     *                       a list containing one or more SelectItem instances
     * @return the modified query definition instance
     */
    QueryDefinition select(List<SelectItem> selectItemList);

    /**
     * Specify the objects / values to be returned. Duplicate results will be
     * eliminated. Replaces the previous select list, if any. If no select items
     * are specified and there is only one query root, the root entity is
     * assumed to be the result.
     *
     * @param selectItems -
     *                    one or more SelectItem instances
     * @return the modified query definition instance
     */
    QueryDefinition selectDistinct(SelectItem... selectItems);

    /**
     * Specify the objects / values to be returned. Duplicate results will be
     * eliminated. Replaces the previous select list, if any. If no select items
     * are specified, and there is only one query root, the root entity is
     * assumed to be the result. is assumed to be the result.
     *
     * @param selectItemList -
     *                       a list containing one or more SelectItem instances
     * @return the modified query definition instance
     */
    QueryDefinition selectDistinct(List<SelectItem> selectItemList);

    /**
     * Modifies the query definition to restrict the result of the query
     * according to the specified predicate. Replaces the previously added
     * restriction(s), if any.
     *
     * @param predicate -
     *                  a simple or compound conditional predicate
     * @return the modified QueryDefinition instance
     */
    QueryDefinition where(Predicate predicate);

    /**
     * Specify the items of the select list that are used in ordering the query
     * results. Replaces the previous order-by list, if any.
     *
     * @param orderByItems -
     *                     one or more OrderByItem instances
     * @return the modified QueryDefinition instance
     */
    QueryDefinition orderBy(OrderByItem... orderByItems);

    /**
     * Specify the items of the select list that are used in ordering the query
     * results. Replaces the previous order-by list, if any.
     *
     * @param orderByItemList -
     *                        a list containing one or more OrderByItem
     *                        instances
     * @return the modified QueryDefinition instance
     */
    QueryDefinition orderBy(List<OrderByItem> orderByItemList);

    /**
     * Specify the items that are used to form groups over the query results.
     * Replaces the previous group-by list, if any.
     *
     * @param pathExprs
     * @return the modified QueryDefinition instance
     */
    QueryDefinition groupBy(PathExpression... pathExprs);

    /**
     * Specify the items that are used to form groups over the query results.
     * Replaces the previous group-by list, if any.
     *
     * @param pathExprList
     * @return the modified QueryDefinition instance
     */
    QueryDefinition groupBy(List<PathExpression> pathExprList);

    /**
     * Specify the restrictions over the groups of a query. Replaces the
     * previous having restriction(s), if any.
     *
     * @param predicate
     * @return the modified QueryDefinition Instance
     */
    QueryDefinition having(Predicate predicate);

    /**
     * Specify that a constructor for the given class is to be applied to the
     * corresponding query results after the query is executed. The class must
     * have a constructor that accepts the Java argument types corresponding to
     * the given select items.
     *
     * @param cls  -
     *             a class with the correponding constructor
     * @param args -
     *             select items that correspond to result types that are valid
     *             as arguments to the constructor
     * @result SelectItem instance representing the constructor
     */
    SelectItem newInstance(Class cls, SelectItem... args);

    /**
     * Use the query definition instance as a subquery in an exists predicate.
     *
     * @return the resulting predicate
     */
    Predicate exists();

    /**
     * Use the query definition object in a subquery in an all expression.
     *
     * @return the resulting Subquery
     */
    Subquery all();

    /**
     * Use the query definition object in a subquery in an any expression.
     *
     * @return the resulting Subquery
     */
    Subquery any();

    /**
     * Use the query definition object in a subquery in a some expression.
     *
     * @return the resulting Subquery
     */
    Subquery some();

    /**
     * Create an empty general case expression. A general case expression is of
     * the form:
     * <p/>
     * generalCase() .when(conditional-predicate).then(scalar-expression)
     * .when(conditional-predicate).then(scalar-expression) ...
     * .elseCase(scalar-expression)
     *
     * @return empty general case expression
     */
    CaseExpression generalCase();

    /**
     * Create a simple case expression with the given case operand. A simple
     * case expression is of the form:
     * <p/>
     * simpleCase(case-operand) .when(scalar-expression).then(scalar-expression)
     * .when(scalar-expression).then(scalar-expression) ...
     * .elseCase(scalar-expression)
     *
     * @param caseOperand -
     *                    expression used for testing against the when scalar
     *                    expressions
     * @return case expression with the given case operand
     */
    CaseExpression simpleCase(Expression caseOperand);

    /**
     * Create a simple case expression with the given case operand. A simple
     * case expression is of the form:
     * <p/>
     * simpleCase(case-operand) .when(scalar-expression).then(scalar-expression)
     * .when(scalar-expression).then(scalar-expression) ...
     * .elseCase(scalar-expression)
     *
     * @param caseOperand -
     *                    numeric value used for testing against the when scalar
     *                    expressions
     * @return case expression with the given case operand
     */
    CaseExpression simpleCase(Number caseOperand);

    /**
     * Create a simple case expression with the given case operand. A simple
     * case expression is of the form:
     * <p/>
     * simpleCase(case-operand) .when(scalar-expression).then(scalar-expression)
     * .when(scalar-expression).then(scalar-expression) ...
     * .elseCase(scalar-expression)
     *
     * @param caseOperand -
     *                    value used for testing against the when scalar
     *                    expressions
     * @return case expression with the given case operand
     */
    CaseExpression simpleCase(String caseOperand);

    /**
     * Create a simple case expression with the given case operand. A simple
     * case expression is of the form:
     * <p/>
     * simpleCase(case-operand) .when(scalar-expression).then(scalar-expression)
     * .when(scalar-expression).then(scalar-expression) ...
     * .elseCase(scalar-expression)
     *
     * @param caseOperand -
     *                    value used for testing against the when scalar
     *                    expressions
     * @return case expression with the given case operand
     */
    CaseExpression simpleCase(Date caseOperand);

    /**
     * Create a simple case expression with the given case operand. A simple
     * case expression is of the form:
     * <p/>
     * simpleCase(case-operand) .when(scalar-expression).then(scalar-expression)
     * .when(scalar-expression).then(scalar-expression) ...
     * .elseCase(scalar-expression)
     *
     * @param caseOperand -
     *                    value used for testing against the when scalar
     *                    expressions
     * @return case expression with the given case operand
     */
    CaseExpression simpleCase(Calendar caseOperand);

    /**
     * Create a simple case expression with the given case operand. A simple
     * case expression is of the form:
     * <p/>
     * simpleCase(case-operand) .when(scalar-expression).then(scalar-expression)
     * .when(scalar-expression).then(scalar-expression) ...
     * .elseCase(scalar-expression)
     *
     * @param caseOperand -
     *                    value used for testing against the when scalar
     *                    expressions
     * @return case expression with the given case operand
     */
    CaseExpression simpleCase(Class caseOperand);

    /**
     * Create a simple case expression with the given case operand. A simple
     * case expression is of the form:
     * <p/>
     * simpleCase(case-operand) .when(scalar-expression).then(scalar-expression)
     * .when(scalar-expression).then(scalar-expression) ...
     * .elseCase(scalar-expression)
     *
     * @param caseOperand -
     *                    value used for testing against the when scalar
     *                    expressions
     * @return case expression with the given case operand
     */
    CaseExpression simpleCase(Enum<?> caseOperand);

    /**
     * coalesce This is equivalent to a case expression that returns null if all
     * its arguments evaluate to null, and the value of its first non-null
     * argument otherwise.
     *
     * @param exp -
     *            expressions to be used for testing against null
     * @return Expression corresponding to the given coalesce expression
     */
    Expression coalesce(Expression... exp);

    /**
     * coalesce This is equivalent to a case expression that returns null if all
     * its arguments evaluate to null, and the value of its first non-null
     * argument otherwise.
     *
     * @param exp -
     *            expressions to be used for testing against null
     * @return Expression corresponding to the given coalesce expression
     */
    Expression coalesce(String... exp);

    /**
     * coalesce This is equivalent to a case expression that returns null if all
     * its arguments evaluate to null, and the value of its first non-null
     * argument otherwise.
     *
     * @param exp -
     *            expressions to be used for testing against null
     * @return Expression corresponding to the given coalesce expression
     */
    Expression coalesce(Date... exp);

    /**
     * coalesce This is equivalent to a case expression that returns null if all
     * its arguments evaluate to null, and the value of its first non-null
     * argument otherwise.
     *
     * @param exp -
     *            expressions to be used for testing against null
     * @return Expression corresponding to the given coalesce expression
     */
    Expression coalesce(Calendar... exp);

    /**
     * nullif This is equivalent to a case expression that tests whether its
     * arguments are equal, returning null if they are and the value of the
     * first expression if they are not.
     *
     * @param exp1
     * @param exp2
     * @return Expression corresponding to the given nullif expression
     */
    Expression nullif(Expression exp1, Expression exp2);

    /**
     * nullif This is equivalent to a case expression that tests whether its
     * arguments are equal, returning null if they are and the value of the
     * first expression if they are not.
     *
     * @param arg1
     * @param arg2
     * @return Expression corresponding to the given nullif expression
     */
    Expression nullif(Number arg1, Number arg2);

    /**
     * nullif This is equivalent to a case expression that tests whether its
     * arguments are equal, returning null if they are and the value of the
     * first expression if they are not.
     *
     * @param arg1
     * @param arg2 Criteria API Java Persistence 2.0, Public Review Draft
     *             Criteria API Interfaces 10/31/08 158 JSR-317 Public Review
     *             Draft Sun Microsystems, Inc.
     * @return Expression corresponding to the given nullif expression
     */
    Expression nullif(String arg1, String arg2);

    /**
     * nullif This is equivalent to a case expression that tests whether its
     * arguments are equal, returning null if they are and the value of the
     * first expression if they are not.
     *
     * @param arg1
     * @param arg2
     * @return Expression corresponding to the given nullif expression
     */
    Expression nullif(Date arg1, Date arg2);

    /**
     * nullif This is equivalent to a case expression that tests whether its
     * arguments are equal, returning null if they are and the value of the
     * first expression if they are not.
     *
     * @param arg1
     * @param arg2
     * @return Expression corresponding to the given nullif expression
     */
    Expression nullif(Calendar arg1, Calendar arg2);

    /**
     * nullif This is equivalent to a case expression that tests whether its
     * arguments are equal, returning null if they are and the value of the
     * first expression if they are not.
     *
     * @param arg1
     * @param arg2
     * @return Expression corresponding to the given nullif expression
     */
    Expression nullif(Class arg1, Class arg2);

    /**
     * nullif This is equivalent to a case expression that tests whether its
     * arguments are equal, returning null if they are and the value of the
     * first expression if they are not.
     *
     * @param arg1
     * @param arg2
     * @return Expression corresponding to the given nullif expression
     */
    Expression nullif(Enum<?> arg1, Enum<?> arg2);

    /**
     * Create a predicate value from the given boolean.
     *
     * @param b boolean value
     * @return a true or false predicate
     */
    Predicate predicate(boolean b);

    /**
     * Create an Expression corresponding to the current time on the database
     * server at the time of query execution.
     *
     * @return the corresponding Expression
     */
    Expression currentTime();

    /**
     * Create an Expression corresponding to the current date on the database
     * server at the time of query execution.
     *
     * @return the corresponding Expression
     */
    Expression currentDate();

    /**
     * Create an Expression corresponding to the current timestamp on the
     * database server at the time of query execution.
     *
     * @return the corresponding Expression
     */
    Expression currentTimestamp();

    /**
     * Create an Expression corresponding to a String value.
     *
     * @param s -
     *          string value
     * @return the corresponding Expression literal
     */
    Expression literal(String s);

    /**
     * Create an Expression corresponding to a numeric value.
     *
     * @param n -
     *          numeric value
     * @return the corresponding Expression literal
     */
    Expression literal(Number n);

    /**
     * Create an Expression corresponding to a boolean value.
     *
     * @param b -
     *          boolean value
         * @return the corresponding Expression literal
         */
	Expression literal(boolean b);

	/**
         * Create an Expression corresponding to a Calendar value.
         *
         * @param c -
         *          Calendar value
         * @return the corresponding Expression literal
         */
	Expression literal(Calendar c);

	/**
         * Create an Expression corresponding to a Date value.
         *
         * @param d -
         *          Date value
         * @return the corresponding Expression literal
         */
	Expression literal(Date d);

	/**
         * Create an Expression corresponding to a character value.
         *
         * @param character value
         * @return the corresponding Expression literal
         */
	Expression literal(char c);

	/**
	 * Create an Expression corresponding to an entity class.
	 *
	 * @param cls -
	 *            entity class
	 * @return the corresponding Expression literal
	 */
	Expression literal(Class cls);

	/**
         * Create an Expression corresponding to an enum.
         *
         * @param e -
         *          enum
         * @return the corresponding Expression literal
         */
	Expression literal(Enum<?> e);

	/**
	 * Create an Expression corresponding to a null value.
	 *
	 * @return the corresponding Expression literal
	 */
	Expression nullLiteral();

	/**
         * Specify use of a parameter of the given name.
         *
         * @param parameter name
         * @return an Expression corresponding to a named parameter
         */
	Expression param(String name);
}
