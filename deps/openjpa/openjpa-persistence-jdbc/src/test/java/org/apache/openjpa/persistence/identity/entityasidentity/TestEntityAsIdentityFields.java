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
package org.apache.openjpa.persistence.identity.entityasidentity;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestEntityAsIdentityFields extends SingleEMFTestCase {    
    public void setUp() {
        setUp( CLEAR_TABLES,
                Account.class, AccountGroup.class, Person.class);
    }
    
    /**
     * Tests for the NullPointerException in MappingInfo.mergeJoinColumn reported in OpenJPA-1141.
     * 
     */
    public void testEntityAsIdentityField001() {
        EntityManager em = null;
        em = emf.createEntityManager();
        
        Query query = em.createQuery("SELECT ag from AccountGroup ag");
        List resultList = query.getResultList();
        
        em.close();
    }
    
    /**
     * Test EntityManager persist() and find() with entities with entity relationships as
     * part of their identity.  Clears persistence context between entity create and find.
     * 
     */
    public void testEntityAsIdentityField002A() {
        EntityManager em = null;
        
        try {
            em = emf.createEntityManager();
            
            // Create population
            createPopulation(em);
            
            // Clear persistence context, fetch from database
            em.clear();
            AccountId aId = new AccountId();
            aId.setAccountId(1);
            aId.setAccountHolder(1);
            aId.setGroupId(1);
            Account findAccount = em.find(Account.class, aId);
            assertTrue(findAccount != null);
        } finally {
            // Cleanup
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
    
    /**
     * Test EntityManager persist() and find() with entities with entity relationships as
     * part of their identity.  Does not clear persistence context between entity create and find.
     * 
     */
    public void testEntityAsIdentityField002B() {
        EntityManager em = null;
        
        try {
            em = emf.createEntityManager();
            
            // Create population
            createPopulation(em);
            
            // Fetch from database
            AccountId aId = new AccountId();
            aId.setAccountId(1);
            aId.setAccountHolder(1);
            aId.setGroupId(1);
            Account findAccount = em.find(Account.class, aId);
            assertTrue(findAccount != null);
        } finally {
            // Cleanup
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
    
    /**
     * Test EntityManager persist() and find() with entities with entity relationships as
     * part of their identity.  Uses different EntityManagers for create and find.
     * 
     */
    public void testEntityAsIdentityField002C() {
        EntityManager em = null;
        EntityManager emPop = null;
        
        try {
            emPop = emf.createEntityManager();
            em = emf.createEntityManager();
            
            // Create population
            createPopulation(emPop);
            
            // Clear persistence context, fetch from database
            em.clear();
            AccountId aId = new AccountId();
            aId.setAccountId(1);
            aId.setAccountHolder(1);
            aId.setGroupId(1);
            Account findAccount = em.find(Account.class, aId);
            assertTrue(findAccount != null);
        } finally {
            // Cleanup
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
            if (emPop != null) {
                if (emPop.getTransaction().isActive()) {
                    emPop.getTransaction().rollback();
                }
                emPop.close();
            }
        }
    }
    
    /**
     * Test a query with a where clause that crosses through the identity relationship.
     * Clear persistence context before performing the query.
     * 
     */
    public void testEntityAsIdentityField003A() {
        EntityManager em = null;
        
        try {
            em = emf.createEntityManager();
            
            // Create population
            createPopulation(em);
            em.clear();
            
            Query query = em.createQuery("SELECT a FROM Account a WHERE a.accountHolder.id > 5");
            List resultList = query.getResultList();
            assertEquals(5, resultList.size());
        } finally {
            // Cleanup
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
    
    /**
     * Test a query with a where clause that crosses through the identity relationship.
     * Use a separate EntityManager to populate the database, and a separate EntityManager
     * to perform the query
     * 
     */
    public void testEntityAsIdentityField004A() {
        EntityManager em = null;
        EntityManager emPop = null;
        
        try {
            emPop = emf.createEntityManager();
            em = emf.createEntityManager();
            
            // Create population
            createPopulation(emPop);
            em.clear();
            
            Query query = em.createQuery("SELECT a FROM Account a WHERE a.accountHolder.id > 5");
            List resultList = query.getResultList();
            assertEquals(5, resultList.size());
        } finally {
            // Cleanup
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
            if (emPop != null) {
                if (emPop.getTransaction().isActive()) {
                    emPop.getTransaction().rollback();
                }
                emPop.close();
            }
        }
    }
    
    /**
     * Database population
     * 
     */
    private void createPopulation(EntityManager em) {
        em.getTransaction().begin();
        
        AccountGroup ag = new AccountGroup();
        ag.setId(1);
        Set<Account> agAccountSet = ag.getAccountSet();
        em.persist(ag);
        
        for (int index = 1; index <= 10; index++) {
            Person peep = new Person();
            peep.setId(index);
            peep.setFirstName("John");
            peep.setLastName("Doe");
            
            Account account = new Account();
            account.setAccountId(index);
            account.setAccountHolder(peep);
            account.setGroupId((index / 5) + 1);
            
            account.setBalanceInCents(0);
            account.setBalanceInDollars(index * 1000);
                       
            em.persist(peep);
            em.persist(account);
            
            agAccountSet.add(account);
        }    
        
        em.getTransaction().commit();
    }
}
