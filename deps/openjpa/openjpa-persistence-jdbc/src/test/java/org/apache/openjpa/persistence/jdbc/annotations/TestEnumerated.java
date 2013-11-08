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
package org.apache.openjpa.persistence.jdbc.annotations;

import javax.persistence.InheritanceType;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test enums and the @Enumerated annotation.
 *
 * @author Abe White
 */
public class TestEnumerated
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(AnnoTest1.class, AnnoTest2.class, Flat1.class, CLEAR_TABLES);
    }

    public void testMapping() {
        ClassMapping cls = (ClassMapping) emf.getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(AnnoTest1.class, 
            null, true);
        FieldMapping fm = cls.getDeclaredFieldMapping("enumeration");
        assertNotNull(fm);
        assertEquals(FieldMapping.MANAGE_PERSISTENT, fm.getManagement());
        assertEquals(JavaTypes.ENUM, fm.getTypeCode());
        assertEquals(JavaTypes.SHORT, fm.getColumns()[0].getJavaType());

        fm = cls.getDeclaredFieldMapping("ordinalEnumeration");
        assertNotNull(fm);
        assertEquals(FieldMapping.MANAGE_PERSISTENT, fm.getManagement());
        assertEquals(JavaTypes.ENUM, fm.getTypeCode());
        assertEquals(JavaTypes.SHORT, fm.getColumns()[0].getJavaType());

        fm = cls.getDeclaredFieldMapping("stringEnumeration");
        assertNotNull(fm);
        assertEquals(FieldMapping.MANAGE_PERSISTENT, fm.getManagement());
        assertEquals(JavaTypes.ENUM, fm.getTypeCode());
        assertEquals(JavaTypes.STRING, fm.getColumns()[0].getJavaType());
    }

    public void testBehavior() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc = new AnnoTest1(1);
        pc.setEnumeration(InheritanceType.TABLE_PER_CLASS);
        pc.setOrdinalEnumeration(InheritanceType.TABLE_PER_CLASS);
        pc.setStringEnumeration(InheritanceType.JOINED);
        em.persist(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(1));
        assertEquals(InheritanceType.TABLE_PER_CLASS, pc.getEnumeration());
        assertEquals(InheritanceType.TABLE_PER_CLASS,
            pc.getOrdinalEnumeration());
        assertEquals(InheritanceType.JOINED,
            pc.getStringEnumeration());
        em.getTransaction().begin();
        pc.setEnumeration(InheritanceType.JOINED);
        pc.setOrdinalEnumeration(InheritanceType.JOINED);
        pc.setStringEnumeration(InheritanceType.TABLE_PER_CLASS);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(1));
        assertEquals(InheritanceType.JOINED, pc.getEnumeration());
        assertEquals(InheritanceType.JOINED, pc.getOrdinalEnumeration());
        assertEquals(InheritanceType.TABLE_PER_CLASS,
            pc.getStringEnumeration());
        em.close();
    }
}
