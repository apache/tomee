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
package org.apache.openjpa.persistence.jdbc.kernel;



import java.util.*;

import org.apache.openjpa.persistence.jdbc.common.apps.*;
import org.apache.openjpa.persistence.common.utils.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;


public class TestOperationOrderUpdateManager
    extends AbstractTestCase {


//	private boolean  = true;//Boolean.valueOf(bool);


    private static boolean _mapped = false;

    private EntityManagerFactory emf;

    /*protected boolean skipTest() {
        if (!getName().startsWith("testAuto"))
            return false;

		emf = getEmf();

        JDBCConfiguration conf =
            (JDBCConfiguration) ((OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.cast(emf)).getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        return !dict.supportsAutoAssign;
    }
*/
    public TestOperationOrderUpdateManager(String name)
    {
    	super(name);
    }

    private void insertTest(boolean autoAssign) {
        OpOrder oid = (autoAssign) ? insertAutoAssign() : insert();

        EntityManager em = currentEntityManager();

        OpOrder pc = (OpOrder) em.find(OpOrder.class,oid.getId());
        assertEquals("pc", pc.getSecondary());
        assertEquals("pcsub", pc.getSub());
        assertNotNull(pc.getRel());
        assertEquals("rel", pc.getRel().getSecondary());
        assertEquals(2, pc.getRelList().size());
        assertTrue(((OpOrder) pc.getRelList().iterator().next()).
            getSecondary().startsWith("child"));
        assertEquals(2, pc.getMappedRelList().size());
        assertTrue(((OpOrder) pc.getMappedRelList().iterator().next()).
            getSecondary().startsWith("mapped"));
        assertEquals(2, pc.getInverseKeyRelList().size());
        assertTrue(((OpOrder) pc.getInverseKeyRelList().iterator().
            next()).getSecondary().startsWith("inverse"));
        em.close();
    }



    private OpOrder insert() {
        OpOrderPCSub pc = new OpOrderPCSub();
        pc.setSecondary("pc");
        pc.setSub("pcsub");
        OpOrderPCSub rel = new OpOrderPCSub();
        rel.setSecondary("rel");
        pc.setRel(rel);
        for (int i = 0; i < 2; i++) {
            OpOrderPCSub child = new OpOrderPCSub();
            child.setSecondary("child" + i);
            pc.getRelList().add(child);

            OpOrderPCSub mapped = new OpOrderPCSub();
            mapped.setSecondary("mapped" + i);
            mapped.setOwner(pc);
            pc.getMappedRelList().add(mapped);

            OpOrderPCSub inverse = new OpOrderPCSub();
            inverse.setSecondary("inverse" + i);
            pc.getInverseKeyRelList().add(inverse);
        }

        EntityManager em = currentEntityManager();
		startTx(em);
        em.persist(rel);
        persistAll(em,pc.getInverseKeyRelList());
        em.persist(pc);
        endTx(em);
        OpOrder oid = em.find(OpOrder.class,pc.getId());
        //em.getObjectId(pc);
        em.close();
        return oid;
    }

    private OpOrder insertAutoAssign() {
        AutoIncrementOpOrderPCSub pc = new AutoIncrementOpOrderPCSub();
        pc.setSecondary("pc");
        pc.setSub("pcsub");
        AutoIncrementOpOrderPCSub rel = new AutoIncrementOpOrderPCSub();
        rel.setSecondary("rel");
        pc.setRel(rel);
        for (int i = 0; i < 2; i++) {
            AutoIncrementOpOrderPCSub child = new AutoIncrementOpOrderPCSub();
            child.setSecondary("child" + i);
            pc.getRelList().add(child);

            AutoIncrementOpOrderPCSub mapped = new AutoIncrementOpOrderPCSub();
            mapped.setSecondary("mapped" + i);
            mapped.setOwner(pc);
            pc.getMappedRelList().add(mapped);

            AutoIncrementOpOrderPCSub inverse = new AutoIncrementOpOrderPCSub();
            inverse.setSecondary("inverse" + i);
            pc.getInverseKeyRelList().add(inverse);
        }

        EntityManager em = currentEntityManager();
        startTx(em);
        em.persist(rel);
        persistAll(em,pc.getInverseKeyRelList());
        em.persist(pc);
        endTx(em);
        OpOrder oid = em.find(AutoIncrementOpOrderPCSub.class,pc.getId());
        em.close();
        return oid;
    }


    private void insertCircularConstraintTest(boolean autoAssign) {
        OpOrder oid = (autoAssign) ? insertCircularAutoAssign()
            : insertCircular();

        EntityManager em = currentEntityManager();
        OpOrder pc1 = (OpOrder) em.find(OpOrder.class,oid.getId());
        assertEquals("pc1", pc1.getSecondary());
        assertNotNull(pc1.getRel());
        assertEquals("pc2", pc1.getRel().getSecondary());
        assertNotNull(pc1.getRel().getRel());
        assertEquals(pc1, pc1.getRel().getRel());
        em.close();
    }

    private OpOrder insertCircular() {
        OpOrderPCSub pc1 = new OpOrderPCSub();
        pc1.setSecondary("pc1");
        pc1.setSub("pcsub");
        OpOrderPCSub pc2 = new OpOrderPCSub();
        pc2.setSecondary("pc2");
        pc1.setRel(pc2);
        pc2.setRel(pc1);

        EntityManager em = currentEntityManager();
        startTx(em);
        em.persist(pc1);
        em.persist(pc2);
        endTx(em);
        OpOrder oid = em.find(OpOrder.class,pc1.getId());
        em.close();
        return oid;
    }

    private OpOrder insertCircularAutoAssign() {
        AutoIncrementOpOrderPCSub pc1 = new AutoIncrementOpOrderPCSub();
        pc1.setSecondary("pc1");
        pc1.setSub("pcsub");
        AutoIncrementOpOrderPCSub pc2 = new AutoIncrementOpOrderPCSub();
        pc2.setSecondary("pc2");
        pc1.setRel(pc2);
        pc2.setRel(pc1);

        EntityManager em = currentEntityManager();
        startTx(em);
        em.persist(pc1);
        em.persist(pc2);
        endTx(em);
        OpOrder oid = em.find(OpOrder.class,pc1.getId());
        em.close();
        return oid;
    }


    private void deleteCircularTest(boolean autoAssign) {
        OpOrder oid = (autoAssign) ? insertCircularAutoAssign()
            : insertCircular();

        EntityManager em = currentEntityManager();
        startTx(em);
        OpOrder pc1 = (OpOrder) em.find(OpOrder.class,oid.getId());
        em.remove(pc1.getRel());
        em.remove(pc1);
        endTx(em);
        em.close();

/*        pm = _pmf.getPersistenceManager();
        if (autoAssign)
            assertEquals(0, ((KodoExtent) pm.getExtent
                (AutoIncrementOpOrderPCSub.class, true)).list().size());
        else
            assertEquals(0, ((KodoExtent) pm.getExtent(OpOrderPC.class,
                true)).list().size());
        pm.close();
*/
    }

    private void deleteTest(boolean autoAssign) {
        OpOrder oid = (autoAssign) ? insertAutoAssign() : insert();

        EntityManager em = currentEntityManager();
        startTx(em);
        OpOrder pc = (OpOrder) em.find(OpOrder.class,oid.getId());
        removeAll(em,pc.getMappedRelList());
        removeAll(em,pc.getInverseKeyRelList());
        removeAll(em,pc.getRelList());
        em.remove(pc.getRel());
        em.remove(pc);
        endTx(em);
        em.close();
/*
        pm = _pmf.getPersistenceManager();
        if (autoAssign)
            assertEquals(0, ((KodoExtent) pm.getExtent
                (AutoIncrementOpOrderPCSub.class, true)).list().size());
        else
            assertEquals(0, ((KodoExtent) pm.getExtent(OpOrderPC.class,
                true)).list().size());
        pm.close();
*/
    }


    public void testInsert() {
        insertTest(false);
    }

    public void testAutoAssignInsert() {
        insertTest(true);
    }


    public void testDeleteCircular() {
        deleteCircularTest(false);
    }

    public void testAutoAssignDeleteCircular() {
        deleteCircularTest(true);
    }

    public void testDelete() {
        deleteTest(false);
    }

    public void testAutoAssignDelete() {
        deleteTest(true);
    }


    public void testInsertCircularConstraint() {
        insertCircularConstraintTest(false);
    }

    public void testAutoAssignInsertCircularConstraint() {
        insertCircularConstraintTest(true);
    }



    private void persistAll(EntityManager em, List list) {

		Iterator i = list.iterator();
		while(i.hasNext()) {
			Object o = i.next();
			em.persist(o);
		}
	}


	private void removeAll(EntityManager em, List list) {

		Iterator i = list.iterator();
		while(i.hasNext()) {
			Object o = i.next();
			em.remove(o);
		}
	}







}
