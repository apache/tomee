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

import java.util.Collection;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test Multi-OneToMany relationship with different fetch types.
 */
public class TestOneToManyWithEagerLazyFetch extends SingleEMFTestCase {

    final int TestUtil1xm_TestRow_Id = 100;

    public void setUp() {
        setUp(Util1xmLf.class, Util1xmRt.class);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        // Initialize Util1xmLf entity
        Util1xmLf lf1xm = new Util1xmLf();
        lf1xm.setId(TestUtil1xm_TestRow_Id);
        lf1xm.setFirstName("loaded firstName");

        Util1xmRt rt1xm1 = new Util1xmRt();
        rt1xm1.setId(TestUtil1xm_TestRow_Id + 11);
        rt1xm1.setLastName("loaded lastName1");
        lf1xm.addUniRightLzy(rt1xm1);
        
        Util1xmRt rt1xm2 = new Util1xmRt();
        rt1xm2.setId(TestUtil1xm_TestRow_Id + 12);
        rt1xm2.setLastName("loaded lastName2");
        lf1xm.addUniRightLzy(rt1xm2);

        Util1xmRt rt1xm3 = new Util1xmRt();
        rt1xm3.setId(TestUtil1xm_TestRow_Id + 21);
        rt1xm3.setLastName("loaded eager lastName3");
        lf1xm.addUniRightEgr(rt1xm3);
        
        Util1xmRt rt1xm4 = new Util1xmRt();
        rt1xm4.setId(TestUtil1xm_TestRow_Id + 22);
        rt1xm4.setLastName("loaded eager lastName4");
        lf1xm.addUniRightEgr(rt1xm4);

        em.persist(rt1xm1);
        em.persist(rt1xm2);
        em.persist(rt1xm3);
        em.persist(rt1xm4);
        em.persist(lf1xm);

        em.getTransaction().commit();
        em.close();
    }

    public void testLoadedOneToManyCount() {
        EntityManager em = emf.createEntityManager();
        Util1xmLf e1 = em.find(Util1xmLf.class, TestUtil1xm_TestRow_Id);
        // SELECT t0.firstName, t1.UTIL1XMLF_ID, t2.id, t2.lastName FROM Util1xmLf t0 
        //      LEFT OUTER JOIN Util1xmLf_Util1xmRt t1 ON t0.id = t1.UTIL1XMLF_ID 
        //      LEFT OUTER JOIN Util1xmRt t2 ON t1.UNIRIGHTEGR_ID = t2.id WHERE t0.id = ? 
        //      [params=(int) 100]

        assertNotNull("Found Util1xmLf(id=" + TestUtil1xm_TestRow_Id + ")", e1);
        
        Collection<Util1xmRt> eRs = e1.getUniRightLzy();
        // SELECT t1.id, t1.lastName FROM Util1xmLf_Util1xmRt t0 
        //      INNER JOIN Util1xmRt t1 ON t0.UNIRIGHTLZY_ID = t1.id WHERE t0.UTIL1XMLF_ID = ? 
        //      [params=(int) 100]
        assertNotNull("Util1xmRt uniRightLzy != null", eRs);
        assertEquals("Util1xmRt uniRightLzy.size == 2", eRs.size(), 2);
        
        Collection<Util1xmRt> eEs = e1.getUniRightEgr();
        assertNotNull("Util1xmRt uniRightEgr != null", eEs);
        // Failing test: Getting 3 in eager collection, one null entry
        assertEquals("Util1xmRt uniRightEgr.size == 2", eEs.size(), 2);

        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestOneToManyWithEagerLazyFetch.class);
    }
}
