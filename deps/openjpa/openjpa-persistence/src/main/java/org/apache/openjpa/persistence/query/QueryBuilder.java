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

/**
 * Factory interface for query definition objects
 */
public interface QueryBuilder {
    /**
     * Create an uninitialized query definition object.
     *
     * @return query definition instance
     */
    QueryDefinition createQueryDefinition();

    /**
     * Create a query definition object with the given root. The root must be an
     * entity class.
     *
     * @param cls -
     *            an entity class
     * @return root domain object
     */
    DomainObject createQueryDefinition(Class root);

    /**
     * Create a query definition object whose root is derived from a domain
     * object of the containing query. Provides support for correlated
     * subqueries. Joins against the resulting domain object do not affect the
     * query domain of the containing query. The path expression must correspond
     * to an entity class. The path expression must not be a domain object of
     * the containing query.
     *
     * @param path -
     *             path expression corresponding to the domain object used to
     *             derive the subquery root.
     * @return the subquery DomainObject
         */
	DomainObject createSubqueryDefinition(PathExpression path);
}
