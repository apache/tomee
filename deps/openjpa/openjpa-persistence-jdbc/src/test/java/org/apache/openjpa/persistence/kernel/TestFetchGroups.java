/*
 * TestFetchGroups.java
 *
 * Created on October 10, 2006, 5:46 PM
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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.AttachA;
import org.apache.openjpa.persistence.kernel.common.apps.AttachB;
import org.apache.openjpa.persistence.kernel.common.apps.AttachC;
import org.apache.openjpa.persistence.kernel.common.apps.AttachD;
import org.apache.openjpa.persistence.kernel.common.apps.AttachE;
import org.apache.openjpa.persistence.kernel.common.apps.AttachF;
import org.apache.openjpa.persistence.kernel.common.apps.FetchGroupTestObject;
import org.apache.openjpa.persistence.kernel.common.apps.
        FetchGroupTestObjectChild;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;

public class TestFetchGroups extends BaseKernelTest {

    private int oid1;
    private int oid2;
    private int oidc1;

    public TestFetchGroups(String s) {
        super(s);
    }

    /**
     * Creates a new instance of TestFetchGroups
     */
    public TestFetchGroups() {
    }

    public void setUp() throws Exception {
        super.setUp(FetchGroupTestObject.class, FetchGroupTestObjectChild.class,
            AttachA.class, AttachB.class, AttachC.class, AttachD.class, AttachE.class, AttachF.class);

        FetchGroupTestObject o1 = new FetchGroupTestObject();
        // the value that 'a' is set to is important -- TestFetchGroupsExtent
        // and TestFetchGroupsQuery rely on this
        o1.setA(5);
        o1.setB("foo");
        //o1.setC (new BigInteger (89));
        o1.setD(new Date());
        o1.setE("e-foo");
        o1.setF("f-foo");

        FetchGroupTestObject o2 = new FetchGroupTestObject();
        // the value that 'a' is set to is important -- TestFetchGroupsExtent
        // and TestFetchGroupsQuery rely on this
        o2.setA(3);
        o2.setB("bar");
        //o2.setC (new BigInteger (13));
        o2.setD(new Date());
        o2.setE("e-bar");
        o2.setF("f-bar");
        o2.setG(o1);
        o2.setH(o1);

        FetchGroupTestObjectChild c1 = new FetchGroupTestObjectChild();
        // the value that 'a' is set to is important -- TestFetchGroupsExtent
        // and TestFetchGroupsQuery rely on this
        c1.setA(4);
        c1.setB("child");
        c1.setD(new Date());
        c1.setE("e-baz");
        c1.setF("f-baz");
        c1.setG(o1);
        c1.setH(o1);
        c1.setChildA(1);
        c1.setChildB(2);
        c1.setChildC(3);
        c1.setChildD(4);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);

        pm.persist(o1);
        pm.persist(o2);
        pm.persist(c1);
        endTx(pm);

        oid1 = o1.getId();
        oid2 = o2.getId();
        oidc1 = c1.getId();

        endEm(pm);
    }

    public void testFetchGroupsFromConfiguration() {
        Map props = new HashMap();
        props.put("openjpa.FetchGroups", "default,fg1,fg2");
        OpenJPAEntityManagerFactory factory = getEmf(props);

        OpenJPAEntityManager pm = factory.createEntityManager();
        checkGroups(pm, new String[]{ "fg1", "fg2" });
        factory.close();
    }

    public void testFetchGroupsNoConfiguration() {
        OpenJPAEntityManager pm = getPM();

        FetchGroupTestObject o1 = getO1(pm);
        FetchGroupTestObject o2 = getO2(pm);

        // only field a should be loaded.
        checkObject(pm, o1, true, false, false, false, false, false);

        // upon loading field b, fields c and d should also be loaded,
        // but e and f should not.
        o1.getB();
        checkObject(pm, o1, true, true, true, true, false, false);

        // loading field h should not cause any of the others to be loaded.
        assertEquals(o1, o2.getH());
        checkObject(pm, o2, true, false, false, false, false, false);

        // loading field g should cause e and f to be loaded.
        assertEquals(o1, o2.getG());
        checkObject(pm, o2, true, false, false, false, true, true);
    }

    public void testRetrieveAll() {
        OpenJPAEntityManager pm = getPM();

        FetchGroupTestObject o1 = getO1(pm);
        FetchGroupTestObject o2 = getO2(pm);

        // only field a should be loaded.
        checkObject(pm, o1, true, false, false, false, false, false);
        checkObject(pm, o2, true, false, false, false, false, false);

        // only field a should be loaded.
        pm.retrieve(o1);
        checkObject(pm, o1, true, false, false, false, false, false);

        // Add groups 1 and 3 to the default fetch configuration.
        pm.getFetchPlan().addFetchGroup("g1");
        pm.getFetchPlan().addFetchGroup("g3");

        // Retrieve o1's "DFG" which will actually do all configured
        // fetch groups.
        // DFG fields and fields in groups 1 and 3 should be loaded
        pm.retrieve(o1);
        checkObject(pm, o1, true, true, true, true, false, false);
    }

    public void testFetchGroupConfiguration() {
        OpenJPAEntityManager pm = getPM();
        FetchPlan fetch = pm.getFetchPlan();

        checkGroups(pm, new String[0]);

        fetch.addFetchGroup("foo");
        checkGroups(pm, new String[]{ "foo" });

        fetch.addFetchGroup("bar");
        fetch.addFetchGroup("baz");
        checkGroups(pm, new String[]{ "foo", "bar", "baz" });

        fetch.addFetchGroup("a");
        fetch.addFetchGroup("b");
        fetch.addFetchGroup("c");
        fetch.addFetchGroup("d");
        checkGroups(pm, new String[]
            { "foo", "bar", "baz", "a", "b", "c", "d" });

        fetch.removeFetchGroup("bar");
        checkGroups(pm, new String[]{ "foo", "baz", "a", "b", "c", "d" });

        fetch.removeFetchGroup("baz");
        fetch.removeFetchGroup("c");
        checkGroups(pm, new String[]{ "foo", "a", "b", "d" });

        fetch.clearFetchGroups().addFetchGroup(FetchPlan.GROUP_DEFAULT);
        checkGroups(pm, new String[0]);
    }

    private void checkGroups(OpenJPAEntityManager pm, String[] groups) {
        HashSet groupSet = new HashSet(Arrays.asList(groups));
        groupSet.add(FetchPlan.GROUP_DEFAULT);
        assertEquals("groupSet dont match", groupSet,
            new HashSet(pm.getFetchPlan().getFetchGroups()));
    }

    public void testFetchGroupsChildWithConfiguration() {
        OpenJPAEntityManager pm = getPM();
        pm.getFetchPlan().addFetchGroup("g1");
        pm.getFetchPlan().addFetchGroup("g3");

        // get this so that h's value is loaded into cache.
        FetchGroupTestObject o1 = getO1(pm);

        FetchGroupTestObjectChild c1 = getC1(pm);

        // DFG fields and fields in groups 1 and 3 should be loaded
        checkChildObject(pm, c1, true, true, true, true, false, false,
            true, true, false, true);

        // upon accessing field b, nothing should change.
        c1.getB();
        checkChildObject(pm, c1, true, true, true, true, false, false,
            true, true, false, true);

        c1.getH();
    }

    public void testFetchGroupsWithConfiguration() {
        OpenJPAEntityManager pm = getPM();

        pm.getFetchPlan().addFetchGroup("g1");
        pm.getFetchPlan().addFetchGroup("g3");

        FetchGroupTestObject o1 = getO1(pm);
        FetchGroupTestObject o2 = getO2(pm);

        // DFG fields and fields in groups 1 and 3 should be loaded
        checkObject(pm, o1, true, true, true, true, false, false);

        // upon accessing field b, nothing should change.
        o1.getB();
        checkObject(pm, o1, true, true, true, true, false, false);

        // loading field h should not cause any of the others to be loaded.
        assertEquals(o1, o2.getH());
        checkObject(pm, o2, true, true, true, true, false, false);

        // loading field g should cause e and f to be loaded.
        assertEquals(o1, o2.getG());
        checkObject(pm, o2, true, true, true, true, true, true);
    }

    /**
     * Tests that relation fields are loaded immediately when
     * they are in one of the PM's configured fetch groups.
     */
    public void testRelationsLoaded() {
        OpenJPAEntityManager pm = getPM();
        pm.getFetchPlan().addFetchGroup("g2");

        // note: important the o1 is *not* in pm's cache at this point, so that
        // we know it takes another datastore trip to get o1

        // load o2 and retrieve its state manager
        OpenJPAStateManager sm = getStateManager(getO2(pm), pm);
        assertNotNull("SM is NULL", sm);

        // 'g' is the name of a 1-1 relation field to o1 in configured
        // fetch group 'g2'; make sure it is loaded
        int field = sm.getMetaData().getField("g").getIndex();
        try {
            assertTrue(sm.getLoaded().get(field));
            assertEquals(oid1,
                ((FetchGroupTestObject) sm.fetchObjectField(field)).getId());
        } catch (junit.framework.AssertionFailedError afe) {
            bug(623, afe, "One to one mappings do not work with custom "
                + "fetch groups");
        }
    }

    protected void checkObject(OpenJPAEntityManager pm,
        FetchGroupTestObject o, boolean a, boolean b,
        boolean c, boolean d, boolean e, boolean f) {
        OpenJPAStateManager sm = getStateManager(o, pm);
        BitSet loaded = sm.getLoaded();

        FieldMetaData[] fmds = sm.getMetaData().getFields();
        int i = 0;
        for (; i < fmds.length; i++) {
            if (fmds[i].getName().equals("a"))
                assertEquals(a, loaded.get(i));
            else if (fmds[i].getName().equals("b"))
                assertEquals(b, loaded.get(i));
            else if (fmds[i].getName().equals("c"))
                assertEquals(c, loaded.get(i));
            else if (fmds[i].getName().equals("d"))
                assertEquals(d, loaded.get(i));
            else if (fmds[i].getName().equals("e"))
                assertEquals(e, loaded.get(i));
            else if (fmds[i].getName().equals("f"))
                assertEquals(f, loaded.get(i));
        }
    }

    protected void checkChildObject(OpenJPAEntityManager pm,
        FetchGroupTestObjectChild o, boolean a, boolean b,
        boolean c, boolean d, boolean e, boolean f, boolean childA,
        boolean childB, boolean childC, boolean childD) {
        checkObject(pm, o, a, b, c, d, e, f);

        OpenJPAStateManager sm = getStateManager(o, pm);
        BitSet loaded = sm.getLoaded();

        FieldMetaData[] fmds = sm.getMetaData().getFields();
        int i = 0;
        for (; i < fmds.length; i++) {
            if (fmds[i].getName().equals("childA"))
                assertEquals(childA, loaded.get(i));
            else if (fmds[i].getName().equals("childB"))
                assertEquals(childB, loaded.get(i));
            else if (fmds[i].getName().equals("childC"))
                assertEquals(childC, loaded.get(i));
            else if (fmds[i].getName().equals("childD"))
                assertEquals(childD, loaded.get(i));
        }
    }

    protected FetchGroupTestObject getO1(OpenJPAEntityManager pm) {
        return pm.find(FetchGroupTestObject.class, oid1);
    }

    protected FetchGroupTestObject getO2(OpenJPAEntityManager pm) {
        return pm.find(FetchGroupTestObject.class, oid2);
    }

    protected FetchGroupTestObjectChild getC1(OpenJPAEntityManager pm) {
        return pm.find(FetchGroupTestObjectChild.class, oidc1);
    }

    /**
     * Tests that named fetch groups actually bring in the
     * managed object.
     */
    public void testFetchGroupInstantiated() {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        AttachE e = new AttachE();
        AttachB b = new AttachB();
        e.setB(b);
        pm.persist(e);

        endTx(pm);
        endEm(pm);

        pm = getPM();
        startTx(pm);
        assertSize(0, pm.getManagedObjects());
        pm.createExtent(AttachE.class, true).iterator().next();
        // make sure relation is not loaded
        assertSize(1, pm.getManagedObjects());
        rollbackTx(pm);
        endEm(pm);

        pm = getPM();
        startTx(pm);
        // now make sure we load relations
        pm.getFetchPlan().addFetchGroup("all");
        assertSize(0, pm.getManagedObjects());
        pm.createExtent(AttachE.class, true).iterator().next();
        // make sure relation is loaded
        assertSize(2, pm.getManagedObjects());
        rollbackTx(pm);
        endEm(pm);
    }
}
