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

import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.openjpa.persistence.kernel.common.apps.DependentFieldsPC;
import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;

public class TestDependentFields2 extends BaseKernelTest {

    private static final int COMMIT = 0;
    private static final int ROLLBACK = 1;
    private static final int PRESTORE = 2;

    private Object _root = null;
    private Object _rel = null;
    private Object _depRel = null;
    private Object _deep = null;
    private Object _coll = null;
    private Object _depColl = null;
    private Object _map = null;
    private Object _depMap = null;
    private Object _repeat = null;

    public TestDependentFields2(String casename) {
        super(casename);
    }

    public void setUp() throws Exception {
        deleteAll(DependentFieldsPC.class);

        DependentFieldsPC root = new DependentFieldsPC();
        root.setRelation(new DependentFieldsPC());
        root.getList().add(new DependentFieldsPC());
        root.getMap().put("key", new DependentFieldsPC());
        root.setDependentRelation(new DependentFieldsPC());
        root.getDependentRelation().setDependentRelation
            (new DependentFieldsPC());
        root.getDependentList().add(new DependentFieldsPC());
        root.getDependentMap().put("key", new DependentFieldsPC());

        DependentFieldsPC repeat = new DependentFieldsPC();
        root.getDependentList().add(repeat);
        root.getDependentMap().put("repeat", repeat);

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(root);
        endTx(pm);

        _root = pm.getObjectId(root);
        assertNotNull(_root);
        _rel = pm.getObjectId(root.getRelation());
        _depRel = pm.getObjectId(root.getDependentRelation());
        _deep = pm.getObjectId(root.getDependentRelation().
            getDependentRelation());
        _coll = pm.getObjectId(root.getList().iterator().next());
        Iterator itr = root.getDependentList().iterator();
        _depColl = pm.getObjectId(itr.next());
        _repeat = pm.getObjectId(itr.next());
        _map = pm.getObjectId(root.getMap().get("key"));
        _depMap = pm.getObjectId(root.getDependentMap().get("key"));

        endEm(pm);
    }

    public void testDependentFieldsLoaded() {
        delete(true, COMMIT);
        checkFields();
    }

    public void testDependentFieldsLoadedWithRollback() {
        delete(true, ROLLBACK);
        checkFields();
    }

    public void testDependentFieldsLoadedWithPreStore() {
        delete(true, PRESTORE);
        checkFields();
    }

    public void testDependentFieldsUnloaded() {
        delete(false, COMMIT);
        checkFields();
    }

    public void testDependentFieldsUnloadedWithRollback() {
        delete(false, ROLLBACK);
        checkFields();
    }

    public void testDependentFieldsUnloadedWithPreStore() {
        delete(false, PRESTORE);
        checkFields();
    }

    private void delete(boolean load, int action) {
        OpenJPAEntityManager pm = getPM(true, true);
        DependentFieldsPC root;
        Object rel = null;
        Object depRel = null;
        Object coll = null;
        Object depColl = null;
        Object map = null;
        Object depMap = null;
        Object repeat = null;
        Object deep = null;
        while (true) {
            startTx(pm);
            root = (DependentFieldsPC) pm.find(DependentFieldsPC.class, _root);
            if (load) {
                rel = root.getRelation();
                assertNotNull(rel);
                depRel = root.getDependentRelation();
                assertNotNull(depRel);
                deep = ((DependentFieldsPC) depRel).getDependentRelation();
                assertNotNull(deep);
                coll = root.getList().iterator().next();
                assertNotNull(coll);
                Iterator itr = root.getDependentList().iterator();
                depColl = itr.next();
                repeat = itr.next();
                assertNotNull(depColl);
                assertNotNull(repeat);
                map = root.getMap().get("key");
                assertNotNull(map);
                depMap = root.getDependentMap().get("key");
                assertNotNull(depMap);

                // pcl: test both depColl and repeat, since they might
                // have been out of order above.
                Object o = root.getDependentMap().get("repeat");
                if (o != repeat)
                    fail("dependent map does not contain 'repeat'");
            }
            pm.remove(root);

            if (action == ROLLBACK) {
                rollbackTx(pm);
                action = COMMIT;
            } else if (action == COMMIT) {
                endTx(pm);
                break;
            } else {
                pm.preFlush();
                break;
            }
        }

        if (load) {
            if (action == PRESTORE) {
                assertFalse(pm.isRemoved(rel));
                assertFalse(pm.isRemoved(coll));
                assertFalse(pm.isRemoved(map));
                assertTrue(pm.isRemoved(depRel));
                assertTrue(pm.isRemoved(deep));
                assertTrue(pm.isRemoved(depColl));
                assertTrue(pm.isRemoved(depMap));
                assertTrue(pm.isRemoved(repeat));
            } else {
                assertNotNull(OpenJPAPersistence.getEntityManager(rel));
                assertNotNull(OpenJPAPersistence.getEntityManager(coll));
                assertNotNull(OpenJPAPersistence.getEntityManager(map));
                assertNull(OpenJPAPersistence.getEntityManager(depRel));
                assertNull(OpenJPAPersistence.getEntityManager(deep));
                assertNull(OpenJPAPersistence.getEntityManager(depColl));
                assertNull(OpenJPAPersistence.getEntityManager(depMap));
                assertNull(OpenJPAPersistence.getEntityManager(repeat));
            }
        }

        if (action == PRESTORE)
            endTx(pm);
        endEm(pm);
    }

    private void checkFields() {
        OpenJPAEntityManager pm = getPM(true, true);
        assertNotNull(pm.find(DependentFieldsPC.class, _rel));
        assertNotNull(pm.find(DependentFieldsPC.class, _coll));
        assertNotNull(pm.find(DependentFieldsPC.class, _map));
        assertNull(pm.find(DependentFieldsPC.class, _depRel));
        assertNull(pm.find(DependentFieldsPC.class, _deep));
        assertNull(pm.find(DependentFieldsPC.class, _depColl));
        assertNull(pm.find(DependentFieldsPC.class, _depMap));

        endEm(pm);
    }

    public void testNullDeletesDependent() {
        nullDeletesDependent(COMMIT);
    }

    public void testNullDeletesDependentWithRollback() {
        nullDeletesDependent(ROLLBACK);
    }

    public void testNullDeletesDependentWithPreStore() {
        nullDeletesDependent(PRESTORE);
    }

    private void nullDeletesDependent(int action) {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        DependentFieldsPC pc;
        DependentFieldsPC depRel;
        while (true) {
            startTx(pm);
            pc = (DependentFieldsPC) pm.find(DependentFieldsPC.class, _root);
            depRel = pc.getDependentRelation();
            assertEquals(_depRel, pm.getObjectId(depRel));
            pc.setDependentRelation(null);
            if (action == ROLLBACK) {
                rollbackTx(pm);
                action = COMMIT;
            } else if (action == COMMIT) {
                endTx(pm);
                break;
            } else {
                pm.preFlush();
                break;
            }
        }

        if (action == PRESTORE) {
            assertTrue(pm.isRemoved(depRel));
            endTx(pm);
        }

        assertTrue(!pm.isPersistent(depRel));
        assertNull(pm.find(DependentFieldsPC.class, _depRel));
        endEm(pm);
    }

    public void testRemoveDeletesDependent() {
        removeDeletesDependent(COMMIT);
    }

    public void testRemoveDeletesDependentWithRollback() {
        removeDeletesDependent(ROLLBACK);
    }

    public void testRemoveDeletesDependentWithPreStore() {
        removeDeletesDependent(PRESTORE);
    }

    private void removeDeletesDependent(int action) {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        DependentFieldsPC pc;
        DependentFieldsPC depColl;
        DependentFieldsPC depMap;
        List list;
        Map map;
        while (true) {
            startTx(pm);
            pc = (DependentFieldsPC) pm.find(DependentFieldsPC.class, _root);
            list = pc.getDependentList();
            assertEquals("list size =! 2", 2, list.size());
            depColl = (DependentFieldsPC) list.remove(0);
            assertEquals("_depColl is not pm.getObjectId(depColl)", _depColl,
                pm.getObjectId(depColl));

            map = pc.getDependentMap();
            assertEquals("map size =! 2", 2, map.size());
            depMap = (DependentFieldsPC) map.remove("key");
            assertEquals("_depMap is not pm.getObjectId(depMap)", _depMap,
                pm.getObjectId(depMap));

            if (action == ROLLBACK) {
                rollbackTx(pm);
                action = COMMIT;
            } else if (action == COMMIT) {
                endTx(pm);
                break;
            } else {
                pm.preFlush();
                break;
            }
        }

        if (action == PRESTORE) {
            assertTrue(pm.isRemoved(depColl));
            assertTrue(pm.isRemoved(depMap));
            endTx(pm);
        }

//        assertTrue("depcoll is persistence", !pm.isPersistent(depColl));
        assertNull(pm.find(DependentFieldsPC.class, _depColl));

//        assertTrue("depMap is persistence", !pm.isPersistent(depMap));
        assertNull(pm.find(DependentFieldsPC.class, _depMap));

        assertNotNull("repeat is null",
            pm.find(DependentFieldsPC.class, _repeat));
        endEm(pm);
    }

    public void testMoveDependentInContainer() {
        moveDependentInContainer(COMMIT);
    }

    public void testMoveDependentInContainerWithRollback() {
        moveDependentInContainer(ROLLBACK);
    }

    public void testMoveDependentInContainerWithPreStore() {
        moveDependentInContainer(PRESTORE);
    }

    private void moveDependentInContainer(int action) {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        DependentFieldsPC pc;
        DependentFieldsPC depColl;
        DependentFieldsPC depMap;
        List list;
        Map map;
        while (true) {
            startTx(pm);
            pc = (DependentFieldsPC) pm.find(DependentFieldsPC.class, _root);
            list = pc.getDependentList();
            assertEquals(2, list.size());
            depColl = (DependentFieldsPC) list.get(0);
            assertEquals(_depColl, pm.getObjectId(depColl));
            list.remove(0);
            list.add(depColl);

            map = pc.getDependentMap();
            assertEquals(2, map.size());
            depMap = (DependentFieldsPC) map.get("key");
            assertEquals(_depMap, pm.getObjectId(depMap));
            map.remove("key");
            map.put("newkey", depMap);

            if (action == ROLLBACK) {
                rollbackTx(pm);
                action = COMMIT;
            } else if (action == COMMIT) {
                endTx(pm);
                break;
            } else {
                pm.preFlush();
                break;
            }
        }

        if (action == PRESTORE) {
            assertFalse(pm.isRemoved(depColl));
            assertFalse(pm.isRemoved(depMap));
            endTx(pm);
        }

        assertTrue(pm.isPersistent(depColl));
        assertNotNull(pm.find(DependentFieldsPC.class, _depColl));
        assertTrue(pm.isPersistent(depMap));
        assertNotNull(pm.find(DependentFieldsPC.class, _depMap));
        assertNotNull(pm.find(DependentFieldsPC.class, _repeat));
        endEm(pm);
    }

    public void testRefedDependentNotDeleted() {
        refedDependentNotDeleted(COMMIT);
    }

    public void testRefedDependentNotDeletedWithRollback() {
        refedDependentNotDeleted(ROLLBACK);
    }

    public void testRefedDependentNotDeletedWithPreStore() {
        refedDependentNotDeleted(PRESTORE);
    }

    private void refedDependentNotDeleted(int action) {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        DependentFieldsPC pc;
        DependentFieldsPC newPC = null;
        DependentFieldsPC depRel;
        while (true) {
            startTx(pm);
            pc = (DependentFieldsPC) pm.find(DependentFieldsPC.class, _root);
            if (newPC == null)
                newPC = new DependentFieldsPC();
            depRel = pc.getDependentRelation();
            newPC.setDependentRelation(depRel);
            pc.setDependentRelation(null);
            pm.persist(newPC);

            if (action == ROLLBACK) {
                rollbackTx(pm);
                action = COMMIT;
            } else if (action == COMMIT) {
                endTx(pm);
                break;
            } else {
                pm.preFlush();
                break;
            }
        }

        if (action == PRESTORE) {
            assertFalse(pm.isRemoved(depRel));
            endTx(pm);
        }

        assertTrue(pm.isPersistent(depRel));
        assertNotNull(pm.find(DependentFieldsPC.class, _depRel));
        endEm(pm);
    }

    public void testNullSharedDependent() {
        nullSharedDependent(COMMIT);
    }

    public void testNullSharedDependentWithRollback() {
        nullSharedDependent(ROLLBACK);
    }

    public void testNullSharedDependentWithPreStore() {
        nullSharedDependent(PRESTORE);
    }

    private void nullSharedDependent(int action) {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        DependentFieldsPC pc;
        DependentFieldsPC repeat;
        List list;
        Map map;
        while (true) {
            startTx(pm);
            pc = (DependentFieldsPC) pm.find(DependentFieldsPC.class, _root);
            list = pc.getDependentList();
            assertEquals(2, list.size());
            repeat = (DependentFieldsPC) list.get(1);
            assertEquals(_repeat, pm.getObjectId(repeat));
            list.remove(1);

            map = pc.getDependentMap();
            assertEquals(2, map.size());
            assertEquals(repeat, (DependentFieldsPC) map.remove("repeat"));

            if (action == PRESTORE)
                pm.preFlush();
            else
                pm.flush();
            assertTrue(pm.isRemoved(repeat));

            // now after deleting on flush, assigning to another field and
            // attempting to commit should throw an error -- can't undelete an
            // object
            pc.getList().add(repeat);

            if (action == ROLLBACK) {
                rollbackTx(pm);
                action = COMMIT;
            } else {
                try {
                    pm.getTransaction().commit();
                    fail("Committed with ref to deleted dependent object");
                } catch (Exception je) {
                    rollbackTx(pm);
                } finally {
                }
                break;
            }
        }

        endEm(pm);
    }

    public void testClearMappedDependentOfDetached() {
        clearDependentOfDetachedTest(true);
    }

    public void testClearInverseKeyDependentOfDetached() {
        clearDependentOfDetachedTest(false);
    }

    private void clearDependentOfDetachedTest(boolean mapped) {
        deleteAll(DependentFieldsPC.class);

        DependentFieldsPC owner = new DependentFieldsPC();
        for (int i = 0; i < 2; i++) {
            DependentFieldsPC child = new DependentFieldsPC();
            if (mapped) {
                owner.getDependentMappedList().add(child);
                child.setOwner(owner);
            } else
                owner.getDependentInverseKeyList().add(child);
        }

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(owner);
        endTx(pm);
        Object oid = pm.getObjectId(owner);
        assertEquals(3,
            ((Extent) pm.createExtent(DependentFieldsPC.class, true))
                .list().size());
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        owner = (DependentFieldsPC) pm.find(DependentFieldsPC.class, oid);
        if (mapped)
            assertEquals(2, owner.getDependentMappedList().size());
        else
            assertEquals(2, owner.getDependentInverseKeyList().size());
        DependentFieldsPC detached = (DependentFieldsPC) pm.detachCopy(owner);
        endEm(pm);

        if (mapped) {
            assertEquals(2, detached.getDependentMappedList().size());
            detached.getDependentMappedList().clear();
        } else {
            assertEquals(2, detached.getDependentInverseKeyList().size());
            detached.getDependentInverseKeyList().clear();
        }

        pm = (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        owner = (DependentFieldsPC) pm.merge(detached);
        if (mapped)
            assertEquals(0, owner.getDependentMappedList().size());
        else
            assertEquals(0, owner.getDependentInverseKeyList().size());
        endTx(pm);
        assertEquals(1,
            ((Extent) pm.createExtent(DependentFieldsPC.class, true)).
                list().size());
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        owner = (DependentFieldsPC) pm.find(DependentFieldsPC.class, oid);
        if (mapped)
            assertEquals(0, owner.getDependentMappedList().size());
        else
            assertEquals(0, owner.getDependentInverseKeyList().size());
        endEm(pm);
    }
}
