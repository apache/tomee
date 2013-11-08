/*
 * TestSerialize.java
 *
 * Created on October 13, 2006, 5:13 PM
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

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestSerialize extends BaseKernelTest {

    private int _oid = 0;
    private Object _oid2 = null;

    /**
     * Creates a new instance of TestSerialize
     */
    public TestSerialize() {
    }

    public TestSerialize(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        RuntimeTest1 a = new RuntimeTest1("1NAME", 1);
        a.setSelfOneOne(new RuntimeTest1("2NAME", 2));

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(a);
        _oid = a.getIntField();

        endTx(pm);
        endEm(pm);
    }

    public void testConfigurationSerializable()
        throws Exception {
        // validate that the hashCode() and equals() method hold true
        // for the round-trip.
        roundtrip(getConfiguration(), true);
    }

    public void testSerialize()
        throws Exception {
        OpenJPAEntityManager pm = getPM();
        //FIXME jthomas
        /*pm.getFetchPlan().setDetachmentOptions
                (FetchPlan.DETACH_ALL_FIELDS);
         */
        RuntimeTest1 a = (RuntimeTest1) pm.find(RuntimeTest1.class, _oid);

        assertEquals("1NAME", a.getStringField());
        assertEquals(1, a.getIntField());
        assertNotNull(a.getSelfOneOne());
        assertEquals("2NAME", a.getSelfOneOne().getStringField());
        assertEquals(2, a.getSelfOneOne().getIntField());

        a = (RuntimeTest1) roundtrip(a, false);

        assertEquals("1NAME", a.getStringField());
        assertEquals(1, a.getIntField());
        assertNotNull(a.getSelfOneOne());
        assertEquals("2NAME", a.getSelfOneOne().getStringField());
        assertEquals(2, a.getSelfOneOne().getIntField());
    }
}
