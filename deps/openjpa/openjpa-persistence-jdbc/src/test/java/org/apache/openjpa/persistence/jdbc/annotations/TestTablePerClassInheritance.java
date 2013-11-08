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

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.strats.FullClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.NoneDiscriminatorStrategy;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <p>Test that InheritanceType.TABLE_PER_CLASS JPA mapping is translated
 * correctly.  See the <code>kodo.jdbc.meta.tableperclass</code> test package
 * for more detailed tests of functionality.</p>
 *
 * @author Abe White
 */
public class TestTablePerClassInheritance
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(TablePerClass1.class, TablePerClass2.class, EmbedOwner.class,
            EmbedValue.class, CLEAR_TABLES);
    }

    public void testMapping() {
        ClassMapping mapping = ((JDBCConfiguration) emf.getConfiguration()).
            getMappingRepositoryInstance().getMapping(TablePerClass2.class,
            null, true);
        assertTrue(mapping.getStrategy() instanceof FullClassStrategy);
        assertTrue(mapping.getDiscriminator().getStrategy()
            instanceof NoneDiscriminatorStrategy);
        assertNull(mapping.getJoinForeignKey());
        assertNull(mapping.getJoinablePCSuperclassMapping());
        assertEquals("TPC_BASIC", mapping.getFieldMapping("basic").
            getColumns()[0].getName());
        ClassMapping embed = mapping.getFieldMapping("embed").
            getEmbeddedMapping();
        assertEquals("TPC_EMB_BASIC", embed.getFieldMapping("basic").
            getColumns()[0].getName());

        ClassMapping sup = mapping.getPCSuperclassMapping();
        assertEquals(TablePerClass1.class, sup.getDescribedType());
        assertTrue(sup.getStrategy() instanceof FullClassStrategy);
        assertTrue(sup.getDiscriminator().getStrategy()
            instanceof NoneDiscriminatorStrategy);
        assertEquals("TPC_BASIC", sup.getFieldMapping("basic").
            getColumns()[0].getName());
        embed = sup.getFieldMapping("embed").getEmbeddedMapping();
        assertEquals("TPC_EMB_BASIC", embed.getFieldMapping("basic").
            getColumns()[0].getName());
    }

    public void testInsertAndRetrieve() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        TablePerClass1 tpc1 = new TablePerClass1();
        tpc1.setBasic(1);
        EmbedValue ev = new EmbedValue();
        ev.setBasic("11");
        tpc1.setEmbed(ev);
        TablePerClass2 tpc2 = new TablePerClass2();
        tpc2.setBasic(2);
        tpc2.setBasic2("2");
        ev = new EmbedValue();
        ev.setBasic("22");
        tpc2.setEmbed(ev);
        em.persistAll(tpc1, tpc2);
        em.getTransaction().commit();
        int id1 = tpc1.getPk();
        int id2 = tpc2.getPk();
        em.close();

        em = emf.createEntityManager();
        tpc1 = em.find(TablePerClass1.class, id1);
        assertEquals(1, tpc1.getBasic());
        assertEquals("11", tpc1.getEmbed().getBasic());
        tpc2 = (TablePerClass2) em.find(TablePerClass1.class, id2);
        assertEquals(2, tpc2.getBasic());
        assertEquals("2", tpc2.getBasic2());
        assertEquals("22", tpc2.getEmbed().getBasic());
        em.close();
    }
}
