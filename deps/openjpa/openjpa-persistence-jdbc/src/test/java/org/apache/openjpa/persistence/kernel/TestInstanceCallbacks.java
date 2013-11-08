/*
 * TestInstanceCallbacks.java
 *
 * Created on October 12, 2006, 1:19 PM
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



import org.apache.openjpa.persistence.kernel.common.apps.InstanceCallbacksTest;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import junit.framework.AssertionFailedError;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestInstanceCallbacks extends BaseKernelTest {

    private static final int COMMIT = 0;
    private static final int FLUSH = 1;
    private static final int PRESTORE = 2;

    private OpenJPAEntityManager _pm = null;
    private InstanceCallbacksTest _callbacks = null;

    public TestInstanceCallbacks(String name) {
        super(name);
    }

    /**
     * Creates a new instance of TestInstanceCallbacks
     */
    public TestInstanceCallbacks() {
    }

    public void setUp() throws Exception {
        deleteAll(InstanceCallbacksTest.class);
        deleteAll(RuntimeTest1.class);
        _pm = getPM(true, true);
        startTx(_pm);
        _callbacks = new InstanceCallbacksTest();
        _callbacks.setStringField("foo");
        _pm.persist(_callbacks);
        Object id = _pm.getObjectId(_callbacks);
        endTx(_pm);
        endEm(_pm);

        // re-find with different PM
        _pm = getPM();
        _callbacks =
            (InstanceCallbacksTest) _pm.find(InstanceCallbacksTest.class, id);
    }

    public void tearDown() throws Exception {
        rollbackTx(_pm);
        endEm(_pm);
        super.tearDown();
    }

    public void testPostLoad() {
        _callbacks.getStringField();
        assertTrue(_callbacks.postLoadCalled);
    }

    public void testPreStore() {
        preStoreTest(COMMIT);
    }

    public void testPreStoreWithFlush() {
        preStoreTest(FLUSH);
    }

    public void testPreStoreWithPreStore() {
        preStoreTest(PRESTORE);
    }

    private void preStoreTest(int action) {
        assertNoCallbacksInvoked(_callbacks);
//        _pm.begin();
        startTx(_pm);

        _callbacks.setStringField("bar");
        Object oid = _pm.getObjectId(_callbacks);
        if (action == COMMIT) {
            _pm.flush();
            endTx(_pm);
        } else if (action == FLUSH)
            _pm.flush();
        else if (action == PRESTORE)
            _pm.preFlush();
        assertTrue("prestore wasnt called", _callbacks.preStoreCalled);
        if (action != COMMIT) {
//            _pm.commit();
            if (action != FLUSH)
                _pm.flush();
            endTx(_pm);
        }

        OpenJPAEntityManager pm = getPM();
        InstanceCallbacksTest callbacks = (InstanceCallbacksTest)
            pm.find(InstanceCallbacksTest.class, oid);
        assertNoCallbacksInvoked(callbacks);
        assertEquals("getonetoone strng is not jdoprestore", "jdoPreStore",
            callbacks.getOneOne().getStringField());
        endEm(pm);
    }

    public void testPreDelete() {
        assertNoCallbacksInvoked(_callbacks);
        startTx(_pm);
        _pm.remove(_callbacks);
        assertTrue(_callbacks.preDeleteCalled);
        endTx(_pm);
    }

    public void testPreDeleteRecursion() {
        assertNoCallbacksInvoked(_callbacks);
        startTx(_pm);
        _callbacks.preDeleteCycle = 0;
        _pm.remove(_callbacks);
        assertEquals(1, _callbacks.preDeleteCycle);
        endTx(_pm);
    }

    public void testSetRelatedReferenceInPreStore() {
        assertNull(_callbacks.getRelId());
        InstanceCallbacksTest callbacks2 = new InstanceCallbacksTest();
        callbacks2.setRelId(_pm.getObjectId(_callbacks));
        startTx(_pm);
        _pm.persist(callbacks2);
        _pm.flush();
        endTx(_pm);
        assertEquals(8888, _callbacks.getIntField());
        try {
            assertEquals(callbacks2, _callbacks.getRel());
        } catch (AssertionFailedError afe) {
            bug(1162, afe, "Setting a related object reference in "
                + "preStore fails");
        }
    }

    public void testFlushCausesFlush() {
        //### JDO2MIG : this is failing because we're consuming exceptions
        // throws from callbacks; need to decide what to do with them
        causeFlushTest(FLUSH);
    }

    public void testPreStoreCausesFlush() {
        //### JDO2MIG : this is failing because we're consuming exceptions
        // throws from callbacks; need to decide what to do with them
        causeFlushTest(PRESTORE);
    }

    private void causeFlushTest(int action) {
        startTx(_pm);
        _callbacks.setStringField("sss");
        _callbacks.flushInPreStore = true;
        try {
            if (action == FLUSH)
                _pm.flush();
            else
                _pm.preFlush();

            bug(1139, "Recursive flush allowed because exception swallowed");
        } catch (Exception je) {
        }
        rollbackTx(_pm);
    }

    private void assertNoCallbacksInvoked(InstanceCallbacksTest pc) {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();

        assertFalse("Expected preDelete to not have been called for object ID "
            + pm.getObjectId(pc), pc.preDeleteCalled);
        assertFalse("Expected preClear to not have been called for object ID "
            + pm.getObjectId(pc), pc.preClearCalled);
        assertFalse("Expected preStore to not have been called for object ID "
            + pm.getObjectId(pc), pc.preStoreCalled);
    }

    /* 
    // no JPA equivalent

    public void testDetachAttach()
    throws Exception {
        OpenJPAEntityManager pm = getPM();
        DetachAttachEvent pc = (DetachAttachEvent) pm.find
                (DetachAttachEvent.class,createDetachableId(4));
        DetachAttachEvent.EVENTS.clear();
        pc = (DetachAttachEvent) pm.detach(pc);
        assertDetachEvents(new String[]{ "PRED4", "POSTD4" });
        endEm(pm,());
        
        assertTrue(pm.isDetached(pc));
        
        pm = getPM();
        startTx(pm,());
        //FIXME jthomas
        
//        pm.addInstanceLifecycleListener(new CreateLifecycleListener() {
//            public void postCreate(InstanceLifecycleEvent ev) {
//                fail("No post create necessary");
//            }
//        }, null);
        pm.persist(pc);
        assertDetachEvents(new String[]{ "PREA4", "POSTA4" });
        endTx(pm,());
        endEm(pm,());
    }
    
    public void testDetachAttachRelations() {
        OpenJPAEntityManager pm = getPM();
        startTx(pm,());
        DetachAttachEvent pc = (DetachAttachEvent) pm.find
                (DetachAttachEvent.class,createDetachableId(2));
        pc.setOneOne((DetachAttachEvent) pm.find
                (DetachAttachEvent.class,createDetachableId(4)));
        endTx(pm,());
        DetachAttachEvent.EVENTS.clear();
        pc = (DetachAttachEvent) pm.detach(pc);
        endEm(pm,());
        assertDetachEvents(
                new String[]{ "PRED2", "PRED4", "POSTD2", "POSTD4" });
        
        pm = getPM();
        startTx(pm,());
        pm.persist(pc);
        assertDetachEvents(
                new String[]{ "PREA2", "PREA4", "POSTA2", "POSTA4" });
        rollbackTx(pm,());
        endEm(pm,());
    }
    
    private void assertDetachEvents(String[] expected) {
        Collection events = DetachAttachEvent.EVENTS;
        if (expected.length != events.size()) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < expected.length; i++)
                buf.append(expected[i]).append(",");
            buf.append("!=");
            for (Iterator it = events.iterator(); it.hasNext();)
                buf.append(it.next()).append(",");
            fail("mismatch event count:" + buf);
        }
        String event;
        for (int i = 0; i < expected.length; i++) {
            if (!events.remove(expected[i]))
                fail("Event not fired:" + expected[i]);
            if (events.contains(expected[i]))
                fail("Event fired twice:" + expected[i]);
        }
        if (!events.isEmpty())
            fail("Excess events fired:" + events);
        DetachAttachEvent.EVENTS.clear();
    }
    
    private Object createDetachableId(int field) {
        OpenJPAEntityManager pm = getPM();
        startTx(pm,());
        DetachAttachEvent pc = new DetachAttachEvent();
        pc.setIntField(field);
        pm.persist(pc);
        endTx(pm,());
        endEm(pm,());
//        return pm.getObjectId(pc);
        return pc.getId();
    }
    */
}
