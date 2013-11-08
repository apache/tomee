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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;



import org.apache.openjpa.persistence.kernel.common.apps.AttachA;
import org.apache.openjpa.persistence.kernel.common.apps.AttachB;
import org.apache.openjpa.persistence.kernel.common.apps.AttachD;
import org.apache.openjpa.persistence.kernel.common.apps.AttachE;
import org.apache.openjpa.persistence.kernel.common.apps.DetachSMPC;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.persistence.DetachStateType;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;

public class TestDetachedStateManager extends BaseKernelTest {

    private static boolean enhanced = false;

    private int oid;
    private int doid;

    /**
     * Creates a new instance of TestDetachedStateManager
     */
    public TestDetachedStateManager(String name) {
        super(name);
    }

    private void deleteAll() {
        deleteAll(AttachA.class);
        deleteAll(AttachD.class);
    }

    public OpenJPAEntityManager getPM() {
        OpenJPAEntityManager pm = super.getPM();
        //FIXME jthomas
        //pm.currentTransaction().setRestoreValues(false);
        return pm;
    }

    public void setUp() throws Exception {
        super.setUp();

        deleteAll();

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        AttachB b = new AttachB();
        pm.persist(b);
        b.setAint(5);
        b.setBstr("5");
        b.getStringIntMap().put("5", new Integer(5));

        AttachE e = new AttachE();
        e.setEstr("E");
        e.setEint(5);

        AttachD d = new AttachD();
        d.setDint(5);
        d.setEmbeddedE(e);
        b.getDs().add(d);

        pm.persist(d);

        oid = b.getId();
        doid = d.getId();
        endTx(pm);
        endEm(pm);
    }

    public void testDetach() {
        OpenJPAEntityManager pm = getPM();
        AttachB b = pm.find(AttachB.class, oid);

        assertNotNull("b is null in testDetach", b);

        b = (AttachB) pm.detachCopy(b);
        endEm(pm);

        assertTrue(pm.isDetached(b));
        assertEquals(5, b.getAint());
        assertEquals("5", b.getBstr());
        assertNull(b.getStringIntMap());

        b.setAint(12);
        b.setBstr("12");
        TreeMap map = new TreeMap();
        map.put("12", new Integer(12));
        b.setStringIntMap(map);

        pm = getPM();
        startTx(pm);
        AttachB attached = (AttachB) pm.merge(b);
        assertEquals(12, attached.getAint());
        assertEquals("12", attached.getBstr());
        assertNull(attached.getStringIntMap().get("12"));
        assertEquals(new Integer(5), attached.getStringIntMap().get("5"));
        endTx(pm);
        endEm(pm);

        pm = getPM();
        b = pm.find(AttachB.class, oid);
        assertEquals(12, b.getAint());
        assertEquals("12", b.getBstr());
        assertNull(b.getStringIntMap().get("12"));
        assertEquals(new Integer(5), b.getStringIntMap().get("5"));
        endEm(pm);
    }

    public void testDetachWithGroups() {
        OpenJPAEntityManager pm = getPM();
        //FIXME jthomas
//      pm.getFetchPlan().setDetachmentOptions(
//      FetchPlanImpl.DETACH_LOAD_FIELDS | FetchPlanImpl.DETACH_UNLOAD_FIELDS);
        pm.setDetachState(DetachStateType.FETCH_GROUPS);
        pm.getFetchPlan().addFetchGroup("all");
        AttachB b = pm.find(AttachB.class, oid);

        assertNotNull("b is null in testDetachWithGroups", b);

        b = (AttachB) pm.detachCopy(b);
        endEm(pm);

        assertTrue(pm.isDetached(b));
        assertEquals("b.getAint() not 5", 5, b.getAint());
        assertEquals("b.getAint() not 5str", "5", b.getBstr());
        assertEquals("b.getStringIntMap().size() not equal to 1", 1,
            b.getStringIntMap().size());

        b.setAint(12);
        b.setBstr("12");
        b.getStringIntMap().put("12", new Integer(12));

        pm = getPM();
        startTx(pm);
        AttachB attached = (AttachB) pm.merge(b);
        assertEquals("not 12", 12, attached.getAint());
        assertEquals("not 12str", "12", attached.getBstr());
        assertEquals("not newInteger(12)", new Integer(12),
            attached.getStringIntMap().get("12"));
        assertEquals("not newInteger(5)", new Integer(5),
            attached.getStringIntMap().get("5"));
        endTx(pm);
        endEm(pm);

        pm = getPM();
        b = (AttachB) pm.find(AttachB.class, oid);
        assertEquals("not equal 12", 12, b.getAint());
        assertEquals("not equal 12str", "12", b.getBstr());
        assertEquals("not equal newinteger(12)", new Integer(12),
            b.getStringIntMap().get("12"));
        assertEquals("not equal newInteger(5)", new Integer(5),
            b.getStringIntMap().get("5"));
        endEm(pm);
    }

    public void testDetachNoOverwrite() {
        OpenJPAEntityManager pm = getPM();
        AttachB b = (AttachB) pm.find(AttachB.class, oid);
        b = (AttachB) pm.detachCopy(b);
        endEm(pm);

        b.setBstr("12");

        pm = getPM();
        startTx(pm);
        AttachB orig = pm.find(AttachB.class, oid);
        orig.setAint(50);

        AttachB attached = (AttachB) pm.merge(b);
        assertEquals(attached, orig);
        assertEquals(50, attached.getAint());
        assertEquals("12", attached.getBstr());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        b = (AttachB) pm.find(AttachB.class, oid);
        assertEquals(50, b.getAint());
        assertEquals("12", b.getBstr());
        endEm(pm);
    }

    public void testOptimisticLock() {
        OpenJPAEntityManager pm = getPM();
        AttachB b = (AttachB) pm.find(AttachB.class, oid);

        assertNotNull("b is null in testOptimisticLock", b);

        b = (AttachB) pm.detachCopy(b);
        endEm(pm);

        b.setAint(12);
        b.setBstr("12");
        TreeMap map = new TreeMap();
        map.put("12", new Integer(12));
        b.setStringIntMap(map);

        pm = getPM();
        startTx(pm);
        AttachB b2 = (AttachB) pm.find(AttachB.class, oid);
        b2.setAint(15);
        endTx(pm);
        endEm(pm);

        pm = getPM();
        startTx(pm);
        try {
            pm.merge(b);
            endTx(pm);
            fail("OL expected.");
        } catch (Exception jove) {
            rollbackTx(pm);
        }
        endEm(pm);
    }

    public void testEmbedded() {
        OpenJPAEntityManager pm = getPM();
        AttachD d = pm.find(AttachD.class, doid);

        assertNotNull("d is null in testEmbedded", d);

        d.getEmbeddedE().getEstr();
        d = (AttachD) pm.detachCopy(d);
        endEm(pm);

        d.getEmbeddedE().setEstr("E12");
        pm = getPM();
        startTx(pm);
        AttachD d2 = (AttachD) pm.merge(d);
        assertNotEquals(d.getEmbeddedE(), d2.getEmbeddedE());
        assertEquals("E12", d2.getEmbeddedE().getEstr());
        assertEquals(5, d2.getEmbeddedE().getEint());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        d2 = (AttachD) pm.find(AttachD.class, doid);

        assertNotNull("d2 is null in testEmbedded", d2);

        assertEquals("E12", d2.getEmbeddedE().getEstr());
        assertEquals(5, d2.getEmbeddedE().getEint());
        endEm(pm);
    }

    public void testNullEmbedded() {
        OpenJPAEntityManager pm = getPM();
        AttachD d = (AttachD) pm.find(AttachD.class, doid);

        assertNotNull("d is null in testNullEmbedded", d);
        d.getEmbeddedE().getEstr();
        d = (AttachD) pm.detachCopy(d);
        endEm(pm);

        d.setEmbeddedE(null);
        pm = getPM();
        startTx(pm);
        AttachD d2 = (AttachD) pm.merge(d);
        assertNull(d2.getEmbeddedE());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        d2 = (AttachD) pm.find(AttachD.class, doid);

        assertNotNull("d2 is null in testNullEmbedded", d2);
        // no null ind
        if (d2.getEmbeddedE() != null) {
            assertNull(d2.getEmbeddedE().getEstr());
            assertEquals(0, d2.getEmbeddedE().getEint());
        }
        endEm(pm);
    }

    public void testNullEmbeddedRelated() {
        OpenJPAEntityManager pm = getPM();
        AttachD d = (AttachD) pm.find(AttachD.class, doid);

        assertNotNull("d is null in testNullEmbeddedRelated", d);

        d.getEmbeddedE().getEstr();
        d = (AttachD) pm.detachCopy(d);
        endEm(pm);

        d.getEmbeddedE().setEstr(null);
        pm = getPM();
        startTx(pm);
        AttachD d2 = (AttachD) pm.merge(d);
        assertNull("d2.getEmbeddedE().getEstr() is not null",
            d2.getEmbeddedE().getEstr());
        assertEquals("d2.getEmbeddedE().getEint() is not equal to 5", 5,
            d2.getEmbeddedE().getEint());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        d2 = (AttachD) pm.find(AttachD.class, doid);
        assertNull("d2.getEmbeddedE().getEstr() is not null",
            d2.getEmbeddedE().getEstr());
        assertEquals("d2.getEmbeddedE().getEint() is not 5", 5,
            d2.getEmbeddedE().getEint());
        endEm(pm);
    }

    public void testNullCollection() {
        OpenJPAEntityManager pm = getPM();
        AttachB b = (AttachB) pm.find(AttachB.class, oid);
        b.getDs();
        b = (AttachB) pm.detachCopy(b);
        endEm(pm);

        assertEquals(1, b.getDs().size());
        b.setDs(null);

        pm = getPM();
        startTx(pm);
        b = (AttachB) pm.merge(b);
        assertNull(b.getDs());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        b = (AttachB) pm.find(AttachB.class, oid);
        assertTrue(b.getDs() == null || b.getDs().size() == 0);
        endEm(pm);
    }

    public void testCollectionAdd() {
        doCollectionTest(false);
    }

    public void testCollectionChanges() {
        doCollectionTest(true);
    }

    private void doCollectionTest(boolean remove) {
        OpenJPAEntityManager pm = getPM();
        AttachB b = (AttachB) pm.find(AttachB.class, oid);

        assertNotNull("b is null in doCollectionTest", b);
        b.getDs();
        b = (AttachB) pm.detachCopy(b);
        endEm(pm);

        assertEquals("b is null in doCollectionTest", 1, b.getDs().size());
        if (remove) {
            for (Iterator it = b.getDs().iterator(); it.hasNext();) {
                it.next();
                it.remove();
            }
        }
        AttachD d = new AttachD();
        d.setDint(12);
        b.getDs().add(d);

        pm = getPM();
        startTx(pm);
        b = (AttachB) pm.merge(b);
        assertSize(remove ? 1 : 2, b.getDs());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        b = (AttachB) pm.find(AttachB.class, oid);
        assertSize(remove ? 1 : 2, b.getDs());
        boolean found1 = false;
        boolean found2 = false;
        for (Iterator it = b.getDs().iterator(); it.hasNext();) {
            d = (AttachD) it.next();
            switch (d.getDint()) {
                case 5:
                    if (found1)
                        fail("Refound.");
                    found1 = true;
                    break;
                case 12:
                    if (found2)
                        fail("Refound.");
                    found2 = true;
                    break;
                default:
                    fail("Unknown d:" + d.getDint());
            }
        }

        if (remove)
            assertFalse(found1);

        endEm(pm);
    }

    /*
   //###
   // No time to get these working right now.  Have to figure out how to
   // enhance certain classes with different DetachState settings in autobuild.
   public void testSerialization ()
       throws Exception
   {
       doSerializationTest (false);
   }


   public void testSerializationAuto ()
       throws Exception
   {
       doSerializationTest (true);
   }
    */

    private void doSerializationTest(boolean auto) throws Exception {
        enhance();
        Map props = new HashMap();
        props.put("openjpa.DetachState", "DetachedStateField=true");

        OpenJPAEntityManagerFactory factory =
            (OpenJPAEntityManagerFactory) getEmf(props);
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) factory.createEntityManager();

        startTx(pm);
        DetachSMPC pc = new DetachSMPC();
        pc.setIntField(1);
        DetachSMPC rel = new DetachSMPC();
        rel.setIntField(2);
        pc.getRelSet().add(rel);
        pc.getStringIntMap().put("a", new Integer(99));
        pm.persist(pc);
        endTx(pm);
        Object pcoid = pm.getObjectId(pc);
        endEm(pm);

        pm = (OpenJPAEntityManager) factory.createEntityManager();
        pc = (DetachSMPC) pm.find(DetachSMPC.class, pcoid);
        pc.getRelSet();
        pc.getStringIntMap();
        if (!auto) {
            pc = (DetachSMPC) pm.detachCopy(pc);
            assertDetachedSM(pc);
        }
        pc = (DetachSMPC) roundtrip(pc, false);
        assertDetachedSM(pc);
        endEm(pm);

        assertDetachedSM(pc);
        assertSize(1, pc.getRelSet());
        assertEquals(1, pc.getStringIntMap().size());

        pc.setIntField(3);
        ((DetachSMPC) pc.getRelSet().iterator().next()).setIntField(4);
        pc.getStringIntMap().put("b", new Integer(100));

        pc = (DetachSMPC) roundtrip(pc, false);

        assertDetachedSM(pc);
        assertEquals(3, pc.getIntField());
        assertSize(1, pc.getRelSet());
        //assertDetachedSM (b.getDs ().iterator ().next ());
        assertEquals(4, ((DetachSMPC) pc.getRelSet().iterator().next())
            .getIntField());
        assertEquals(new Integer(100), pc.getStringIntMap().get("b"));

        pm = (OpenJPAEntityManager) factory.createEntityManager();
        startTx(pm);
        pc = (DetachSMPC) pm.merge(pc);
        assertEquals(3, pc.getIntField());
        assertSize(1, pc.getRelSet());
        assertEquals(4, ((DetachSMPC) pc.getRelSet().iterator().next())
            .getIntField());
        assertEquals(2, pc.getStringIntMap().size());
        assertEquals(new Integer(100), pc.getStringIntMap().get("b"));
        endTx(pm);
        endEm(pm);

        pm = (OpenJPAEntityManager) factory.createEntityManager();
        pc = (DetachSMPC) pm.find(DetachSMPC.class, pcoid);
        assertEquals(3, pc.getIntField());
        assertSize(1, pc.getRelSet());
        assertEquals(4, ((DetachSMPC) pc.getRelSet().iterator().next())
            .getIntField());
        assertEquals(2, pc.getStringIntMap().size());
        assertEquals(new Integer(100), pc.getStringIntMap().get("b"));

        startTx(pm);
        deleteAll(DetachSMPC.class, pm);
        endTx(pm);
        endEm(pm);
        factory.close();
    }

    private void enhance() throws Exception {
        Properties props = getProperties(new String[]{
            "openjpa.DetachState", "DetachedStateField=true",
        });
        OpenJPAConfiguration conf = new OpenJPAConfigurationImpl(true, false);
        conf.fromProperties(props);

        Options opts = new Options();
        opts.put("jdo", "true");
        PCEnhancer.run(conf, new String[]{
            "org.apache.openjpa.persistence.kernel.noenhance.DetachSMPC" },
            opts);
    }

    private void assertDetachedSM(Object obj) {
        OpenJPAEntityManager pm = getPM();
        assertTrue(pm.isDetached(obj));
        PersistenceCapable pc = (PersistenceCapable) obj;
        assertEquals(DetachedStateManager.class,
            pc.pcGetStateManager().getClass());
        endEm(pm);
    }
}
