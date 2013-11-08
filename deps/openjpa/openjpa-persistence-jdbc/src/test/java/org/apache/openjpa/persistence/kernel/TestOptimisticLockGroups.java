/*
 * TestOptimisticLockGroups.java
 *
 * Created on October 12, 2006, 2:57 PM
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



import org.apache.openjpa.persistence.kernel.common.apps.LockGroupPC;
import org.apache.openjpa.persistence.kernel.common.apps.LockGroupPC2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestOptimisticLockGroups extends BaseKernelTest {

    private Object oid;

    /**
     * Creates a new instance of TestOptimisticLockGroups
     */
    public TestOptimisticLockGroups() {
    }

    public TestOptimisticLockGroups(String name) {
        super(name);
    }

    public void setUp() {
        deleteAll(LockGroupPC.class);

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        Object o = new LockGroupPC();
        pm.persist(o);
        endTx(pm);
        oid = pm.getObjectId(o);
        endEm(pm);
    }

    public void testDefaultLockGroupFailure1() {
        OpenJPAEntityManager pm1 = getPM(false, false);
        startTx(pm1);
        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class, oid);
        pc1.setDefaultLockGroupStringField("pm1 value");

        OpenJPAEntityManager pm2 = getPM(false, false);
        startTx(pm2);
        LockGroupPC pc2 = (LockGroupPC) pm2.find(LockGroupPC.class, oid);
        pc2.setDefaultLockGroupStringField("pm2 value");

        endTx(pm1);
        endEm(pm1);

        try {
            endTx(pm2);
            fail("should not be able to commit change to same value");
        } catch (Exception e) {
            assertEquals(pc2, getFailedObject(e));
        }
        endEm(pm2);
    }

    public void testDefaultLockGroupFailure2() {
        OpenJPAEntityManager pm1 = getPM(false, false);
        startTx(pm1);
        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class, oid);
        pc1.setDefaultLockGroupStringField("pm1 value");

        OpenJPAEntityManager pm2 = getPM(false, false);
        startTx(pm2);
        LockGroupPC pc2 = (LockGroupPC) pm2.find(LockGroupPC.class, oid);
        pc2.setExplicitDefaultLockGroupIntField(2);

        endTx(pm1);
        endEm(pm1);

        try {
            endTx(pm2);
            fail("should not be able to commit change to same value");
        } catch (Exception e) {
            assertEquals(pc2, getFailedObject(e));
        }
        endEm(pm2);
    }

    public void testNonDefaultLockGroupFailure1() {
        OpenJPAEntityManager pm1 = getPM(false, false);
        startTx(pm1);
        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class, oid);
        pc1.setLockGroup0IntField(1);

        OpenJPAEntityManager pm2 = getPM(false, false);
        startTx(pm2);
        LockGroupPC pc2 = (LockGroupPC) pm2.find(LockGroupPC.class, oid);
        pc2.setLockGroup0IntField(2);

        endTx(pm1);
        endEm(pm1);

        try {
            endTx(pm2);
            fail("should not be able to commit change to same value");
        } catch (Exception e) {
            assertEquals(pc2, getFailedObject(e));
        }
        endEm(pm2);
    }

    public void testNonDefaultLockGroupFailure2() {
        OpenJPAEntityManager pm1 = getPM(false, false);
        startTx(pm1);
        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class, oid);
        pc1.setLockGroup0IntField(1);

        OpenJPAEntityManager pm2 = getPM(false, false);
        startTx(pm2);
        LockGroupPC pc2 = (LockGroupPC) pm2.find(LockGroupPC.class, oid);
        pc2.setLockGroup0StringField("pm2");

        endTx(pm1);
        endEm(pm1);

        try {
            endTx(pm2);
            fail("should not be able to commit change to same value");
        } catch (Exception e) {
            assertEquals(pc2, getFailedObject(e));
        }
        endEm(pm2);
    }

    public void testMultipleLockGroupSuccess1() {
        OpenJPAEntityManager pm1 = getPM(false, false);
        startTx(pm1);
        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class, oid);
        pc1.setDefaultLockGroupStringField("pm1 value");
        pc1.setExplicitDefaultLockGroupIntField(1);

        OpenJPAEntityManager pm2 = getPM(false, false);
        startTx(pm2);
        LockGroupPC pc2 = (LockGroupPC) pm2.find(LockGroupPC.class, oid);
        pc2.setLockGroup0IntField(2);

        OpenJPAEntityManager pm3 = getPM(false, false);
        startTx(pm3);
        LockGroupPC pc3 = (LockGroupPC) pm3.find(LockGroupPC.class, oid);
        pc3.setLockGroup1RelationField(new RuntimeTest1());

        endTx(pm1);
        endEm(pm1);

        endTx(pm2);
        endEm(pm2);

        endTx(pm3);
        endEm(pm3);
    }

    public void testMultipleLockGroupSuccess2() {
        OpenJPAEntityManager pm1 = getPM(false, false);
        startTx(pm1);
        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class, oid);
        pc1.setDefaultLockGroupStringField("pm1 value");
        pc1.setLockGroup0IntField(1);

        OpenJPAEntityManager pm2 = getPM(false, false);
        startTx(pm2);
        LockGroupPC pc2 = (LockGroupPC) pm2.find(LockGroupPC.class, oid);
        pc2.setLockGroup1RelationField(new RuntimeTest1());

        endTx(pm2);
        endEm(pm2);

        endTx(pm1);
        endEm(pm1);
    }

//        FIX ME: aokeke - test is taking so much resource and causing
//        subsequent test to fail
//        public void testNoLockGroupSuccess() {
//        OpenJPAEntityManager pm1 = getPM(false, false);
//        startTx(pm1,());
//        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class,oid);
//        pc1.setDefaultLockGroupStringField("pm1 value");
//        pc1.setLockGroup0IntField(1);
//        pc1.setUnlockedStringField("pm1 value");
//        
//        OpenJPAEntityManager pm2 = getPM(false, false);
//        startTx(pm2,());
//        LockGroupPC pc2 = (LockGroupPC) pm2.find(LockGroupPC.class,oid);
//        pc2.setLockGroup1RelationField(new RuntimeTest1());
//        pc2.setUnlockedStringField("pm2 value");
//        
//        endTx(pm1,());
//        endEm(pm1);
//        
//        endTx(pm2,());
//        endEm(pm2);
//    }

    public void testAttachDetachSuccess()
        throws Exception {
        OpenJPAEntityManager pm1 = getPM(false, false);
        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class, oid);
        Object detached = pm1.detachCopy(pc1);
        startTx(pm1);
        pc1.setLockGroup0IntField(1);
        pc1.setUnlockedStringField("pm1 changed value");
        endTx(pm1);
        endEm(pm1);

        /*
         // won't work without non-transient detached state
         ByteArrayOutputStream baos = new ByteArrayOutputStream ();
         ObjectOutputStream oos = new ObjectOutputStream (baos);
         oos.writeObject (detached);
         oos.close ();
         baos.close ();

         ByteArrayInputStream bais =
             new ByteArrayInputStream (baos.toByteArray ());
         ObjectInputStream ois = new ObjectInputStream (bais);
         LockGroupPC clone = (LockGroupPC) ois.readObject ();
        */
        LockGroupPC clone = (LockGroupPC) detached;
        clone.setLockGroup1IntField(2);
        clone.setUnlockedStringField("pm2 value");

        OpenJPAEntityManager pm2 = getPM(false, false);
        startTx(pm2);
        pm2.merge(clone);
        endTx(pm2);
        endEm(pm2);
    }

    public void testAttachDetachFailure()
        throws Exception {
        OpenJPAEntityManager pm1 = getPM(false, false);
        LockGroupPC pc1 = (LockGroupPC) pm1.find(LockGroupPC.class, oid);
        Object detached = pm1.detachCopy(pc1);
        startTx(pm1);
        pc1.setLockGroup0IntField(1);
        endTx(pm1);
        endEm(pm1);

        /*
         // won't work without non-transient detached state
         ByteArrayOutputStream baos = new ByteArrayOutputStream ();
         ObjectOutputStream oos = new ObjectOutputStream (baos);
         oos.writeObject (detached);
         oos.close ();
         baos.close ();

         ByteArrayInputStream bais =
             new ByteArrayInputStream (baos.toByteArray ());
         ObjectInputStream ois = new ObjectInputStream (bais);
         LockGroupPC clone = (LockGroupPC) ois.readObject ();
        */
        LockGroupPC clone = (LockGroupPC) detached;
        clone.setLockGroup0IntField(2);

        OpenJPAEntityManager pm2 = getPM(false, false);
        startTx(pm2);
        boolean failed = false;
        try {
            pm2.merge(clone);
        } catch (Exception e) {
            failed = true;
        }

        if (failed)
            rollbackTx(pm2);
        else {
            try {
                endTx(pm2);
                fail("Allowed conflicting changes");
            } catch (Exception jve) {
            }
        }
    }

    public void testLockGroupNone() {
        OpenJPAEntityManager pm = getPM(false, false);
//        pm.begin();
        deleteAll(LockGroupPC2.class, pm);
        startTx(pm);
        LockGroupPC2 pc = new LockGroupPC2();
        pc.setName("pc");
        pm.persist(pc);
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        pm = getPM(false, false);
        pc = (LockGroupPC2) pm.find(LockGroupPC2.class, oid);
        startTx(pm);
        pc.getList().add("foo");

        OpenJPAEntityManager pm2 = getPM(false, false);
        LockGroupPC2 pc2 = (LockGroupPC2) pm2.find(LockGroupPC2.class, oid);
        startTx(pm2);
        pc2.getList().add("bar");
        endTx(pm2);
        endEm(pm2);

        endTx(pm);
        endEm(pm);

        pm = getPM(false, false);
        pc = (LockGroupPC2) pm.find(LockGroupPC2.class, oid);
        assertEquals(2, pc.getList().size());
        endEm(pm);
    }

//    public void testKnownSubclass() {
//        OpenJPAEntityManager pm = getPM(false, false);
//        LockGroupPCKnownSubclass pc = new LockGroupPCKnownSubclass();
//        pc.setDefaultLockGroupStringField("pc");
//        startTx(pm,());
//        pm.persist(pc);
//        endTx(pm,());
//        
//        startTx(pm,());
//        pc.setKnownSubclassStringField("foo");
//        endTx(pm,());
//    }

    //FIXME jthomas - what do we need to substitute for JDOException ?
//    private Object getFailedObject(JDOException e) {
//        return ((JDOException) e.getNestedExceptions()[0]).getFailedObject();
//    }

    private Object getFailedObject(Exception e) {
        return null;
    }
}
