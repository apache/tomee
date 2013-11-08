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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_27_ex0;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import junit.framework.Assert;

import org.apache.openjpa.kernel.QueryImpl;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestSpec10_1_27 extends SQLListenerTestCase {
    public int numItems = 2;
    public int numImagesPerItem = 3;
    public int numCompany = 2;
    public int numDivisionsPerCo = 2;
    public int itemId = 1;
    public int compId = 1;
    public int divId = 1;
    public int vpId = 1;
    public List<Compny1> rsAllCompny1 = null;
    public List<Compny2> rsAllCompny2 = null;

    public void setUp() {
        super.setUp(CLEAR_TABLES,
                Compny1.class, Compny2.class,
                Item1.class, Item2.class,
                Division.class, VicePresident.class);
        createObj(emf);
        rsAllCompny1 = getAll(Compny1.class);
        rsAllCompny2 = getAll(Compny2.class);
    }

    @AllowFailure
    public void testQueryInMemoryQualifiedId() throws Exception {
        queryQualifiedId(true);
    }
    
    public void testQueryQualifiedId() throws Exception {
        queryQualifiedId(false);
    }

    public void setCandidate(Query q, Class<?> clz) 
        throws Exception {
        org.apache.openjpa.persistence.QueryImpl<?> q1 = 
            (org.apache.openjpa.persistence.QueryImpl<?>) q;
        org.apache.openjpa.kernel.Query q2 = q1.getDelegate();
        org.apache.openjpa.kernel.QueryImpl qi = (QueryImpl) q2;
        if (clz == Compny1.class)
            qi.setCandidateCollection(rsAllCompny1);
        else if (clz == Compny2.class)
            qi.setCandidateCollection(rsAllCompny2);
    }

    public void queryQualifiedId(boolean inMemory) throws Exception {
        EntityManager em = emf.createEntityManager();

        String query = "select KEY(e) from Compny1 c, " +
            " in (c.orgs) e order by c.id";
        Query q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Compny1.class);
        List<?> rs = q.getResultList();
        Division d = (Division) rs.get(0);
        Division d2 = (Division) rs.get(1);

        em.clear();
        query = "select ENTRY(e) from Compny1 c, " +
        " in (c.orgs) e order by c.id";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Compny1.class);
        rs = q.getResultList();
        Map.Entry me = (Map.Entry) rs.get(0);

        assertTrue(d.equals(me.getKey()) || d2.equals(me.getKey()));
        
        query = "select KEY(e) from Compny2 c, " +
            " in (c.orgs) e order by c.id";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Compny2.class);
        rs = q.getResultList();
        d = (Division) rs.get(0);
        d2 = (Division) rs.get(1);

        em.clear();
        query = "select ENTRY(e) from Compny2 c, " +
            " in (c.orgs) e order by c.id";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Compny2.class);
        rs = q.getResultList();
        me = (Map.Entry) rs.get(0);

        assertTrue(d.equals(me.getKey()) || d2.equals(me.getKey()));
        
        // new tests for element collection
        em.clear();
        query = "select im from Item1 i, in (i.images) im " +
            " order by VALUE(im)";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Item1.class);
        rs = q.getResultList();
        assertEquals(numItems * numImagesPerItem, rs.size());
        
        em.clear();
        query = "select im from Item1 i, in (i.images) im " +
            " where VALUE(im) = 'file11'";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Item1.class);
        rs = q.getResultList();
        assertEquals(1, rs.size());
        
        em.clear();
        query = "select im from Item1 i, in (i.images) im " +
            " group by im " +
            " having VALUE(im) like 'file1%'";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Item1.class);
        rs = q.getResultList();
        assertEquals(numImagesPerItem, rs.size());       

        em.close();
    }

    public void testQueryObj() throws Exception {
        queryObj(emf);
    }

    public void createObj(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        for (int i = 0; i < numItems; i++) {
            createItem1(em, itemId++);
            em.flush();
        }
        for (int i = 0; i < numItems; i++) {
            createItem2(em, itemId++);
            em.flush();
        }
        for (int i = 0; i < numCompany; i++) {
            createCompany1(em, compId++);
            em.flush();
        }
        for (int i = 0; i < numCompany; i++) {
            createCompany2(em, compId++);
            em.flush();
        }
        em.flush();
        tran.commit();
        em.close();
    }

    public void createItem1(EntityManager em, int id) {
        Item1 item = new Item1();
        item.setId(id);
        for (int i = 0; i < numImagesPerItem; i++) {
            item.addImage("image" + id + i, "file" + id + i);
        }
        em.persist(item);
    }

    public void createItem2(EntityManager em, int id) {
        Item2 item = new Item2();
        item.setId(id);
        for (int i = 0; i < numImagesPerItem; i++) {
            item.addImage("image" + id + i, "file" + id + i);
        }
        em.persist(item);
    }

    public void createCompany1(EntityManager em, int id) {
        Compny1 c = new Compny1();
        c.setId(id);
        for (int i = 0; i < numDivisionsPerCo; i++) {
            Division d = createDivision(em, divId++);
            VicePresident vp = createVicePresident(em, vpId++);
            c.addToOrganization(d, vp);
            em.persist(d);
            em.persist(vp);
        }
        em.persist(c);
    }

    public void createCompany2(EntityManager em, int id) {
        Compny2 c = new Compny2();
        c.setId(id);
        for (int i = 0; i < numDivisionsPerCo; i++) {
            Division d = createDivision(em, divId++);
            VicePresident vp = createVicePresident(em, vpId++);
            c.addToOrganization(d, vp);
            em.persist(d);
            em.persist(vp);
        }
        em.persist(c);
    }

    public Division createDivision(EntityManager em, int id) {
        Division d = new Division();
        d.setId(id);
        d.setName("d" + id);
        return d;
    }

    public VicePresident createVicePresident(EntityManager em, int id) {
        VicePresident vp = new VicePresident();
        vp.setId(id);
        vp.setName("vp" + id);
        return vp;
    }

    public void findObj(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();

        Item1 item1 = em.find(Item1.class, 1);
        assertItem1(item1);

        Item2 item2 = em.find(Item2.class, 4);
        assertItem2(item2);

        Compny1 c1 = em.find(Compny1.class, 1);
        assertCompany1(c1);

        Compny2 c2 = em.find(Compny2.class, 3);
        assertCompany2(c2);

        Division d = em.find(Division.class, 1);
        assertDivision(d);

        VicePresident vp = em.find(VicePresident.class, 1);
        assertVicePresident(vp);
        em.close();
    }

    public void assertItem1(Item1 item) {
        int id = item.getId();
        Map<String, String> images = item.getImages();
        Assert.assertEquals(numImagesPerItem, images.size());
    }

    public void assertItem2(Item2 item) {
        int id = item.getId();
        Map<String, String> images = item.getImages();
        Assert.assertEquals(numImagesPerItem, images.size());
    }

    public void assertCompany1(Compny1 c) {
        int id = c.getId();
        Map<Division, VicePresident> organization = c.getOrganization();
        Assert.assertEquals(2,organization.size());
    }

    public void assertCompany2(Compny2 c) {
        int id = c.getId();
        Map<Division, VicePresident> organization = c.getOrganization();
        Assert.assertEquals(2,organization.size());
    }    

    public void assertDivision(Division d) {
        int id = d.getId();
        String name = d.getName();
    }

    public void assertVicePresident(VicePresident vp) {
        int id = vp.getId();
        String name = vp.getName();
    }

    public void queryObj(EntityManagerFactory emf) {
        queryItem(emf);
        queryCompany(emf);
        queryDivision(emf);
        queryVicePresident(emf);
    }

    public void queryItem(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q1 = em.createQuery("select i from Item1 i");
        List<Item1> is1 = q1.getResultList();
        for (Item1 item : is1){
            assertItem1(item);
        }

        Query q2 = em.createQuery("select i from Item2 i");
        List<Item2> is2 = q2.getResultList();
        for (Item2 item : is2){
            assertItem2(item);
        }
        tran.commit();
        em.close();
    }

    public void queryCompany(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q1 = em.createQuery("select c from Compny1 c");
        List<Compny1> cs1 = q1.getResultList();
        for (Compny1 c : cs1){
            assertCompany1(c);
        }
        Query q2 = em.createQuery("select c from Compny2 c");
        List<Compny2> cs2 = q2.getResultList();
        for (Compny2 c : cs2){
            assertCompany2(c);
        }
        tran.commit();
        em.close();
    }

    public void queryDivision(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select d from Division d");
        List<Division> ds = q.getResultList();
        for (Division d : ds){
            assertDivision(d);
        }
        tran.commit();
        em.close();
    }

    public void queryVicePresident(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select vp from VicePresident vp");
        List<VicePresident> vps = q.getResultList();
        for (VicePresident vp : vps){
            assertVicePresident(vp);
        }
        tran.commit();
        em.close();
    }    
}
