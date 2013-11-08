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

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

public class TestFetchJoin extends CriteriaTest {
    public void testFetchJoin() {
        String jpql = "SELECT e FROM Employee e INNER JOIN FETCH e.department";
        OpenJPACriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> e = q.from(Employee.class);
        q.select(e);
        e.fetch(Employee_.department);
        
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());
    }
    
    public void testLeftFetchJoin() {
        String jpql = "SELECT e FROM Employee e LEFT JOIN FETCH e.department";
        OpenJPACriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> e = q.from(Employee.class);
        q.select(e);
        e.fetch(Employee_.department, JoinType.LEFT);
        
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());
    }
}
