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
package org.apache.openjpa.persistence.jdbc.update;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests for update on entity that uses a Timestamp as version.
 * 
 * @see <A HREF="https://issues.apache.org/jira/browse/OPENJPA-1583">OPENJPA-1583</A>
 *     
 * @author Pinaki Poddar
 * 
 */
public class TestTimestampVersion extends SingleEMFTestCase {
	public void setUp() {
        super.setUp(CLEAR_TABLES, TimestampedEntity.class, NumericVersionedEntity.class, BaseTimestampedEntity.class);
    }
	public void testQueryOnVersion() {
	    EntityManager em = emf.createEntityManager();
	    String pql = "SELECT s FROM TimestampedEntity s WHERE s.version < :endDate";
        Query queryObj = em.createQuery(pql);
        Timestamp t1 = new Timestamp((new Date()).getTime());
        queryObj.setParameter("endDate", t1);
        List<TimestampedEntity> scenarioList = queryObj.getResultList();
	}

    public void testBulkUpdateOnTimestampedVersion() {
        TimestampedEntity pc = new TimestampedEntity();
        pc.setName("Original");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(pc);
        em.getTransaction().commit();
        
        try {
            // delay to ensure the new timestamp exceeds the timer's resolution.
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        em.getTransaction().begin();
        Timestamp oldVersion = pc.getVersion();
        String jpql = "UPDATE TimestampedEntity t SET t.name=:newname WHERE t.name=:oldname";
        em.createQuery(jpql)
          .setParameter("newname", "Updated")
          .setParameter("oldname", "Original")
          .executeUpdate();
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        em.refresh(pc);
        Timestamp newVersion = pc.getVersion();
        assertTrue("Expected newVersion=" + newVersion.toString() + " to be after oldVersion=" + oldVersion.toString(),
            newVersion.after(oldVersion));
    }
    
    public void testBulkUpdateOnNumericVersion() {
        NumericVersionedEntity pc = new NumericVersionedEntity();
        pc.setName("Original");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(pc);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        int oldVersion = pc.getVersion();
        String jpql = "UPDATE NumericVersionedEntity t SET t.name=:newname WHERE t.name=:oldname";
        em.createQuery(jpql)
          .setParameter("newname", "Updated")
          .setParameter("oldname", "Original")
          .executeUpdate();
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        em.refresh(pc);
        int newVersion = pc.getVersion();
        assertEquals(newVersion, oldVersion+1);
    }

	
}
