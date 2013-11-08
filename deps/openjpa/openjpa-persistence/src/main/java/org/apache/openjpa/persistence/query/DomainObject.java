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
 * Domain objects define the domain over which a query operates. A domain object
 * plays a role analogous to that of a Java Persistence query language
 * identification variable.
 */
public interface DomainObject extends PathExpression, QueryDefinition {
    /**
     * Extend the query domain by joining with a class that can be navigated to
     * or that is embedded in the class corresponding to the domain object on
     * which the method is invoked. This method is permitted to be invoked only
     * when defining the domain of the query. It must not be invoked within the
     * context of the select, where, groupBy, or having operations. The domain
     * object must correspond to a class that contains the referenced attribute.
     * The query definition is modified to include the newly joined domain
     * object.
     *
     * @param attribute -
     *                  name of the attribute that references the target of the
     *                  join
     * @return the new DomainObject that is added for the target of the join
     */
    DomainObject join(String attribute);

    /**
     * Extend the query domain by left outer joining with a class that can be
     * navigated to or that is embedded in the class corresponding to the domain
     * object on which the method is invoked. This method is permitted to be
     * invoked only when defining the domain of the query. It must not be
     * invoked within the context of the select, where, groupBy, or having
     * operations. The domain object must correspond to a class that contains
     * the referenced attribute. The query definition is modified to include the
     * newly joined domain object.
     *
     * @param attribute -
     *                  name of the attribute that references the target of the
     *                  join
     * @return the new DomainObject that is added for the target of the join
     */
    DomainObject leftJoin(String attribute);

    /**
     * Specify that the association or element collection that is referenced by
     * the attribute be eagerly fetched through use of an inner join. The domain
     * object must correspond to a class that contains the referenced attribute.
     * The query is modified to include the joined domain object.
     *
     * @param attribute -
     *                  name of the attribute that references the target of the
     *                  join
     * @return the FetchJoinObject that is added for the target of the join
     */
    FetchJoinObject joinFetch(String attribute);

    /**
     * Specify that the association or element collection that is referenced by
     * the attribute be eagerly fetched through use of a left outer join. The
     * domain object must correspond to a class that contains the referenced
     * attribute. The query is modified to include the joined domain object.
     *
     * @param attribute -
     *                  name of the attribute that references the target of the
     *                  join
     * @return the FetchJoinObject that is added for the target of the join
     */
    FetchJoinObject leftJoinFetch(String attribute);

    /**
     * Return a path expression corresponding to the value of a map-valued
     * association or element collection. This method is only permitted to be
     * invoked upon a domain object that corresponds to a map-valued association
     * or element collection.
     *
     * @return PathExpression corresponding to the map value
     */
    PathExpression value();

    /**
     * Return a path expression corresponding to the key of a map-valued
     * association or element collection. This method is only permitted to be
     * invoked upon a domain object that corresponds to a map-valued association
     * or element collection.
     *
     * @return PathExpression corresponding to the map key
     */
    PathExpression key();

    /**
     * Return a select item corresponding to the map entry of a map-valued
     * association or element collection. This method is only permitted to be
     * invoked upon a domain object that corresponds to a map-valued association
     * or element collection.
     *
     * @return SelectItem corresponding to the map entry
     */
    SelectItem entry();

    /**
     * Return an expression that corresponds to the index. of the domain object
     * in the referenced association or element collection. This method is only
	 * permitted to be invoked upon a domain object that corresponds to a
     * multi-valued association or element collection for which an order column
	 * has been defined.
	 *
	 * @return Expression denoting the index
	 */
	Expression index();
}
