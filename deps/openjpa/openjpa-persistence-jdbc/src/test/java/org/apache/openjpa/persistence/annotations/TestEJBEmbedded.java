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

import org.apache.openjpa.persistence.OpenJPAEntityManager;

//import kodo.persistence.test.*;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;
import org.apache.openjpa.persistence.test.AllowFailure;

/**
 * Test for embedded
 *
 * @author Steve Kim
 */
@AllowFailure(message="excluded")
public class TestEJBEmbedded extends AnnotationTestCase
{

	public TestEJBEmbedded(String name)
	{
		super(name, "annotationcactusapp");
	}

    private static final String CLOB;

    static {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 1000; i++)
            buf.append('a');
        CLOB = buf.toString();
    }

    public void setUp()
    {
        deleteAll (EmbedOwner.class);
    }

    public void testEmbedded()
    {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        EmbedOwner owner = new EmbedOwner();
        owner.setBasic("foo");
        EmbedValue embed = new EmbedValue();
        embed.setClob(CLOB);
        embed.setBasic("bar");
        embed.setBlob("foobar".getBytes());
        embed.setOwner(owner);
        owner.setEmbed(embed);
        em.persist(owner);
        int pk = owner.getPk();
        endTx(em);
        endEm(em);

        em =(OpenJPAEntityManager) currentEntityManager();
        owner = em.find(EmbedOwner.class, pk);
        assertEquals("foo", owner.getBasic());
        embed = owner.getEmbed();
        assertNotNull(embed);
        assertEquals(CLOB, embed.getClob());
        assertEquals("bar", embed.getBasic());
        assertEquals("foobar", new String(embed.getBlob()));
        assertEquals(owner, embed.getOwner());
        endEm(em);
    }

    public void testNull() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        EmbedOwner owner = new EmbedOwner();
        owner.setBasic("foo");
        em.persist(owner);
        int pk = owner.getPk();
        endTx(em);
        endEm(em);

        em =(OpenJPAEntityManager) currentEntityManager();
        owner = em.find(EmbedOwner.class, pk);
        assertEquals("foo", owner.getBasic());
        EmbedValue embed = owner.getEmbed();
        assertNotNull(embed);
        assertNull(embed.getClob());
        assertNull(embed.getBasic());
        assertNull(embed.getBlob());
        startTx(em);
    }

//    public void testMappingTransferAndOverride() {
//        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
//        ClassMapping cls = conf.getMappingRepositoryInstance().getMapping
//            (EmbedOwner.class, null, true);
//        assertEquals("OWN_BASIC", cls.getFieldMapping("basic").
//            getColumns()[0].getName());
//        ClassMapping embed = cls.getFieldMapping("embed")
//            .currentEntityManager()beddedMapping();
//        assertEquals("EMB_BLOB", embed.getFieldMapping("blob").
//            getColumns()[0].getName());
//        assertEquals("OVER_BASIC", embed.getFieldMapping("basic").
//            getColumns()[0].getName());
//        assertEquals("OVER_OWNER", embed.getFieldMapping("owner").
//            getColumns()[0].getName());
//
//        FieldMapping fm = embed.getFieldMapping("clob");
//        DBDictionary dict = conf.getDBDictionaryInstance();
//        if (dict.getPreferredType(Types.CLOB) == Types.CLOB) {
//            if (dict.maxEmbeddedClobSize > 0)
//                assertTrue(fm.getStrategy() instanceof
//                    MaxEmbeddedClobFieldStrategy);
//            else
//                assertTrue(fm.getHandler() instanceof ClobValueHandler);
//        } else
//            assertTrue(fm.getStrategy() instanceof StringFieldStrategy);
//    }
}
