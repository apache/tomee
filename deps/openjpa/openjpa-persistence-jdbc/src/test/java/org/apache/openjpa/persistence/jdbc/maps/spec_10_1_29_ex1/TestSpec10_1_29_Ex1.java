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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_29_ex1;

import java.util.ArrayList;
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

public class TestSpec10_1_29_Ex1 extends SQLListenerTestCase {
    public int numCompany = 2;
    public int numDivisionsPerCo = 2;
    public List rsAllCompany = null;
    
    public int compId = 1;
    public int divId = 1;
    public int vpId = 1;
    
    public void setUp() {
        super.setUp(CLEAR_TABLES,
            Company.class,
            Division.class,
            VicePresident.class);
        createObj(emf);
        rsAllCompany = getAll(Company.class);
    }

    public void testQueryObj() throws Exception {
        queryObj(emf);
    }

    @AllowFailure
    public void testQueryInMemoryQualifiedId() throws Exception {
        queryQualifiedId(true);
    }
    
    public void testQueryQualifiedId() throws Exception {
        queryQualifiedId(false);
    }

    public void setCandidate(Query q, Class clz) 
        throws Exception {
        org.apache.openjpa.persistence.QueryImpl q1 = 
            (org.apache.openjpa.persistence.QueryImpl) q;
        org.apache.openjpa.kernel.Query q2 = q1.getDelegate();
        org.apache.openjpa.kernel.QueryImpl qi = (QueryImpl) q2;
        if (clz == Company.class)
            qi.setCandidateCollection(rsAllCompany);
    }

    public void queryQualifiedId(boolean inMemory) throws Exception {
        EntityManager em = emf.createEntityManager();

        String query = "select KEY(e), e from Company c, " +
            " in (c.organization) e order by c.id";
        Query q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Company.class);
        List rs = q.getResultList();
        Division d = (Division) ((Object[]) rs.get(0))[0];
        VicePresident v = (VicePresident) ((Object[]) rs.get(0))[1];

        query = "select KEY(e), b from Company c, " +
            " in (c.organization) e, in(KEY(e).branches) b order by b";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Company.class);
        rs = q.getResultList();
        String branch = (String) ((Object[]) rs.get(0))[1];
        assertEquals(branch, "branch0");

        em.clear();
        query = "select ENTRY(e) from Company c, " +
            " in (c.organization) e order by c.id";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Company.class);
        rs = q.getResultList();
        Map.Entry me = (Map.Entry) rs.get(0);
        assertTrue(d.equals(me.getKey()));
        assertEquals(v.getId(), ((VicePresident) me.getValue()).getId());

        em.clear();
        query = "select KEY(e), e from Company c " +
            " left join c.organization e order by c.id";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Company.class);
        rs = q.getResultList();
        d = (Division) ((Object[]) rs.get(0))[0];
        v = (VicePresident) ((Object[]) rs.get(0))[1];

        em.clear();
        query = "select ENTRY(e) from Company c " +
            " left join c.organization e order by c.id";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Company.class);
        rs = q.getResultList();
        me = (Map.Entry) rs.get(0);
        assertTrue(d.equals(me.getKey()));
        assertEquals(v.getId(), ((VicePresident) me.getValue()).getId());

        em.close();
    }

    protected List<String> sql = new ArrayList<String>();
    protected int sqlCount;

    public List<String> getSql() {
        return sql;
    }

    public int getSqlCount() {
        return sqlCount;
    }

    public void createObj(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numCompany; i++)
            createCompany(em, compId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createCompany(EntityManager em, int id) {
        Company c = new Company();
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
        for (int i = 0; i < 2; i++) {
            d.addBranch("branch"+i);
        }
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
        Company c = em.find(Company.class, 1);
        assertCompany(c);

        Division d = em.find(Division.class, 1);
        assertDivision(d);

        VicePresident vp = em.find(VicePresident.class, 1);
        assertVicePresident(vp);

        em.close();
    }

    public void assertCompany(Company c) {
        int id = c.getId();
        Map organization = c.getOrganization();
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
        queryCompany(emf);
        queryDivision(emf);
        queryVicePresident(emf);
    }

    public void queryCompany(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select c from Company c");
        List<Company> cs = q.getResultList();
        for (Company c : cs){
            assertCompany(c);
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
