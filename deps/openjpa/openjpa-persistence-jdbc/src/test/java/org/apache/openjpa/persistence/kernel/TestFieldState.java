/*
 * TestFieldState.java
 *
 * Created on October 12, 2006, 10:22 AM
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
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestFieldState extends BaseKernelTest {

    private Object oid;

    /**
     * Creates a new instance of TestFieldState
     */
    public TestFieldState() {
    }

    public TestFieldState(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);

        // create a test object
        RuntimeTest1 a = new RuntimeTest1("foo", 3);
        pm.persist(a);

        endTx(pm);

        oid = pm.getObjectId(a);
        endEm(pm);
    }

    public void testNotDirtyAfterSameChange() {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);

        RuntimeTest1 a = pm.find(RuntimeTest1.class, oid);
        a.setStringField(a.getStringField());
        OpenJPAStateManager sm = getStateManager(a, pm);
        FieldMetaData fmd = sm.getMetaData().getField("stringField");
        assertTrue(sm.getDirty().get(fmd.getIndex()) == false);

        endTx(pm);
        endEm(pm);
    }
}
