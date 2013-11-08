/*
 * TestExtents.java
 *
 * Created on October 10, 2006, 4:34 PM
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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;

import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestExtents extends BaseKernelTest {

    /**
     * Creates a new instance of TestExtents
     */
    public TestExtents() {
    }

    public TestExtents(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);

        RuntimeTest1 b = new RuntimeTest1("STRING", 10);
        RuntimeTest2 c = new RuntimeTest2("STRING2", 11);
        pm.persist(b);
        pm.persist(c);

        endTx(pm);
        endEm(pm);
    }

    public void testExtent1() {

        OpenJPAEntityManager pm = getPM();
        Extent ext = pm.createExtent(RuntimeTest1.class, false);

        assertEquals(pm, ext.getEntityManager());

        assertEquals(RuntimeTest1.class, ext.getElementClass());
        assertTrue(!ext.hasSubclasses());
    }

    public void testExtent2() {
        OpenJPAEntityManager pm = getPM();
        Extent ext = pm.createExtent(RuntimeTest1.class, false);

        boolean found = false;
        for (Iterator i = ext.iterator(); i.hasNext(); found = true)
            assertEquals(RuntimeTest1.class, i.next().getClass());
        assertTrue(found);
    }

    public void testExtent3() {
        OpenJPAEntityManager pm = getPM();
        Extent ext = pm.createExtent(RuntimeTest1.class, true);

        boolean foundB = false;
        for (Iterator i = ext.iterator(); i.hasNext();)
            if (i.next().getClass().equals(RuntimeTest2.class))
                foundB = true;
        assertTrue(foundB);
    }

    public void testExtent4() {
        OpenJPAEntityManager pm = getPM();
        Extent ext = pm.createExtent(RuntimeTest1.class, true);

        List all = new LinkedList();
        for (Iterator i = ext.iterator(); i.hasNext();)
            all.add(i.next());

        List aList = ext.list();
        assertEquals(all.size(), aList.size());
        assertContainsSame(all, aList);
    }

    private void assertContainsSame(List l, Collection c) {
        int size = 0;
        for (Iterator iter = c.iterator(); iter.hasNext(); iter.next())
            size++;

        assertEquals(l.size(), size);

        for (Iterator iter = l.iterator(); iter.hasNext();)
            assertTrue(c.contains(iter.next()));
    }
}
