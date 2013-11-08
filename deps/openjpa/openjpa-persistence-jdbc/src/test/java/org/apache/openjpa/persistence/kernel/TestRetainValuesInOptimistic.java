/*
 * TestRetainValuesInOptimistic.java
 *
 * Created on October 16, 2006, 10:18 AM
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;

public class TestRetainValuesInOptimistic extends BaseKernelTest {

    private Object _oid = null;

    /**
     * Creates a new instance of TestRetainValuesInOptimistic
     */
    public TestRetainValuesInOptimistic() {
    }

    public TestRetainValuesInOptimistic(String test) {
        super(test);
    }

    public void setUp()
        throws Exception {
        super.setUp();

        deleteAll(RuntimeTest1.class);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        RuntimeTest1 pc = new RuntimeTest1("str1", 1);
        pm.persist(pc);
        endTx(pm);
        _oid = pm.getObjectId(pc);
        endEm(pm);
    }

    public void testRetain() {
        clearTest(true);
        optLockTest(true);
    }

    public void testNotRetain() {
        clearTest(false);
        optLockTest(false);
    }

    private void clearTest(boolean retain) {
        OpenJPAEntityManager pm = getPM(retain);
        OpenJPAEntityManagerFactory pmf = pm.getEntityManagerFactory();
        RuntimeTest1 pc = (RuntimeTest1) pm.find(RuntimeTest1.class, _oid);

        OpenJPAEntityManager pm2 = getPM();
        RuntimeTest1 pc2 = (RuntimeTest1) pm2.find(RuntimeTest1.class, _oid);
        startTx(pm2);
        pc2.setStringField("str2");
        pc2.setIntField1(2);
        endTx(pm2);
        endEm(pm2);

        startTx(pm);
        // tickle the object so that it enters the transaction
        pc.setLocaleField(Locale.CHINA);
        assertEquals((retain) ? "str1" : "str2", pc.getStringField());
        assertEquals((retain) ? 1 : 2, pc.getIntField1());
        try {
            endTx(pm);
            if (retain)
                fail("Should have caused OL violation");
        }
        catch (RuntimeException re) {
            if (!retain)
                throw re;
        }
        catch (Exception e) {
            //
        }

        endEm(pm);

        // make sure everything stuck
        if (!retain) {
            pm = getPM();
            pc = (RuntimeTest1) pm.find(RuntimeTest1.class, _oid);
            assertEquals("str2", pc.getStringField());
            assertEquals(2, pc.getIntField1());
            endEm(pm);
            try {
                pmf.close();
            } catch (Exception e) {
                // consumme exceptions ... other PMs might be open and
                // active on this PMF.
            }
        }
    }

    private void optLockTest(boolean retain) {
        OpenJPAEntityManager pm1 = getPM(retain);
        OpenJPAEntityManagerFactory pmf = pm1.getEntityManagerFactory();

        startTx(pm1);
        RuntimeTest1 pc1 = (RuntimeTest1) pm1.find(RuntimeTest1.class, _oid);
        endTx(pm1);

        OpenJPAEntityManager pm2 = getPM(retain);
        startTx(pm2);
        RuntimeTest1 pc2 = (RuntimeTest1) pm2.find(RuntimeTest1.class, _oid);
        pc2.setStringField("str3");
        pc2.setIntField1(3);
        endTx(pm2);

        startTx(pm1);
        pc1.setStringField("str4");
        pc1.setIntField1(4);
        try {
            endTx(pm1);
            if (retain)
                fail("Expected opt lock error.");
        } catch (Exception jove) {
            if (!retain)
                fail("Caught opt lock error.");
        }

        pm1.close();
        endEm(pm2);
        if (retain) {
            try {
                pmf.close();
            } catch (Exception e) {
                // maybe other PMs are open...
            }
        }
    }

    private OpenJPAEntityManager getPM(boolean retain) {
        OpenJPAEntityManager pm;
        Map props = new HashMap();
        props.put("openjpa.AutoClear", "all");

        if (retain)
            pm = getPM(true, true);
        else {
            OpenJPAEntityManagerFactory pmf = getEmf(props);

            pm = (OpenJPAEntityManager) pmf.createEntityManager();
            pm.setOptimistic(true);
            pm.setRetainState(true);
        }
        return pm;
    }
}
