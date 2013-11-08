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

import java.util.*;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * Test for m-m
 *
 * @author Steve Kim
 */
public class TestManyToMany extends AnnotationTestCase
{
	public TestManyToMany(String name)
	{
		super(name, "annotationcactusapp");
	}

    public void setUp() {
        deleteAll(AnnoTest1.class);
        deleteAll(AnnoTest2.class);
    }

    public void testManyToMany() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        long lid = 4;
        AnnoTest1 pc = new AnnoTest1(lid);
        em.persist(pc);
        AnnoTest2 pc2;
        for (int i = 0; i < 3; i++) {
            pc2 = new AnnoTest2(5 + i, "foo" + i);
            pc.getManyMany().add(pc2);
            em.persist(pc2);
        }
        endTx(em);
        endEm(em);

        em =(OpenJPAEntityManager) currentEntityManager();
        pc = em.find(AnnoTest1.class, new Long(lid));
        Set<AnnoTest2> many = pc.getManyMany();
        assertEquals(3, many.size());
        for (AnnoTest2 manyPc2 : many) {
            switch ((int) manyPc2.getPk1()) {
                case 5:
                    assertEquals("foo0", manyPc2.getPk2());
                    break;
                case 6:
                    assertEquals("foo1", manyPc2.getPk2());
                    break;
                case 7:
                    assertEquals("foo2", manyPc2.getPk2());
                    break;
                default:
                    fail("bad pk:" + manyPc2.getPk1());
            }
        }
        endEm(em);
    }

    public void testInverseOwnerManyToMany() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        long lid = 4;
        AnnoTest1 pc = new AnnoTest1(lid);
        em.persist(pc);
        AnnoTest2 pc2;
        for (int i = 0; i < 3; i++) {
            pc2 = new AnnoTest2(5 + i, "foo" + i);
            pc2.getManyMany().add(pc);
            em.persist(pc2);
        }
        endTx(em);
        endEm(em);

        em =(OpenJPAEntityManager) currentEntityManager();
        pc = em.find(AnnoTest1.class, new Long(lid));
        Set<AnnoTest2> many = pc.getInverseOwnerManyMany();
        assertEquals(3, many.size());
        for (AnnoTest2 manyPc2 : many) {
            assertTrue(manyPc2.getManyMany().contains(pc));
            switch ((int) manyPc2.getPk1()) {
                case 5:
                    assertEquals("foo0", manyPc2.getPk2());
                    break;
                case 6:
                    assertEquals("foo1", manyPc2.getPk2());
                    break;
                case 7:
                    assertEquals("foo2", manyPc2.getPk2());
                    break;
                default:
                    fail("bad pk:" + manyPc2.getPk1());
            }
        }
        endEm(em);
    }
}
