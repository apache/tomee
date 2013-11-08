/*
 * TestOpenJPAEntityManager.java
 *
 * Created on October 12, 2006, 4:40 PM
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest4;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.DelegatingBrokerFactory;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.util.Id;

public class TestPersistenceManager extends BaseKernelTest {

    private int _id = 0;
    private int _id2 = 0;

    public TestPersistenceManager(String name) {
        super(name);
    }

    /**
     * Creates a new instance of TestOpenJPAEntityManager
     */
    public TestPersistenceManager() {
    }

    public void setUp() throws Exception {
        deleteAll(RuntimeTest1.class);
        deleteAll(RuntimeTest2.class);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);

        RuntimeTest1 a = new RuntimeTest1("STRING", 10);
        RuntimeTest2 b = new RuntimeTest2("STRING2", 11);
        pm.persist(a);
        pm.persist(b);
        _id = a.getIntField();
        _id2 = b.getIntField();

        endTx(pm);
        endEm(pm);
    }

    /**
     * No-op test that can be run in name to see if the setup is working
     * properly.
     */
    public void testSetup() {
    }

    /**
     * Tests that the PM throws Exceptions on usage attempts after it has been
     * closed.
     */
    public void testClosed() {
        OpenJPAEntityManager pm = getPM();
        endEm(pm);

        try {
            // this is the only method that should succeed
            pm.isOpen();
        } catch (RuntimeException re) {
            fail("isClosed");
        }
        try {
            pm.find(RuntimeTest1.class, _id);
            fail("find");
        } catch (RuntimeException re) {
        }
        try {
            pm.persist(new RuntimeTest1(20));
            fail("setUserObject");
        } catch (RuntimeException re) {
        }
        try {
            pm.setNontransactionalRead(true);
            fail("setNontransactionalRead");
        } catch (RuntimeException re) {
        }
        try {
            // this method should fail
            endEm(pm);
            bug(65, null, "multiple close should not be allowed");
            fail("multiple close should not be allowed");
        } catch (Exception jdoe) {
            // good: we should get an exception
        }
    }

    public void testMultipleCloseThreaded()
        throws Throwable {
        final OpenJPAEntityManager pm = getPM();
        final List result = new ArrayList();
        startTx(pm);

        endEm(pm);

        new Thread() {
            public void run() {
                try {
                    endEm(pm);
                    result.add(new Integer(0));
                } catch (Exception jdoe) {
                    result.add(jdoe);
                } catch (Throwable t) {
                    result.add(t);
                }
            }
        }.start();

        while (result.size() == 0)
            Thread.currentThread().yield(); // wait for results
        Object ret = result.get(0);

        if (ret instanceof Exception)
            return; // good

        if (ret instanceof Throwable)
            throw (Throwable) ret;

        bug(65, null,
            "multiple close in different threads should not be allowed");
    }

    /**
     * This method tries to perform operations that should lead to
     * illegal states, such as persisting instances outside of transactions,
     * etc.
     */
    public void testIllegalState() {
        OpenJPAEntityManager pm = getPM();

        RuntimeTest1 a = new RuntimeTest1("foo", 14);
        RuntimeTest1 a2 = (RuntimeTest1) pm.find(RuntimeTest1.class, _id);

        try {
            pm.persist(a);
            fail("persist");
        }
        catch (Exception ise) {
        }
        try {
            pm.isTransactional(a2);
            fail("makeTransactional");
        }
        catch (Exception ise) {
        }
        try {
            pm.remove(a2);
            fail("deletePersistent");
        }
        catch (Exception ise) {
        }

        endEm(pm);
    }

    public void testOpenJPAEntityManagerFactorySerializable()
        throws Exception {
        OpenJPAEntityManagerFactory pmf =
            (OpenJPAEntityManagerFactory) getEmf();
        assertNotNull("OpenJPAEntityManagerFactory is null.", pmf);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(pmf);

        OpenJPAEntityManagerFactory pmf2 = (OpenJPAEntityManagerFactory)
            new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray())).readObject();
        assertNotNull("Deserialized OpenJPAEntityManagerFactory is null.",
            pmf2);

        OpenJPAEntityManager pm = pmf2.createEntityManager();
        startTx(pm);
        // go through some objects to make sure our PM is OK
        for (Iterator i = pm.createExtent(RuntimeTest1.class, true).iterator();
            i.hasNext(); i.next())
            ;
        endTx(pm);
        endEm(pm);
    }

    private static BrokerFactory toBrokerFactory(EntityManagerFactory emf) {
        BrokerFactory bf = JPAFacadeHelper.toBrokerFactory(emf);
        if (bf instanceof DelegatingBrokerFactory)
            bf = ((DelegatingBrokerFactory) bf).getInnermostDelegate();
        return bf;
    }

    /**
     * Test that a getObjectById() returns the correct instance.
     */
    public void testGetObjectById() {
        // test with valid id
        OpenJPAEntityManager pm = getPM();
        RuntimeTest1 b = (RuntimeTest1) pm.find(RuntimeTest1.class, _id);
        assertEquals("STRING", b.getStringField());

        // invalid with possible subclasses should throw immediate exception
        Object invalidId = new Id(RuntimeTest1.class, -1L);
        try {
            pm.find(RuntimeTest1.class, invalidId);
            fail("Invalid Object");
        } catch (Exception e) {
        }

        // invalid without subclasses and without validating should return
        // hollow
        invalidId = new Id(RuntimeTest4.class, -1L);
        try {
            RuntimeTest4 a = (RuntimeTest4) pm.getReference(RuntimeTest4.class,
                invalidId);
            assertNotNull("invalid without subclasses and without validating "
                + "should return hollow or throw exception", a);
            a.getName();
            fail("Allowed access of invalid hollow instance.");
        }
        catch (EntityNotFoundException enfe) {
            // expected
        }

        invalidId = new Id(RuntimeTest4.class, -3L);
        assertNull(pm.find(RuntimeTest4.class, invalidId));

        endEm(pm);
    }

    public void testGetObjectsById() {
        OpenJPAEntityManager pm = getPM();
        ArrayList idlist = new ArrayList();
        idlist.add(_id);
        idlist.add(_id2);
        Collection pcs = pm.findAll(RuntimeTest1.class, idlist);
        assertEquals(2, pcs.size());
        Iterator iter = pcs.iterator();
        assertEquals("STRING", ((RuntimeTest1) iter.next()).getStringField());
        assertEquals("STRING2", ((RuntimeTest2) iter.next()).getStringField());
        endEm(pm);

        pm = getPM();
        idlist = new ArrayList();
        idlist.add(_id);
        idlist.add(_id);
        pcs = pm.findAll(RuntimeTest1.class, idlist);
        iter = pcs.iterator();
        assertEquals(2, pcs.size());
        assertEquals("STRING", ((RuntimeTest1) iter.next()).getStringField());
        iter = pcs.iterator();
        assertTrue(iter.next() == iter.next());
        endEm(pm);

        // invalid id causes exception
        Object invalidId = new Id(RuntimeTest4.class, -1L);
        pm = getPM();
        idlist = new ArrayList();
        idlist.add(_id);
        idlist.add(invalidId);
        try {
            pcs = (ArrayList) pm.findAll(RuntimeTest1.class, idlist);
            iter = pcs.iterator();
            assertEquals(2, pcs.size());
            assertEquals("STRING",
                ((RuntimeTest1) iter.next()).getStringField());
            assertNotNull(iter.next());
            fail("invalid id didnt cause exception");
        }
        catch (Exception onfe) {
            //expected exception. invalid id causes exception
        }

        try {
            ((RuntimeTest4) iter.next()).getName();
            fail("Accessed invalid object.");
        }
        catch (Exception onfe) {
            bug(1138, onfe, "Wrong exception type");
        }

        pm = getPM();
        try {
            pm.findAll(RuntimeTest1.class, idlist);
            fail("Found invalid object.");
        } catch (Exception e) {
        }
        endEm(pm);
    }

    public void testEvictAll() {
        OpenJPAEntityManager pm = getPM();

        List l = ((Extent) pm.createExtent(RuntimeTest1.class, true)).list();
        pm.retrieveAll(l);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.PNONTRANS, s);
        }
        pm.evictAll();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.HOLLOW, s);
        }
    }

    public void testEvictAllCollection() {
        OpenJPAEntityManager pm = getPM();

        List l = ((Extent) pm.createExtent(RuntimeTest1.class, true)).list();
        pm.retrieveAll(l);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.PNONTRANS, s);
        }
        pm.evictAll(l);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.HOLLOW, s);
        }
    }

    public void testEvictAllClass() {
        OpenJPAEntityManager pm = getPM();

        List l = ((Extent) pm.createExtent(RuntimeTest1.class, true)).list();
        pm.retrieveAll(l);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.PNONTRANS, s);
        }
        pm.evictAll(RuntimeTest1.class);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.HOLLOW, s);
        }
    }

    public void testEvictAllClassFailure() {
        OpenJPAEntityManager pm = getPM();

        List l = ((Extent) pm.createExtent(RuntimeTest1.class, true)).list();
        pm.retrieveAll(l);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.PNONTRANS, s);
        }
        pm.evictAll(RuntimeTest2.class);
        boolean foundPNONTRANS = false;
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            if (s == PCState.PNONTRANS) {
                foundPNONTRANS = true;
                break;
            }
        }
        assertTrue("should have found some RuntimeTest1s that were not "
            + "evicted", foundPNONTRANS);
    }

    public void testEvictAllExtent() {
        OpenJPAEntityManager pm = getPM();

        List l = ((Extent) pm.createExtent(RuntimeTest1.class, true)).list();
        pm.retrieveAll(l);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.PNONTRANS, s);
        }
        pm.evictAll(pm.createExtent(RuntimeTest1.class, true));
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.HOLLOW, s);
        }

        pm.retrieveAll(l);
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PCState s = getStateManager(iter.next(), pm).getPCState();
            assertEquals(PCState.PNONTRANS, s);
        }
        pm.evictAll(pm.createExtent(RuntimeTest1.class, false));
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o.getClass() == RuntimeTest1.class) {
                PCState s = getStateManager(o, pm).getPCState();
                assertEquals(PCState.HOLLOW, s);
            }
        }
    }
}
