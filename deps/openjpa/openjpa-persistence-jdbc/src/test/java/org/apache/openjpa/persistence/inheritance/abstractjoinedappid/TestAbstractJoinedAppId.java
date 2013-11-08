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
package org.apache.openjpa.persistence.inheritance.abstractjoinedappid;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that you traverse an inverse key-baesd relation to an abstract
 * class using joined inheritance.
 */
public class TestAbstractJoinedAppId
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(Superclass.class, Subclass.class, RelationOwner.class, 
            DROP_TABLES);

        Subclass s = new Subclass();
        s.setId(99);
        s.setAttr1("supattr");
        s.setAttr2("subattr");
    
        RelationOwner ro = new RelationOwner();
        ro.setId(1);
        ro.setSupers(Arrays.asList(new Superclass[] { s }));

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(ro);
        em.getTransaction().commit();
        em.close();
    }

    public void testEntityTypeForAbstractJoined() {
        EntityManager em = emf.createEntityManager();
        String query = "select s from RelationOwner r join r.supers s where TYPE(s) = Subclass";
        List rs = em.createQuery(query).getResultList();
        assertTrue(rs.size() > 0);
        for (int i = 0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof Subclass);
        query = "select s from Superclass s where TYPE(s) = Subclass";
        rs = em.createQuery(query).getResultList();
        assertTrue(rs.size() > 0);
        for (int i = 0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof Subclass);
        em.close();
    }

    public void testTraverseRelation() {
        EntityManager em = emf.createEntityManager();
        RelationOwner ro = em.find(RelationOwner.class, 1);
        assertNotNull(ro);

        Collection supers = ro.getSupers();
        assertEquals(1, supers.size());        
        Superclass s = (Superclass) supers.iterator().next();
        assertTrue(s instanceof Subclass);
        assertEquals(new Integer(99), s.getId());
        assertEquals("supattr", s.getAttr1());
        assertEquals("subattr", ((Subclass) s).getAttr2());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestAbstractJoinedAppId.class);
    }
}

