/*
 * TestSavepointEmbedded.java
 *
 * Created on October 16, 2006, 10:29 AM
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

import java.util.Properties;



import org.apache.openjpa.persistence.kernel.common.apps.EmbeddedOwnerPC;
import org.apache.openjpa.persistence.kernel.common.apps.EmbeddedPC;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestSavepointEmbedded extends BaseKernelTest {

    private int id = 10000;

    /**
     * Creates a new instance of TestSavepointEmbedded
     */
    public TestSavepointEmbedded(String name) {
        super(name);
    }

    protected String getSavepointPlugin() {
        return "in-mem(PreFlush=false)";
    }

    protected boolean expectNewEmbeddedFailure() {
        return true;
    }

    public Properties getProperties(String[] props) {
        Properties properties = super.getProperties(props);
        properties.put("openjpa.SavepointManager", getSavepointPlugin());
        return properties;
    }

    public void setUp() {
        deleteAll(EmbeddedOwnerPC.class);
    }

    private EmbeddedOwnerPC newEmbeddedOwnerPC() {
        return new EmbeddedOwnerPC(id++, id++);
    }

    public void testClearNew()
        throws Exception {
        doClearNewTest(true, 0, 0);
        doClearNewTest(true, 2, 0);
        doClearNewTest(true, 0, 2);
        doClearNewTest(true, 2, 2);
        doClearNewTest(false, 0, 0);
        doClearNewTest(false, 2, 0);
        doClearNewTest(false, 0, 2);
        doClearNewTest(false, 2, 2);
    }

    public void doClearNewTest(boolean newPC, int before, int after)
        throws Exception {
        deleteAll(EmbeddedOwnerPC.class);
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        EmbeddedOwnerPC pc = newEmbeddedOwnerPC();
        pm.persist(pc);
        pc.setStringField("orig");
        Object oid = pm.getObjectId(pc);
        if (!newPC) {
            endTx(pm);
            endEm(pm);
            pm = getPM();
            startTx(pm);
            pc = (EmbeddedOwnerPC) pm.find(EmbeddedOwnerPC.class, oid);
        }
        for (int i = 0; i < before; i++) {
            pc.setStringField("b" + i);
            pm.setSavepoint("b" + i);
        }

        pc.setStringField("test");
        pm.setSavepoint("test");
        EmbeddedPC embed = new EmbeddedPC();
        embed.setIntField(99);
        pc.setEmbedded(embed);

        for (int i = 0; i < after; i++) {
            pc.setStringField("a" + i);
            pm.setSavepoint("a" + i);
        }

        pm.rollbackToSavepoint("test");
        if (newPC)
            assertNull(pc.getEmbedded());
        else
            assertEquals(0, pc.getEmbedded().getIntField());
        assertEquals("test", pc.getStringField());
        endTx(pm);
        endEm(pm);
    }

    public void testEmbeddedReassign() {
        doEmbeddedReassignTest(true, 0, 0);
        doEmbeddedReassignTest(true, 2, 0);
        doEmbeddedReassignTest(true, 0, 2);
        doEmbeddedReassignTest(true, 2, 2);
        doEmbeddedReassignTest(false, 0, 0);
        doEmbeddedReassignTest(false, 2, 0);
        doEmbeddedReassignTest(false, 0, 2);
        doEmbeddedReassignTest(false, 2, 2);
    }

    public void doEmbeddedReassignTest(boolean newPC, int before, int after) {
        deleteAll(EmbeddedOwnerPC.class);
        deleteAll(EmbeddedPC.class);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        EmbeddedOwnerPC pc = newEmbeddedOwnerPC();
        EmbeddedPC embed = new EmbeddedPC();
        embed.setIntField1(1000);
        pc.setStringField("orig");
        pm.persist(pc);
        pc.setEmbedded(embed);

        Object oid = pm.getObjectId(pc);
        if (!newPC) {
            endTx(pm);
            endEm(pm);
            pm = getPM();
            startTx(pm);
            pc = (EmbeddedOwnerPC) pm.find(EmbeddedOwnerPC.class, oid);
        }
        for (int i = 0; i < before; i++) {
            pc.setStringField("b" + i);
            pm.setSavepoint("b" + i);
        }

        pm.setSavepoint("test");
        embed = new EmbeddedPC();
        embed.setIntField1(2000);
        pc.setEmbedded(embed);

        for (int i = 0; i < after; i++) {
            pc.setStringField("b" + i);
            pm.setSavepoint("a" + i);
        }

        pm.rollbackToSavepoint("test");
        assertNotNull(pc.getEmbedded());
        assertEquals(1000, pc.getEmbedded().getIntField1());
        endTx(pm);
        endEm(pm);
    }

    public void testEmbeddedChange()
        throws Exception {
        try {
            doEmbeddedChangeTest(true, 0, 0);
            doEmbeddedChangeTest(true, 2, 0);
            doEmbeddedChangeTest(true, 0, 2);
            doEmbeddedChangeTest(true, 2, 2);
        } catch (Throwable t) {
            if (expectNewEmbeddedFailure())
                bug(1141, t, "changes to new embedded not detected");
            else
                throw (Exception) t;
        }
        doEmbeddedChangeTest(false, 0, 0);
        doEmbeddedChangeTest(false, 2, 0);
        doEmbeddedChangeTest(false, 0, 2);
        doEmbeddedChangeTest(false, 2, 2);
    }

    public void doEmbeddedChangeTest(boolean newPC, int before, int after) {
        deleteAll(EmbeddedOwnerPC.class);
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        EmbeddedOwnerPC pc = newEmbeddedOwnerPC();
        EmbeddedPC embed = new EmbeddedPC();
        embed.setIntField1(1000);
        pc.setStringField("orig");
        pm.persist(pc);
        pc.setEmbedded(embed);

        Object oid = pm.getObjectId(pc);
        if (!newPC) {
            endTx(pm);
            endEm(pm);
            pm = getPM();
            startTx(pm);
            pc = (EmbeddedOwnerPC) pm.find(EmbeddedOwnerPC.class, oid);
        }
        for (int i = 0; i < before; i++) {
            pc.setStringField("b" + i);
            pm.setSavepoint("b" + i);
        }

        pm.setSavepoint("test");
        pc.getEmbedded().setIntField1(2000);

        for (int i = 0; i < after; i++) {
            pc.setStringField("a" + i);
            pm.setSavepoint("a" + i);
        }

        pm.rollbackToSavepoint("test");
        assertNotNull(pc.getEmbedded());
        assertEquals(1000, pc.getEmbedded().getIntField1());
        endTx(pm);
        endEm(pm);
    }

    public void testEmbeddedChange2()
        throws Exception {
        try {
            doEmbeddedChangeTest2(true, 0, 0);
            doEmbeddedChangeTest2(true, 2, 0);
            doEmbeddedChangeTest2(true, 0, 2);
            doEmbeddedChangeTest2(true, 2, 2);
        } catch (Throwable t) {
            if (expectNewEmbeddedFailure())
                bug(1141, t, "changes to new embedded not detected");
            else
                throw (Exception) t;
        }
        doEmbeddedChangeTest2(false, 0, 0);
        doEmbeddedChangeTest2(false, 2, 0);
        doEmbeddedChangeTest2(false, 0, 2);
        doEmbeddedChangeTest2(false, 2, 2);
    }

    // variation with changing embedde fields vs owner field
    public void doEmbeddedChangeTest2(boolean newPC, int before, int after) {
        deleteAll(EmbeddedOwnerPC.class);
        deleteAll(EmbeddedPC.class);
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        EmbeddedOwnerPC pc = newEmbeddedOwnerPC();
        EmbeddedPC embed = new EmbeddedPC();
        embed.setIntField1(1000);
        pc.setStringField("orig");
        pm.persist(pc);
        pc.setEmbedded(embed);

        Object oid = pm.getObjectId(pc);
        if (!newPC) {
            endTx(pm);
            endEm(pm);
            pm = getPM();
            startTx(pm);
            pc = (EmbeddedOwnerPC) pm.find(EmbeddedOwnerPC.class, oid);
        }
        for (int i = 0; i < before; i++) {
            pc.getEmbedded().setIntField1(i);
            pm.setSavepoint("b" + i);
        }

        pm.setSavepoint("test");
        pc.getEmbedded().setIntField1(2000);

        for (int i = 0; i < after; i++) {
            pc.getEmbedded().setIntField1(i * -1);
            pm.setSavepoint("a" + i);
        }

        pm.rollbackToSavepoint("test");
        assertNotNull(pc.getEmbedded());
        if (before == 0)
            assertEquals(1000, pc.getEmbedded().getIntField1());
        else
            assertEquals(before - 1, pc.getEmbedded().getIntField1());
        endTx(pm);
        endEm(pm);
    }
}
