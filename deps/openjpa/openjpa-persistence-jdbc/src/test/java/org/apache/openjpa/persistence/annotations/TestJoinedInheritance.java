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

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;

import org.apache.openjpa.persistence.OpenJPAEntityManager;


/**
 * Test for InheritanceType.JOINED
 *
 * @author Steve Kim
 */
public class TestJoinedInheritance extends AnnotationTestCase
{

	public TestJoinedInheritance(String name)
	{
		super(name, "annotationcactusapp");
	}

    public void setUp()
    {
        new AnnoTest1();
        new AnnoTest2();
        new AnnoTest3();

        deleteAll(AnnoTest1.class);
        deleteAll(AnnoTest2.class);
    }

    public void testInheritance() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        AnnoTest3 pc = new AnnoTest3();
        pc.setPk(new Long(1));
        pc.setBasic(10);
        pc.setBasic2(20);
        em.persist(pc);

        AnnoTest2 pc2 = new AnnoTest2();
        pc2.setPk1(2);
        pc2.setPk2("bar");
        pc2.setInverseOneOne(pc);
        pc.setSubOneOne(pc2);
        em.persist(pc2);

        endTx(em);
        endEm(em);

        em =(OpenJPAEntityManager) currentEntityManager();
        pc2 = em.find(AnnoTest2.class, new AnnoTest2.Oid(2, "bar"));
        pc = (AnnoTest3) pc2.getInverseOneOne();
        assertEquals(pc.getInverseOwnerOneOne(), pc2);
        assertEquals(10, pc.getBasic());
        assertEquals(20, pc.getBasic2());
        assertEquals(pc2, pc.getSubOneOne());
        endEm(em);
    }
}
