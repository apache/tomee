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
 * Interface used to define compound predicates.
 */
public interface Predicate {
    /**
     * Creates an AND of the predicate with the argument.
     *
     * @param predicate -
     *                  A simple or compound predicate
     * @return the predicate that is the AND of the original simple or compound
     *         predicate and the argument.
     */
    Predicate and(Predicate predicate);

    /**
     * Creates an OR of the predicate with the argument.
     *
     * @param predicate -
     *                  A simple or compound predicate
     * @return the predicate that is the OR of the original simple or compound
     *         predicate and the argument.
     */
    Predicate or(Predicate predicate);

    /**
     * Creates a negation of the predicate with the argument.
     *
     * @return the predicate that is the negation of the original simple or
	 *         compound predicate.
	 */
	Predicate not();
}
