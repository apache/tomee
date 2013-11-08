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

import javax.persistence.Tuple;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Metamodel;

/**
 * OpenJPA-specific extension to JPA 2.0 Criteria Query Builder API.
 * 
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 */
public interface OpenJPACriteriaBuilder extends CriteriaBuilder {
    /**
     * The mnemonic to identify the query language.
     */
    public static final String LANG_CRITERIA = "javax.persistence.criteria";
    
    /**
     * Create a predicate based upon the attribute values of a given
     * "example" entity instance. The predicate is the conjunction 
     * or disjunction of predicates for subset of attribute of the entity.
     * <br>
     * All the singular entity attributes (the basic, embedded
     * and uni-cardinality relations) that have a non-null or non-default
     * value for the example instance and are not an identity or version
     * attribute are included. The comparable attributes can be further
     * pruned by specifying variable list of attributes for exclusion.
     * 
     * @param example a non-null instance of a persistent entity.
     * 
     * @param style specifies various aspects of comparison such as whether
     * non-null attribute values be included, how string-valued attribute be 
     * compared, whether the individual attribute based predicates are ANDed
     * or ORed etc. Can be null to designate default comparison style.
     * 
     * @param excludes list of attributes that are excluded from comparison.
     * Can be null.
     *  
     * @return a predicate 
     */
    public <T> Predicate qbe(From<?, T> from, T example, ComparisonStyle style, Attribute<?,?>... excludes);
    
    /**
     * Overloaded with no extra attribute to exclude.
     */
    public <T> Predicate qbe(From<?, T> from, T example, ComparisonStyle style);
    
    /**
     * Overloaded with default comparison style.
     */
    public <T> Predicate qbe(From<?, T> from, T example, Attribute<?,?>... excludes);
    
    /**
     * Overloaded with default comparison style and no extra attribute to exclude.
     */
    public <T> Predicate qbe(From<?, T> from, T example);
    
    /**
     * Create a mutable style to apply on query-by-example.
     */
    public ComparisonStyle qbeStyle();
    
    /**
     * Gets the metamodel for the managed, persistent domain entities.
     */
    public Metamodel getMetamodel();
    
    /**
     *  Create a <code>CriteriaQuery</code> object.
     *  @return criteria query object
     */
    OpenJPACriteriaQuery<Object> createQuery();

    /**
     *  Create a <code>CriteriaQuery</code> object with the specified result 
     *  type.
     *  @param resultClass  type of the query result
     *  @return criteria query object
     */
    <T> OpenJPACriteriaQuery<T> createQuery(Class<T> resultClass);

    /**
     *  Create a <code>CriteriaQuery</code> object that returns a tuple of 
     *  objects as its result.
     *  @return criteria query object
     */
    OpenJPACriteriaQuery<Tuple> createTupleQuery();

}

