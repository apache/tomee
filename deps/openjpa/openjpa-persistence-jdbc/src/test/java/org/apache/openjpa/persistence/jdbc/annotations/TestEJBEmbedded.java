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

import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.strats.ClobValueHandler;
import org.apache.openjpa.jdbc.meta.strats.MaxEmbeddedClobFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.StringFieldStrategy;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test for embedded
 *
 * @author Steve Kim
 */
public class TestEJBEmbedded extends SingleEMFTestCase {

    private static final String CLOB;

    static {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 1000; i++)
            buf.append('a');
        CLOB = buf.toString();
    }

    public void setUp() {
        setUp(EmbedOwner.class, EmbedValue.class, CLEAR_TABLES
//        ,"openjpa.Log","SQL=trace"    
        );
    }

    public void testEmbedded() {

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        EmbedOwner owner = new EmbedOwner();
        owner.setBasic("foo");
        EmbedValue embed = new EmbedValue();
        embed.setClob(CLOB);
        embed.setBasic("bar");
        embed.setBlob("foobar".getBytes());
        embed.setOwner(owner);
        owner.setEmbed(embed);
        Set<EmbedValue> embedVals = new HashSet<EmbedValue>();
        embedVals.add(embed);
        owner.setEmbedCollection(embedVals);
        em.persist(owner);
        int pk = owner.getPk();
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        owner = em.find(EmbedOwner.class, pk);
        assertEquals("foo", owner.getBasic());
        embed = owner.getEmbed();
        assertNotNull(embed);
        assertEquals(CLOB, embed.getClob());
        assertEquals("bar", embed.getBasic());
        assertEquals("foobar", new String(embed.getBlob()));
        assertEquals(owner, embed.getOwner());
        em.close();
    }

    public void testEmbeddedMetaData() {
        ClassMetaData ownerMeta =
            JPAFacadeHelper.getMetaData(emf, EmbedOwner.class);
        FieldMetaData fmd = ownerMeta.getField("embed");
        ClassMetaData embeddedMeta = fmd.getDefiningMetaData();
        assertNotNull(embeddedMeta);
        assertNull(embeddedMeta.getField("transientField"));
    }

    private void nullTestLogic(boolean cache) {
        // A place holder to swap the existing emf back in... maybe unnecessary?
        OpenJPAEntityManagerFactorySPI tempEmf = null;
        if (cache) {
            tempEmf = emf;
            emf = createEMF("openjpa.DataCache", "true");
        }
        try {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            
            EmbedOwner owner = new EmbedOwner();
            owner.setBasic("foo");
            
            assertNull(owner.getEmbed());
            assertNull(owner.getEmbedCollection());
            em.persist(owner);
            assertNull(owner.getEmbed());
            assertNull(owner.getEmbedCollection());
            
            int pk = owner.getPk();
            em.getTransaction().commit();
            assertNull(owner.getEmbed());
            assertNull(owner.getEmbedCollection());
            em.close();
            assertNull(owner.getEmbed());            
            assertNull(owner.getEmbedCollection());

            em = emf.createEntityManager();
            owner = em.find(EmbedOwner.class, pk);
            assertEquals("foo", owner.getBasic());
            EmbedValue embed = owner.getEmbed();
            assertNotNull(embed);
            assertNull(embed.getClob());
            assertNull(embed.getBasic());
            assertNull(embed.getBlob());
            
            Set<EmbedValue> embedCollection = owner.getEmbedCollection(); 
            assertNotNull(embedCollection);
            assertEquals(0, embedCollection.size());
            em.close();
        } finally {
            if(tempEmf!=null){
                emf.close();
                emf = tempEmf;
            }
        }
    }
    public void testNullNoCache() {
        nullTestLogic(false);
    }
    public void testNullCacheEnabled() {
        nullTestLogic(true);
    }

    public void testMappingTransferAndOverride() {
        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
        ClassMapping cls = conf.getMappingRepositoryInstance().getMapping
            (EmbedOwner.class, null, true);
        assertEquals("OWN_BASIC", cls.getFieldMapping("basic").
            getColumns()[0].getName());
        ClassMapping embed = cls.getFieldMapping("embed").getEmbeddedMapping();
        assertEquals("EMB_BLOB", embed.getFieldMapping("blob").
            getColumns()[0].getName());
        assertEquals("OVER_BASIC", embed.getFieldMapping("basic").
            getColumns()[0].getName());
        assertEquals("OVER_OWNER", embed.getFieldMapping("owner").
            getColumns()[0].getName());

        FieldMapping fm = embed.getFieldMapping("clob");
        DBDictionary dict = conf.getDBDictionaryInstance();
        if (dict.getPreferredType(Types.CLOB) == Types.CLOB) {
            if (dict.maxEmbeddedClobSize > 0)
                assertTrue(fm.getStrategy() instanceof
                    MaxEmbeddedClobFieldStrategy);
            else
                assertTrue(fm.getHandler() instanceof ClobValueHandler);
        } else
            assertTrue(fm.getStrategy() instanceof StringFieldStrategy);
    }
}
