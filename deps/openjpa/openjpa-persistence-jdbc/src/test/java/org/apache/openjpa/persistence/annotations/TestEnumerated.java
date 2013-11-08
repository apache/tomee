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
package org.apache.openjpa.persistence.annotations;

import org.apache.openjpa.persistence.*;

import javax.persistence.*;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;





/**
 * Test enums and the @Enumerated annotation.
 *
 * @author Abe White
 */
public class TestEnumerated extends AnnotationTestCase
{

	public TestEnumerated(String name)
	{
		super(name, "annotationcactusapp");
	}

    public void setUp() {
        deleteAll(AnnoTest1.class);
    }

   /** public void testMapping() {
        ClassMapping cls = (ClassMapping) getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(AnnoTest1.class,
            null, true);
        FieldMapping fm = cls.getDeclaredFieldMapping("enumeration");
        assertNotNull(fm);
        assertEquals(FieldMapping.MANAGE_PERSISTENT, fm.getManagement());
        assertEquals(JavaTypes.OBJECT, fm.getTypeCode());
        assertEquals(JavaTypes.SHORT, fm.getColumns()[0].getJavaType());

        fm = cls.getDeclaredFieldMapping("ordinalEnumeration");
        assertNotNull(fm);
        assertEquals(FieldMapping.MANAGE_PERSISTENT, fm.getManagement());
        assertEquals(JavaTypes.OBJECT, fm.getTypeCode());
        assertEquals(JavaTypes.SHORT, fm.getColumns()[0].getJavaType());

        fm = cls.getDeclaredFieldMapping("stringEnumeration");
        assertNotNull(fm);
        assertEquals(FieldMapping.MANAGE_PERSISTENT, fm.getManagement());
        assertEquals(JavaTypes.OBJECT, fm.getTypeCode());
        assertEquals(JavaTypes.STRING, fm.getColumns()[0].getJavaType());
    }*/

    public void testBehavior() 
    {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        
        AnnoTest1 pc = new AnnoTest1(1);
        assertNotNull("pc is null", pc);
        assertNotNull("InheritanceType.TABLE_PER_CLASS is null",
                InheritanceType.TABLE_PER_CLASS);
        assertNotNull("InheritanceType.JOINED is null", InheritanceType.JOINED);
        pc.setEnumeration(InheritanceType.TABLE_PER_CLASS);
        pc.setOrdinalEnumeration(InheritanceType.TABLE_PER_CLASS);
        pc.setStringEnumeration(InheritanceType.JOINED);
        em.persist(pc);
        endTx(em);
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        OpenJPAQuery q = em.createQuery("SELECT o FROM AnnoTest1 o"); 
        assertEquals(1, q.getResultList().size());
        
//        AnnoTest1 pc2 = em.find(AnnoTest1.class, new Long(1));
        AnnoTest1 pc2 = (AnnoTest1) q.getSingleResult();
        assertNotNull("pc2 is null", pc2);
        assertEquals(InheritanceType.TABLE_PER_CLASS, pc2.getEnumeration());
        assertEquals(InheritanceType.TABLE_PER_CLASS,
                pc2.getOrdinalEnumeration());
        assertEquals(InheritanceType.JOINED,  pc2.getStringEnumeration());
        startTx(em);
        pc2.setEnumeration(InheritanceType.JOINED);
        pc2.setOrdinalEnumeration(InheritanceType.JOINED);
        pc2.setStringEnumeration(InheritanceType.TABLE_PER_CLASS);
        endTx(em);
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
//        pc2 = em.find(AnnoTest1.class, new Long(1));
        q = em.createQuery("SELECT o FROM AnnoTest1 o"); 
        pc2 = (AnnoTest1) q.getSingleResult();
        assertEquals(InheritanceType.JOINED, pc2.getEnumeration());
        assertEquals(InheritanceType.JOINED, pc2.getOrdinalEnumeration());
        assertEquals(InheritanceType.TABLE_PER_CLASS,
                pc2.getStringEnumeration());
        endEm(em);
    }
    
}
