/*
 * TestGetObjectsById.java
 *
 * Created on October 12, 2006, 10:49 AM
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



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest4;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest5;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestGetObjectsById extends BaseKernelTest {

    private Object[] oids;

    /**
     * Creates a new instance of TestGetObjectsById
     */
    public TestGetObjectsById(String name) {
        super(name);
    }

    public void setUp() {
        deleteAll(RuntimeTest5.class);
        deleteAll(RuntimeTest4.class);

        RuntimeTest4 rt4 = new RuntimeTest4("foo");

        RuntimeTest5 related0 = new RuntimeTest5("bar");
        related0.setRuntimeTest4(rt4);
        rt4.getRuntimeTest5s().add(related0);

        RuntimeTest5 related1 = new RuntimeTest5("baz");
        related1.setRuntimeTest4(rt4);
        rt4.getRuntimeTest5s().add(related1);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(rt4);
        endTx(pm);
        oids = new Object[]
            {
                pm.getObjectId(rt4),
                pm.getObjectId(related0),
                pm.getObjectId(related1),
            };
        endEm(pm);
    }

    public void testGetObjectsByIdInvocation() {
        OpenJPAEntityManager pm = getPM();
        try {
            Object[] pcs = pm.findAll(Object.class, oids);
            assertEquals(oids.length, pcs.length);
            for (int i = 0; i < oids.length; i++)
                assertEquals(oids[i], pm.getObjectId(pcs[i]));
        } catch (Exception e) {
            bug(1017, e, "getObjectsById() bug");
        } finally {
            endEm(pm);
        }
    }
}
