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

import java.util.List;

import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.openjpa.persistence.OpenJPAPersistence;

/**
 * Test editing of Criteria Query.
 * 
 * The tests construct a CriteriaQuery and takes a pair of JPQL String.
 * The Criteria Query is executed and its target SQL is compared with that of the first of the JPQL String pair.
 * Then the same Criteria Query is edited and the target SQL of the edited version is compared with that of the second 
 * of the JPQL String pair.
 * 
 * Also test boundary cases where nothing is selected etc.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestEdit extends CriteriaTest {
    
    public void testWhereConditionEditedToAddOr() {
        String jpql = "select p from Person p where p.name='XYZ'";
        String editedjpql = "select p from Person p where p.name='XYZ' or p.name='ABC'";
        
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        c.where(cb.equal(p.get(Person_.name), "XYZ"));
        
        assertEquivalence(c, jpql);
        
        Predicate where = c.getRestriction();
        c.where(cb.or(where, cb.equal(p.get(Person_.name), "ABC")));
        
        assertEquivalence(c, editedjpql);
    }
    
    public void testWhereConditionEditedToAddAnd() {
        String jpql = "select p from Person p where p.name='XYZ'";
        String editedjpql = "select p from Person p where p.name='XYZ' and p.name='ABC'";
        
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        c.where(cb.equal(p.get(Person_.name), "XYZ"));
        
        assertEquivalence(c, jpql);
        
        Predicate where = c.getRestriction();
        c.where(cb.and(where, cb.equal(p.get(Person_.name), "ABC")));
        
        assertEquivalence(c, editedjpql);
    }
    
    public void testWhereConditionEditedToRemoveAnd() {
        String jpql = "select p from Person p where p.name='XYZ' and p.name='ABC'";
        String editedjpql = "select p from Person p where p.name='XYZ'";
        
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        Predicate p1 = cb.equal(p.get(Person_.name), "XYZ");
        Predicate p2 = cb.equal(p.get(Person_.name), "ABC");
        c.where(p1,p2);
        
        assertEquivalence(c, jpql);
        
        Predicate where = c.getRestriction();
        List<Expression<Boolean>> exprs = where.getExpressions();
        assertEquals(2, exprs.size());
        assertTrue(exprs.contains(p1));
        assertTrue(exprs.contains(p2));
        exprs.remove(p1);
        // editing from the list does not impact the query 
        assertEquivalence(c, jpql);
        
        c.where(p1);
        assertEquivalence(c, editedjpql);
    }

    public void testEditOrderBy() {
        String jpql = "select p from Person p ORDER BY p.name";
        String editedjpql = "select p from Person p ORDER BY p.name, p.id DESC";
        
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        c.orderBy(cb.asc(p.get(Person_.name)));
        
        assertEquivalence(c, jpql);
        
        List<Order> orders = c.getOrderList();
        assertEquals(1, orders.size());
        orders.add(cb.desc(p.get(Person_.id)));
        // editing the list does not impact query
        assertEquivalence(c, jpql);
        
        // adding the modified list back does 
        c.orderBy(orders.toArray(new Order[orders.size()]));
        
        assertEquivalence(c, editedjpql);
    }
    
    public void testEditedToAddMultiselectionTerm() {
        String jpql = "select p from Person p";
        String editedjpql = "select p,p.name from Person p";
        
        CriteriaQuery<Tuple> c = cb.createTupleQuery();
        Root<Person> p = c.from(Person.class);
        c.multiselect(p);
        
        assertEquivalence(c, jpql);
        
        List<Selection<?>> terms = c.getSelection().getCompoundSelectionItems();
        terms.add(p.get(Person_.name));
        // editing the list does not impact query
        assertEquivalence(c, jpql);
        
        c.multiselect(p, p.get(Person_.name));
        assertEquivalence(c, editedjpql);
    }
    
    public void testSingleSelectionHasNoCompoundItems() {
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        c.select(p);
        try {
            Selection<Person> term = c.getSelection();
            term.getCompoundSelectionItems();
            fail("Expected to fail because primary selection has no compound terms");
        } catch (IllegalStateException e) {
            // good
        }
    }
    
    /**
     * Candidate class is implicitly selected but a null is returned by getSelection()
     */
    public void testCandidateClassIsImplicitlySelectedForEntityQuery() {
        String jpql = "select p from Person p";
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        Selection<Person> term = c.getSelection();
        assertNull(term);
        
        assertEquivalence(c, jpql);
    }
    
    public void testCandidateClassIsNotImplicitlySelectedForNonEntityQuery() {
        String jpql = "select p from Person p";
        CriteriaQuery<Tuple> c = cb.createTupleQuery();
        Root<Person> p = c.from(Person.class);
        Selection<Tuple> term = c.getSelection();
        assertNull(term);
        
        assertFails("Expected to fail without a projection term", c);
    }
    
    public void testRootIsNotImplicitlyDefined() {
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Selection<Person> term = c.getSelection();
        assertNull(term);
        
        assertFails("Expected to fail without a defined root", c);
    }
    
    public void testEditParameterizedPredicate() {
        String jpql = "select p from Person p where p.name=:p1";
        String editedjpql = "select p from Person p where p.name=:p1 and p.name=:p2";
        
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        c.where(cb.equal(p.get(Person_.name), cb.parameter(String.class, "p1")));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("p1", "XYZ");
            }
        }, c, jpql);
        
        Predicate where = c.getRestriction();
        c.where(cb.and(where, cb.equal(p.get(Person_.name), cb.parameter(String.class, "p2"))));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("p1", "MNO");
                q.setParameter("p2", "ABC");
            }
        }, c, editedjpql);
    }
    
    public void testEditParameterizedPredicateReplaced() {
        String jpql = "select p from Person p where p.name=:p1 and p.name=:p2";
        String editedjpql = "select p from Person p where p.name=:p3";
        
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        c.where(cb.and(cb.equal(p.get(Person_.name), cb.parameter(String.class, "p1")),
                       cb.equal(p.get(Person_.name), cb.parameter(String.class, "p2"))));
        assertEquals(2,c.getParameters().size());
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("p1", "XYZ");
                q.setParameter("p2", "ABC");
            }
        }, c, jpql);
        
        c.where(cb.equal(p.get(Person_.name), cb.parameter(String.class, "p3")));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("p3", "MNO");
            }
        }, c, editedjpql);
    }
    
    public void testEditParameterizedPredicateRemoved() {
        String jpql = "select p from Person p where p.name=:p1 and p.name=:p2";
        String editedjpql = "select p from Person p where p.name=:p1";
        
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        c.where(cb.and(cb.equal(p.get(Person_.name), cb.parameter(String.class, "p1")),
                       cb.equal(p.get(Person_.name), cb.parameter(String.class, "p2"))));
        assertEquals(2,c.getParameters().size());
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("p1", "XYZ");
                q.setParameter("p2", "ABC");
            }
        }, c, jpql);
        
        c.where(cb.equal(p.get(Person_.name), cb.parameter(String.class, "p1")));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("p1", "MNO");
            }
        }, c, editedjpql);
    }
    
    public void testSerachWithinResult() {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Person p").executeUpdate();
        em.getTransaction().commit();
        em.getTransaction().begin();
        Person p1 = new Person(); p1.setName("Pinaki");
        Person p2 = new Person(); p2.setName("Pacino");
        Person p3 = new Person(); p3.setName("Tom");
        Person p4 = new Person(); p4.setName("Dick");
        em.persist(p1); em.persist(p2);em.persist(p3); em.persist(p4);
        em.getTransaction().commit();
        
        CriteriaQuery<Person> c = cb.createQuery(Person.class);
        Root<Person> p = c.from(Person.class);
        c.select(p);
        Predicate like = cb.like(p.get(Person_.name), cb.parameter(String.class, "pattern"));
        c.where(like);
        TypedQuery<Person> q1 = em.createQuery(c).setParameter("pattern", "P%");
        List<Person> r1 = q1.getResultList();
        assertEquals(2, r1.size());
        
        Predicate exact = cb.equal(p.get(Person_.name), cb.parameter(String.class, "exact"));
        c.where(like, exact);
        TypedQuery<Person> q2 = em.createQuery(c).setParameter("pattern", "P%").setParameter("exact","Pinaki");
        OpenJPAPersistence.cast(q2).setCandidateCollection(r1);
        auditor.clear();
        List<Person> r2 = q2.getResultList();
        assertTrue(auditor.getSQLs().isEmpty());
        assertEquals(1, r2.size());
    }
    
    void assertFails(String message, CriteriaQuery<?> c) {
        try {
            em.createQuery(c);
            fail(message); 
        } catch (IllegalStateException e) {
            // good
        }
    }

}
