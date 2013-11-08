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
package org.apache.openjpa.persistence.enhance;


import org.apache.openjpa.persistence.enhance.common.apps.NoClone;
import org.apache.openjpa.persistence.enhance.common.apps.PCClone;
import org.apache.openjpa.persistence.enhance.common.apps.SubclassClone;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestClone
    extends AbstractTestCase {

    public TestClone(String name) {
        super(name, "enhancecactusapp");
    }

    public void setUp() {

        deleteAll(NoClone.class);
        deleteAll(PCClone.class);
        deleteAll(SubclassClone.class);
    }

    /**
     * test no clone method declared.
     */
    public void testNoClone() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        NoClone orig = new NoClone("test");
        pm.persist(orig);
        endTx(pm);
        pm.refresh(orig);
        NoClone copy = orig.safeClone();
        assertEquals("test", copy.getString());
        assertTrue(pm.isPersistent(orig));
        assertFalse(pm.isPersistent(copy));
        endEm(pm);
    }

    /**
     * test subclass which we cannot handle safely
     */
    public void testSubclassClone() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        SubclassClone orig = new SubclassClone("test");
        pm.persist(orig);
        endTx(pm);
        pm.refresh(orig);
        SubclassClone copy = orig.safeClone();
        assertEquals("test", copy.getString());
        assertTrue(pm.isPersistent(orig));
        // this should be true since the sm should be VM copied.
        // as we intentionally don't handle this case.
        assertTrue(pm.isPersistent(copy));
        endEm(pm);
    }

    /**
     * test explicit clone call
     */
    public void testPCClone() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        PCClone orig = new PCClone("test");
        pm.persist(orig);
        endTx(pm);
        pm.refresh(orig);
        PCClone copy = (PCClone) orig.clone();
        assertEquals("test", copy.getString());
        assertTrue(pm.isPersistent(orig));
        assertFalse(pm.isPersistent(copy));
        endEm(pm);
    }
}

