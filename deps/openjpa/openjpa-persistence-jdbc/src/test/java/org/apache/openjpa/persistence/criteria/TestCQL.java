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

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Test logical predicates precedence is preserved and rendered correctly in CQL.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestCQL extends CriteriaTest {
    public void testLogicalPrecedence() {
        OpenJPACriteriaQuery<Person> q = cb.createQuery(Person.class);
        Root<Person> p = q.from(Person.class);
        q.select(p);
        Predicate a = cb.equal(p.get(Person_.name), "A");
        Predicate b = cb.equal(p.get(Person_.name), "B");
        Predicate c = cb.equal(p.get(Person_.name), "C");
        Predicate d = cb.equal(p.get(Person_.name), "D");
        
        // (a OR b) AND (c or D)
        q.where(cb.or(a,b), cb.or(c,d));
        
        String jpql = "";
        // The strings are compared for exact match so be careful about spaces and such... 
        jpql = "SELECT p FROM Person p WHERE ((p.name = 'A' OR p.name = 'B') AND (p.name = 'C' OR p.name = 'D'))";
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());
        
        // (a OR b or C) AND D
        q.where(cb.or(a,b,c), d);
        jpql = "SELECT p FROM Person p WHERE ((p.name = 'A' OR p.name = 'B' OR p.name = 'C') AND p.name = 'D')";
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());
        
        // a AND (b OR c) AND d 
        q.where(a, cb.or(b,c), d);
        jpql = "SELECT p FROM Person p WHERE (p.name = 'A' AND (p.name = 'B' OR p.name = 'C') AND p.name = 'D')";
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());
        
        // a OR (b AND (c OR d)) 
        q.where(cb.or(a, cb.and(b, cb.or(c,d))));
        jpql = "SELECT p FROM Person p WHERE (p.name = 'A' OR (p.name = 'B' AND (p.name = 'C' OR p.name = 'D')))";
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());
        
        // NOT (a OR b) 
        q.where(cb.or(a, b).not());
        jpql = "SELECT p FROM Person p WHERE NOT (p.name = 'A' OR p.name = 'B')";
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());
        
        // NOT a 
        q.where(cb.and(a).not());
        jpql = "SELECT p FROM Person p WHERE NOT p.name = 'A'";
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());

        // NOT a OR NOT b
        q.where(cb.or(cb.not(a), cb.not(b)));
        jpql = "SELECT p FROM Person p WHERE (p.name <> 'A' OR p.name <> 'B')";
        assertEquivalence(q, jpql);
        assertEquals(jpql, q.toCQL());
    }
}
