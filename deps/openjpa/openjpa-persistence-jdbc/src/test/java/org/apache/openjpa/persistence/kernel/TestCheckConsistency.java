/*
 * TestCheckConsistency.java
 *
 * Created on October 9, 2006, 6:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.kernel;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;

import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestCheckConsistency extends BaseKernelTest {

    private Object _oid = null;

    /**
     * Creates a new instance of TestCheckConsistency
     */
    public TestCheckConsistency(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class);

        RuntimeTest1 pc = new RuntimeTest1();
        pc.setIntField(1);
        pc.setIntField1(1);
        _oid = persist(pc);
    }

    public void testConsistentDatastoreTransaction() {

        OpenJPAEntityManager pm = getPM();
        pm.setOptimistic(false);
        pm.validateChanges();        // no-op outside trans
        startTx(pm);

        RuntimeTest1 pc = pm.find(RuntimeTest1.class, _oid);
        pc.setIntField1(100);

        RuntimeTest1 npc = new RuntimeTest1();
        npc.setIntField(2);
        npc.setIntField1(2);
        pm.persist(npc);
        pm.validateChanges();

        assertEquals(100, pc.getIntField1());
        assertTrue(pm.isPersistent(npc));

        pc.setIntField1(200);
        npc.setIntField1(300);
        endTx(pm);

        assertEquals(200, pc.getIntField1());
        assertTrue(pm.isPersistent(npc));
        assertEquals(300, npc.getIntField1());
        endEm(pm);
    }

    public void testConsistentDatastoreTransactionWithRollback() {
        OpenJPAEntityManager pm = getPM();
        pm.setOptimistic(false);
        pm.validateChanges();        // no-op outside trans
        startTx(pm);

        RuntimeTest1 pc = pm.find(RuntimeTest1.class, _oid);
        pc.setIntField1(100);

        RuntimeTest1 npc = new RuntimeTest1();
        pm.persist(npc);
        Object noid = pm.getObjectId(npc);

        pm.validateChanges();
        assertEquals(100, pc.getIntField1());
        assertTrue(pm.isPersistent(npc));

        pc.setIntField1(200);
        npc.setIntField1(300);
        rollbackTx(pm);

        assertEquals(1, pc.getIntField1());
        assertFalse(pm.isPersistent(npc));
        assertEquals(0, npc.getIntField1());
        endEm(pm);

        pm = getPM();
        try {
            RuntimeTest1 temp =
                pm.find(RuntimeTest1.class, noid);
            fail("Object should not exist." + temp.getIntField() + "::" +
                temp.getIntField1());
        } catch (Exception jonfe) {
        }
        endEm(pm);
    }

    //FIXME jthomas
/*    
    public void testInconsistentDatastoreTransaction() {
        OpenJPAEntityManager pm = getPM();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        pm.setOptimistic(false);
        pm.setRetainState(false);
        pm.validateChanges();        // no-op outside trans
        pm.begin();
  */
    //FIXME jthomas
    /*
   fetch.setReadLockLevel(pm.LOCK_NONE);
   fetch.setWriteLockLevel(pm.LOCK_NONE);
    */
    /*
RuntimeTest1 pc = (RuntimeTest1) pm.find(RuntimeTest1.class,_oid);
pc.setIntField(100);

OpenJPAEntityManager pm2 = getPM();
pm2.begin();
RuntimeTest1 copy = (RuntimeTest1) pm2.find(RuntimeTest1.class,_oid);
copy.setIntField(-1);
pm2.commit();
pm2.close();

RuntimeTest1 npc = new RuntimeTest1();
pm.persist(npc);

try {
   pm.validateChanges();
   fail("Didn't find inconsistency.");
} catch (Exception jove) {
   //FIXME
   /*
   Throwable[] t = jove.getNestedExceptions();
   assertEquals(1, t.length);
   assertEquals(pc, (((JDOException) t[0]).getFailedObject()));
    */
//        }

    /*        assertTrue(pm.getRollbackOnly());
           pm.rollback();

           assertEquals(-1, pc.getIntField());
           assertFalse(pm.isPersistent(npc));
           endEm(pm,());
       }
    */
    public void testConsistentOptimisticTransaction() {
        OpenJPAEntityManager pm = getPM();
        pm.setOptimistic(true);
        pm.validateChanges();        // no-op outside trans
        startTx(pm);
        boolean hasConn = hasConnection(pm);

        RuntimeTest1 pc = pm.find(RuntimeTest1.class, _oid);
        pc.setIntField1(100);

        RuntimeTest1 npc = new RuntimeTest1();
        npc.setIntField(2);
        npc.setIntField1(2);
        pm.persist(npc);

        pm.validateChanges();
        if (!hasConn)
            assertFalse(hasConnection(pm));

        assertEquals(100, pc.getIntField1());
        assertTrue(pm.isPersistent(npc));

        pc.setIntField1(200);
        npc.setIntField1(300);
        endTx(pm);

        assertEquals(200, pc.getIntField1());
        assertTrue(pm.isPersistent(npc));
        assertEquals(300, npc.getIntField1());
        endEm(pm);
    }

    private boolean hasConnection(OpenJPAEntityManager pm) {
        return JPAFacadeHelper.toBroker(pm).hasConnection();
    }

    public void testConsistentOptimisticTransactionWithRollback() {
        OpenJPAEntityManager pm = getPM();
        pm.setOptimistic(true);
        pm.validateChanges();        // no-op outside trans
        startTx(pm);
        boolean hasConn = hasConnection(pm);

        RuntimeTest1 pc = pm.find(RuntimeTest1.class, _oid);
        pc.setIntField1(100);

        RuntimeTest1 npc = new RuntimeTest1();
        pm.persist(npc);
        Object noid = pm.getObjectId(npc);

        pm.validateChanges();
        if (!hasConn)
            assertFalse(hasConnection(pm));

        assertEquals(100, pc.getIntField1());
        assertTrue(pm.isPersistent(npc));

        pc.setIntField1(200);
        npc.setIntField1(300);
        rollbackTx(pm);

        assertEquals(1, pc.getIntField1());
        assertFalse(pm.isPersistent(npc));
        assertEquals(0, npc.getIntField1());
        endEm(pm);

        pm = getPM();
        try {
            RuntimeTest1 temp =
                pm.find(RuntimeTest1.class, noid);

            fail("Object should not exist." + temp.getIntField() + "::" +
                temp.getIntField1());
        } catch (Exception jonfe) {
        }
    }
//FIXME 
    /*
public void testInconsistentOptimisticTransactionWithoutRefresh() {
OpenJPAEntityManager pm = getPM();
pm.setRetainState(false);
pm.setOptimistic(true);
pm.validateChanges();        // no-op outside trans
pm.begin();

RuntimeTest1 pc = (RuntimeTest1) pm.find(RuntimeTest1.class,_oid);
pc.setIntField(100);

OpenJPAEntityManager pm2 = getPM();
pm2.begin();
RuntimeTest1 copy = (RuntimeTest1) pm2.find(RuntimeTest1.class,_oid);
copy.setIntField(-1);
pm2.commit();
pm2.close();

RuntimeTest1 npc = new RuntimeTest1();
pm.persist(npc);
Object noid = pm.getObjectId(npc);

try {
   pm.validateChanges();
   fail("Didn't find inconsistency.");
} catch (Exception jove) {
   //FIXME jthomas
   /*
   Throwable[] t = jove.getNestedExceptions();
   assertEquals(1, t.length);
   assertEquals(pc, (((JDOException) t[0]).getFailedObject()));
    */
    //     }
/*     assertFalse(pm.getRollbackOnly());
        
        try {
            pm.commit();
            fail("Committed inconsistent transaction.");
        } catch (Exception je) {
        }
        
        assertEquals(-1, pc.getIntField());
        assertFalse(pm.isPersistent(npc));
        endEm(pm,());
        
        pm = getPM();
        try {
            pm.find(RuntimeTest1.class,noid);
            fail("Object should not exist.");
        } catch (Exception jonfe) {
        }
        endEm(pm,());
    }
 */

//FIXME 
/*    
    public void testInconsistentOptimisticTransactionWithRefresh() {
        OpenJPAEntityManager pm = getPM();
        pm.setOptimistic(true);
        pm.validateChanges();        // no-op outside trans
        pm.begin();
        
        RuntimeTest1 pc = pm.find(RuntimeTest1.class,_oid);
        pc.setIntField(100);
        
        OpenJPAEntityManager pm2 = getPM();
        pm2.begin();
        RuntimeTest1 copy = pm2.find(RuntimeTest1.class,_oid);
        copy.setIntField(-1);
        pm2.commit();
        pm2.close();
        
        RuntimeTest1 npc = new RuntimeTest1();
        pm.persist(npc);
        try {
            pm.validateChanges();
            fail("Didn't find inconsistency.");
        } catch (Exception jove) {
            //FIXME jthomas
            /*
            Throwable[] t = jove.getNestedExceptions();
            assertEquals(1, t.length);
            assertEquals(pc, (((JDOException) t[0]).getFailedObject()));
             */
    //      }
    /*       assertFalse(pm.getRollbackOnly());
         pm.refresh(pc);

         assertEquals(-1, pc.getIntField());
         assertTrue(pm.isPersistent(npc));

         pc.setIntField(200);
         npc.setIntField(300);
         pm.commit();

         assertEquals(200, pc.getIntField());
         assertTrue(pm.isPersistent(npc));
         assertEquals(300, npc.getIntField());
         endEm(pm,());
     }
    */

//FIXME 

/*    
public void testInconsistentOptimisticTransactionWithRollback() {
OpenJPAEntityManager pm = getPM();
pm.setRetainState(false);
pm.setOptimistic(true);
pm.validateChanges();        // no-op outside trans
pm.begin();

RuntimeTest1 pc = (RuntimeTest1) pm.find(RuntimeTest1.class,_oid);
pc.setIntField(100);

OpenJPAEntityManager pm2 = getPM();
pm2.begin();
RuntimeTest1 copy = (RuntimeTest1) pm2.find(RuntimeTest1.class,_oid);
copy.setIntField(-1);
pm2.commit();
pm2.close();

RuntimeTest1 npc = new RuntimeTest1();
pm.persist(npc);
Object noid = pm.getObjectId(npc);

try {
pm.validateChanges();
fail("Didn't find inconsistency.");
} catch (Exception jove) {
//FIXME jthomas
/*
Throwable[] t = jove.getNestedExceptions();
assertEquals(1, t.length);
assertEquals(pc, (((JDOException) t[0]).getFailedObject()));
*/
    //      }
    /*       assertFalse(pm.getRollbackOnly());
          pm.rollback();

          assertEquals(-1, pc.getIntField());
          assertFalse(pm.isPersistent(npc));
          endEm(pm,());

          pm = getPM();
          try {
              pm.find(RuntimeTest1.class,_oid);
              fail("Object should not exist.");
          } catch (Exception jonfe) {
          }
          endEm(pm,());
      }

    */
}
