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

import javax.persistence.criteria.CriteriaQuery;

/**
 * OpenJPA-specific extension to JPA 2.0 Criteria Query API.
 * 
 * @param <T> type of result returned by this query
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 */
public interface OpenJPACriteriaQuery<T> extends CriteriaQuery<T> {
    /**
     * Convert the query to a JPQL-like string.
     * The conversion of Criteria Query may not be an exact JPQL string.
     *  
     * @return a JPQL-like string.
     */
    public String toCQL();
    
    /**
     * Compile the query.
     * 
     * @return the same instance compiled.
     */
    public OpenJPACriteriaQuery<T> compile();
}

