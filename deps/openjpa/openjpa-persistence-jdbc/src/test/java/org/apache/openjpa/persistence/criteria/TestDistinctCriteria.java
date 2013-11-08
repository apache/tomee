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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

public class TestDistinctCriteria extends CriteriaTest {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            deleteDataForTestDistinct();
            createDataForTestDistinct();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void tearDown() throws Exception {
        try {
            deleteDataForTestDistinct();
        } catch (Exception e) {
            
        }
        super.tearDown();
    }
    
    void createDataForTestDistinct() {
        em.getTransaction().begin();
        
        Address a1 = new Address(); a1.setState("NY");
        Address a2 = new Address(); a2.setState("RI");
        
        Customer c1 = new Customer(); c1.setAddress(a1);
        Customer c2 = new Customer(); c2.setAddress(a2);
        
        Order o1 = new Order(); o1.setCustomer(c1); 
        Order o2 = new Order(); o2.setCustomer(c1); 
        Order o3 = new Order(); o3.setCustomer(c2); 
        
        Set<Order> orders = new HashSet<Order>();
        orders.add(o1); orders.add(o2);
        c1.setOrders(orders);
        orders.clear();
        orders.add(o3);
        c2.setOrders(orders);
        
        em.persist(c1);
        em.persist(c2);
        em.persist(a1);
        em.persist(a2);
        em.persist(o1);
        em.persist(o2);
        em.persist(o3);
        
        em.getTransaction().commit();
    }
    
    void deleteDataForTestDistinct() {
        em.getTransaction().begin();
        em.createQuery("delete from Customer o").executeUpdate();
        em.createQuery("delete from Address o").executeUpdate();
        em.createQuery("delete from Order o").executeUpdate();
        em.getTransaction().commit();
    }
        

    public void testDistinct() {
        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> customer = cq.from(Customer.class);
        Fetch<Customer, Order> o = customer.fetch("orders", JoinType.LEFT);
        cq.where(customer.get("address").get("state").in("NY", "RI"));
        cq.select(customer).distinct(true);
        TypedQuery<Customer> distinctQuery = em.createQuery(cq);
        distinctQuery.setMaxResults(20);
        List<Customer> distinctResult = distinctQuery.getResultList();  
        assertEquals(2, distinctResult.size());
        
        cq.distinct(false);
        TypedQuery<Customer> indistinctQuery = em.createQuery(cq);
        indistinctQuery.setMaxResults(20);
        List<Customer> indistinctResult = indistinctQuery.getResultList();  
        assertEquals(3, indistinctResult.size());
    }
}
