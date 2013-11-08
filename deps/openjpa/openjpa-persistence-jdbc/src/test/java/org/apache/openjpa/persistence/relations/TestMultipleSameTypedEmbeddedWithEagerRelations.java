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
package org.apache.openjpa.persistence.relations;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that querying and retrieving entities with multiple same-typed embedded
 * relations, which themselves have eager relations, works. 
 *
 * @author Abe White
 */
public class TestMultipleSameTypedEmbeddedWithEagerRelations
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(EmbeddableWithRelation.class, MultipleSameTypedEmbedded.class);

        EmbeddableWithRelation embed1 = new EmbeddableWithRelation();
        embed1.setName("embed1");
        EmbeddableWithRelation embed2 = new EmbeddableWithRelation();
        embed2.setName("embed2");

        MultipleSameTypedEmbedded m1 = new MultipleSameTypedEmbedded();
        m1.setName("m1");
        m1.setEmbed1(embed1);
        m1.setEmbed2(embed2);
        MultipleSameTypedEmbedded m2 = new MultipleSameTypedEmbedded();
        m2.setName("m2");
        m2.setEmbed1(embed2);
        m2.setEmbed2(embed1);

        embed1.setRel(m1);
        embed2.setRel(m2);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(m1);
        em.persist(m2);
        em.getTransaction().commit();
        em.close();
    }

    public void testQuery() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select m from MultipleSameTypedEmbedded m "
            + "where m.embed1.rel.name = 'm1' "
            + "and m.embed2.rel.name = 'm2'");
        List res = q.getResultList();
        assertEquals(1, res.size());

        MultipleSameTypedEmbedded m = (MultipleSameTypedEmbedded) res.get(0);
        assertEquals("m1", m.getName());
        assertEquals("embed1", m.getEmbed1().getName());
        assertEquals("m1", m.getEmbed1().getRel().getName());
        assertEquals("embed2", m.getEmbed2().getName());
        assertEquals("m2", m.getEmbed2().getRel().getName());
            
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestMultipleSameTypedEmbeddedWithEagerRelations.class);
    }
}

