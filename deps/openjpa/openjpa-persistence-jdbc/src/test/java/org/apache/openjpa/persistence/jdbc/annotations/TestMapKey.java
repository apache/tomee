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

import javax.persistence.MapKey;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.strats.RelationMapInverseKeyFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.RelationMapTableFieldStrategy;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <p>Test the use of the {@link MapKey} annotation.  Note that we have
 * more thorough Kodo core tests for the mappings themselves.  See
 * {@link kodo.jdbc.meta.TestMappedByKeyMaps}.
 *
 * @author Abe White
 */
public class TestMapKey
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(Flat1.class, AnnoTest1.class, AnnoTest2.class, CLEAR_TABLES);

    }

    public void testDefaultMapKeyMapping() {
        ClassMapping mapping = ((JDBCConfiguration) emf.getConfiguration())
            .getMappingRepositoryInstance().getMapping(AnnoTest1.class,
                null, true);
        FieldMapping fm = mapping.getFieldMapping("defaultMapKey");
        assertTrue(fm.getStrategy() instanceof RelationMapTableFieldStrategy);
        assertEquals("pk", fm.getKey().getValueMappedBy());
        assertEquals(mapping.getRepository().getMetaData(Flat1.class, null,
            true).getField("pk"), fm.getKey().getValueMappedByMetaData());
    }

    public void testNamedMapKeyMapping() {
        ClassMapping mapping = ((JDBCConfiguration) emf.getConfiguration())
            .getMappingRepositoryInstance().getMapping(AnnoTest1.class,
                null, true);
        FieldMapping fm = mapping.getFieldMapping("namedMapKey");
        assertTrue(fm.getStrategy() instanceof RelationMapTableFieldStrategy);
        assertEquals("basic", fm.getKey().getValueMappedBy());
        assertEquals(mapping.getRepository().getMetaData(Flat1.class, null,
            true).getField("basic"), fm.getKey().getValueMappedByMetaData());
    }

    public void testInverseOwnerMapKeyMapping() {
        ClassMapping mapping = ((JDBCConfiguration) emf.getConfiguration())
            .getMappingRepositoryInstance().getMapping(AnnoTest1.class,
                null, true);
        FieldMapping fm = mapping.getFieldMapping("inverseOwnerMapKey");
        assertTrue(fm.getStrategy() instanceof
            RelationMapInverseKeyFieldStrategy);
        assertEquals("basic", fm.getKey().getValueMappedBy());
        assertEquals(mapping.getRepository().getMetaData(AnnoTest2.class,
            null, true).getField("basic"), fm.getKey().
            getValueMappedByMetaData());
    }

    public void testInsertAndRetrieve() {
        Flat1 f1 = new Flat1(1);
        f1.setBasic(100);
        Flat1 f2 = new Flat1(2);
        f2.setBasic(200);
        AnnoTest2 a1 = new AnnoTest2(1L, "1");
        a1.setBasic("100");
        AnnoTest2 a2 = new AnnoTest2(2L, "2");
        a2.setBasic("200");

        AnnoTest1 pc = new AnnoTest1(1L);
        pc.getDefaultMapKey().put(f1.getPk(), f1);
        pc.getDefaultMapKey().put(f2.getPk(), f2);
        pc.getNamedMapKey().put(f1.getBasic(), f1);
        pc.getNamedMapKey().put(f2.getBasic(), f2);
        pc.getInverseOwnerMapKey().put(a1.getBasic(), a1);
        pc.getInverseOwnerMapKey().put(a2.getBasic(), a2);
        a1.setOneManyOwner(pc);
        a2.setOneManyOwner(pc);

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persistAll(new Object[]{ pc, f1, f2, a1, a2 });
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(1));
        assertEquals(2, pc.getDefaultMapKey().size());
        assertEquals(1, pc.getDefaultMapKey().get(1).getPk());
        assertEquals(2, pc.getDefaultMapKey().get(2).getPk());
        assertEquals(2, pc.getNamedMapKey().size());
        assertEquals(100, pc.getNamedMapKey().get(100).getBasic());
        assertEquals(200, pc.getNamedMapKey().get(200).getBasic());
        assertEquals(2, pc.getInverseOwnerMapKey().size());
        assertEquals("100", pc.getInverseOwnerMapKey().get("100").
            getBasic());
        assertEquals("200", pc.getInverseOwnerMapKey().get("200").
            getBasic());
        em.close();
    }
}
