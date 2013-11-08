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
package org.apache.openjpa.persistence.embed;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.RollbackException;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestUpdateEmbeddedQueryResult extends SingleEMFTestCase {
   
    public int ID = 1;

    public void setUp() {
        setUp(Embed.class,
            Embed_Embed.class, 
            EntityA_Embed_Embed.class,
            BaseEntity.class, Address.class, Geocode.class,
            DROP_TABLES);
    }

    public void testUpdateEmbedded() {
        EntityManager em = emf.createEntityManager();
        Address a = new Address();
        a.setStreetAddress("555 Bailey Ave");
        a.setCity("San Jose");
        a.setState("CA");
        a.setZip(12955);
        Geocode g = new Geocode();
        g.setLatitude(1.0f);
        g.setLongtitude(2.0f);
        a.setGeocode(g);
        em.getTransaction().begin();        
        em.persist(a);
        em.getTransaction().commit();

        em.clear();
        em.getTransaction().begin();
        String query = "select address from Address address" +
            " where address.streetAddress = '555 Bailey Ave'";
        List<Address> list = (List<Address>) em.createQuery(query).getResultList();
        Address a1 = list.get(0);

        assertEquals(a1.getGeocode().getLatitude(),1.0f);
        
        Long id = a1.getId();

        g = new Geocode();
        g.setLatitude(3.0f);
        g.setLongtitude(4.0f);
        a1.setGeocode(g);

        em.getTransaction().commit();

        Address a2 = em.find(Address.class, id);
        assertEquals(a2.getGeocode().getLatitude(),3.0f);

        em.getTransaction().begin();
        g.setLatitude(5.0f);
        a2.setGeocode(g);
        em.getTransaction().commit();

        em.clear();
        Address a3 = em.find(Address.class, id);
        assertEquals(a3.getGeocode().getLatitude(),5.0f);

        em.clear();
        query = "select a.geocode from Address a where a.id = ?1";
        List<Geocode> rs = em.createQuery(query).setParameter(1, id).getResultList();
        Geocode g2 = rs.get(0);
        em.getTransaction().begin();
        try {
            g2.setLatitude(7.0f);
        } catch (ArgumentException e) {
            //as expected: update of embeddable field in query result is not allowed.
        }

        try {
            em.getTransaction().commit();
        } catch (RollbackException e) {
            //as expected: update of embeddable field in query result is not allowed.
        }
        em.close();
    }

    public void testEntityA_Embed_Embed_update() {
        createEntityA_Embed_Embed();
        updateEmbedded_EntityA_Embed_Embed();
    }

    /*
     * Create EntityA_Embed_Embed
     */
    public void createEntityA_Embed_Embed() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        createEntityA_Embed_Embed(em, ID);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createEntityA_Embed_Embed(EntityManager em, int id) {
        EntityA_Embed_Embed a = new EntityA_Embed_Embed();
        a.setId(id);
        a.setName("a" + id);
        a.setAge(id);
        Embed_Embed embed = createEmbed_Embed(em, id, 0);
        a.setEmbed(embed);
        em.persist(a);
    }

    public Embed_Embed createEmbed_Embed(EntityManager em, int id, int idx) {
        Embed_Embed embed = new Embed_Embed();
        embed.setIntVal1(id * 100 + idx * 10 + 1);
        embed.setIntVal2(id * 100 + idx * 10 + 2);
        embed.setIntVal3(id * 100 + idx * 10 + 3);
        Embed embed1 = createEmbed(id, idx);
        embed.setEmbed(embed1);
        return embed;
    }

    public Embed createEmbed(int id, int idx) {
        Embed embed = new Embed();
        embed.setIntVal1(id * 100 + idx * 10 + 4);
        embed.setIntVal2(id * 100 + idx * 10 + 5);
        embed.setIntVal3(id * 100 + idx * 10 + 6);
        return embed;
    }

    /*
     * 
     */
    public void testBulkUpdateEmbeddedField() {
        EntityManager em = emf.createEntityManager();
        String update = "UPDATE EntityA_Embed_Embed a set a.embed.embed.intVal1 = ?1," +
                " a.embed.embed.intVal2 = ?2 where a.id = 10";
        em.getTransaction().begin();
        int count = em.createQuery(update).setParameter(1, 100).setParameter(2, 200).executeUpdate();
        em.getTransaction().commit();
        assertEquals(count, 0);

        // test invalid bulk update embeddable field
        update = "UPDATE EntityA_Embed_Embed a set a.embed.embed = ?1";
        Embed embed1 = createEmbed(ID, 10);
        try {
        int updateCount = em.createQuery(update).setParameter(1, embed1).executeUpdate();
        } catch (ArgumentException e) {
            // as expected: Bulk update of embeddable field is not allowed.
        }
        em.close();
    }

    /*
     * update embedded object returned from query
     */
    public void updateEmbedded_EntityA_Embed_Embed() {
        EntityManager em = emf.createEntityManager();
        // test update embedded object returned from query
        String query[] = {
            "select a.embed from EntityA_Embed_Embed a",
            "select a.embed.embed from EntityA_Embed_Embed a",
            "select a.embed as e from EntityA_Embed_Embed a ORDER BY e",
        };
        List rs = null;
        Embed_Embed embedembed = null;
        Embed embed = null;
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        for (int i = 0; i < query.length; i++) {
            rs = em.createQuery(query[i]).getResultList();
            assertTrue(rs.size() > 0);
            try {
                switch (i) {
                case 0:
                case 2:
                    embedembed = (Embed_Embed) rs.get(0);
                    embedembed.getEmbed().setIntVal1(1111);
                    embedembed.setEmbed(embed);
                    break;
                case 1:
                    embed = (Embed) rs.get(0);
                    embed.setIntVal1(1111);
                    break;
                }
            } catch(ArgumentException e) {
                // as expected:
                // Update of embedded object returned from query result is not allowed.
            }
        }
        try {
            tran.commit();
        } catch(RollbackException e) {
            // as expected:
            // Update of embedded object returned from query result is not allowed.
        }
        em.close();
    }
}
