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
package org.apache.openjpa.persistence.inheritance.mappedsuperclass;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test case and domain classes were originally part of the reported issue
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-873">OPENJPA-873</A>
 * <p>
 * Added a new test where mapped super class does not decalre an identity.
 * Originally reported 
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-2325">OPENJPA-2325</A>
 * 
 * @author pioneer_ip@yahoo.com
 * @author Fay Wang
 * @author Pinaki Poddar
 */
public class TestMappedSuperClass extends SingleEMFTestCase {

    public void setUp() {
        setUp(CashBaseEntity.class, 
              SituationDA.class, ValuableItemDA.class, 
              MappedSuperWithoutId.class, DerivedEntityFromMappedSuperWithoutId.class,
              CLEAR_TABLES);
    }

    public void testMappedSuperClass() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i = 0; i < 3; i++) {
            SituationDA s = new SituationDA();
            s.setCashBoxPeriodSerial("test");
            s.setType((short) (i+1));
            em.persist(s);
        }
        for (int i = 0; i < 3; i++) {
            ValuableItemDA v = new ValuableItemDA();
            v.setCode((short)(10+i));
            em.persist(v);
        }
        em.getTransaction().commit();
        
        em.clear();

        // test polymorphic queries
        String query = "select s from CashBaseEntity s where TYPE(s) = SituationDA";
        List rs = em.createQuery(query).getResultList();
        for (int i = 0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof SituationDA);
        
        query = "select s from CashBaseEntity s where TYPE(s) <> ValuableItemDA";
        try {
            rs = em.createQuery(query).getResultList();
        } catch (ArgumentException e) {
            // as expected
        } finally {
            em.close();
        }
    }
    
    /**
     * Tests that new entity can be merged when the entity is derived from a mapped
     * super class that does not declare an identity field.
     */
    public void testMergeNewInstanceDerivedFromMappedSuperClassWithoutIdentityField() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final long id = System.currentTimeMillis();
		DerivedEntityFromMappedSuperWithoutId pc = new DerivedEntityFromMappedSuperWithoutId();
		pc.setId(id);
		pc.setName("abc");
		em.persist(pc);
		em.getTransaction().commit();
		em.close();
    	
		DerivedEntityFromMappedSuperWithoutId newpc = new DerivedEntityFromMappedSuperWithoutId();
		em = emf.createEntityManager();
        em.getTransaction().begin();
		newpc.setId(id);
		newpc.setName("xyz");
		em.merge(newpc);
		em.getTransaction().commit();
		em.close();
		
		em = emf.createEntityManager();
		DerivedEntityFromMappedSuperWithoutId found = em.find(DerivedEntityFromMappedSuperWithoutId.class, id);
		assertNotNull(found);
		assertEquals("xyz", found.getName());
		
    }
}
