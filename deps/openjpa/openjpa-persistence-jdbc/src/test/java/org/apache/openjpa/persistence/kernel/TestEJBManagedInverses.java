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

import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.InverseA;
import org.apache.openjpa.persistence.kernel.common.apps.InverseB;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBManagedInverses extends AbstractTestCase {

    public TestEJBManagedInverses(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        super.setUp(InverseA.class, InverseB.class);
    }

    public void testOneOne() {
        oneOneTest(true);
    }

    public void testOneOneWithPreStore() {
        oneOneTest(false);
    }

    private void oneOneTest(boolean flush) {
        EntityManager pm = currentEntityManager();
        startTx(pm);
        InverseA a = new InverseA();
        InverseA a2 = new InverseA();
        InverseA a3 = new InverseA();
        a.setOneOne(a2);
        a.setOneOneOwner(a2);
        pm.persist(a);
        pm.persist(a3);

//		assertEquals (a, a2.getOneOne ());
//		assertEquals (a, a2.getOneOneOwner ());
        assertEquals("a.getOneOne is not a2", a2, a.getOneOne());
        assertEquals("a.getOneOneOwner is not a2", a2, a.getOneOneOwner());
        endTx(pm);

        startTx(pm);
        a2.setOneOne(a3);
        a2.setOneOneOwner(a3);

//		assertEquals (a2, a3.getOneOne ());
//		assertEquals (a2, a3.getOneOneOwner ());
        assertEquals(a3, a2.getOneOne());
        assertEquals(a3, a2.getOneOneOwner());
        endTx(pm);

        // make sure commit doesn't retrigger changes
//		assertEquals (a2, a3.getOneOne ());
//		assertEquals (a2, a3.getOneOneOwner ());
        assertEquals(a3, a2.getOneOne());
        assertEquals(a3, a2.getOneOneOwner());

        // test persistent -> dirty
        startTx(pm);
        a2.setOneOne(null);
        a2.setOneOneOwner(null);
//		if (flush)
//			pm.flush ();
//		else
//			pm.setFlushMode(FlushModeType.AUTO);
//		assertNull (a3.getOneOne ());
//		assertNull (a3.getOneOneOwner ());
        endTx(pm);
        endEm(pm);
    }
}
