/*
 * TestPCClass.java
 *
 * Created on October 12, 2006, 4:27 PM
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

import java.io.Serializable;



import org.apache.openjpa.persistence.kernel.common.apps.PCClassInterface;
import org.apache.openjpa.persistence.kernel.common.apps.PCClassPC;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestPCClass extends BaseKernelTest {

    /**
     * Creates a new instance of TestPCClass
     */
    public TestPCClass() {
    }

    public TestPCClass(String test) {
        super(test);
    }

    public void setUp() throws Exception {
        super.setUp(PCClassPC.class);
    }

    public void testMetaData() {

        MetaDataRepository repos = getConfiguration()
            .getMetaDataRepositoryInstance();
        ClassMetaData meta = repos.getMetaData(PCClassPC.class, null, true);

        FieldMetaData fmd = meta.getField("specificPC");
        assertNotNull("fmd is nulll", fmd);
        assertEquals("JavaTypes.PC != fmd.getTypeCode()", JavaTypes.PC,
            fmd.getTypeCode());
        assertEquals("PCClassPC.class.getName() != fmd.getType().getName()",
            PCClassPC.class.getName(), fmd.getType().getName());
        assertEquals(
            "Object.class.getName() != fmd.getDeclaredType().getName()",
            Object.class.getName(), fmd.getDeclaredType().getName());

        fmd = meta.getField("genericPC");
        assertNotNull("fmd is null", fmd);
        assertEquals("JavaTypes.PC_UNTYPE != fmd.getTypeCode()",
            JavaTypes.PC_UNTYPED, fmd.getTypeCode());
        assertEquals(
            "PersistenceCapable.class.getName() != fmd.getType().getName()",
            PersistenceCapable.class.getName(), fmd.getType().getName());
        assertEquals(Object.class.getName(),
            fmd.getDeclaredType().getName());

        fmd = meta.getField("genericObject");
        assertNotNull(fmd);
        assertEquals(JavaTypes.OBJECT, fmd.getTypeCode());
        assertEquals(Object.class.getName(), fmd.getType().getName());
        assertEquals(Object.class.getName(),
            fmd.getDeclaredType().getName());

        fmd = meta.getField("specificInterface");
        assertNotNull(fmd);
        assertEquals(JavaTypes.PC, fmd.getTypeCode());
        assertEquals(PCClassPC.class.getName(), fmd.getType().getName());
        assertEquals(PCClassInterface.class.getName(),
            fmd.getDeclaredType().getName());

        fmd = meta.getField("defaultInterface");
        assertNotNull(fmd);
        assertEquals(JavaTypes.PC_UNTYPED, fmd.getTypeCode());
        assertEquals(PCClassInterface.class.getName(),
            fmd.getType().getName());
        assertEquals(PCClassInterface.class.getName(),
            fmd.getDeclaredType().getName());

        fmd = meta.getField("serializableInterface");
        assertNotNull(fmd);
        assertEquals(JavaTypes.OBJECT, fmd.getTypeCode());
        assertEquals(Serializable.class.getName(), fmd.getType().getName());
        assertEquals(Serializable.class.getName(),
            fmd.getDeclaredType().getName());

        fmd = meta.getField("genericInterface");
        assertNotNull(fmd);
        assertEquals(JavaTypes.OBJECT, fmd.getTypeCode());
        assertEquals(Object.class.getName(), fmd.getType().getName());
        assertEquals(PCClassInterface.class.getName(),
            fmd.getDeclaredType().getName());
    }

    public void testPersist() {
        PCClassPC pc = new PCClassPC();
        pc.setSpecificPC(pc);
        pc.setGenericPC(pc);
        pc.setGenericObject(pc);
        pc.setSpecificInterface(pc);
        pc.setDefaultInterface(pc);
        pc.setGenericInterface(pc);
        pc.setSerializableInterface(pc);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = getPM();

        pc = pm.find(PCClassPC.class, oid);

        assertTrue(pc == pc.getSpecificPC());
        assertTrue(pc == pc.getGenericPC());
        assertNotNull(pc.getGenericObject());
        assertTrue(pc == pc.getGenericObject());
        assertTrue(pc == pc.getSpecificInterface());
        assertTrue(pc == pc.getDefaultInterface());
        assertNotNull(pc.getGenericInterface());
        assertTrue(pc == pc.getGenericInterface());
        assertNotNull(pc.getSerializableInterface());
        assertTrue(pc == pc.getSerializableInterface());

        endEm(pm);
    }
}
