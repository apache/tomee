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


import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;

/**
 * <p>Test embeddable superclasses.</p>
 *
 * @author Abe White
 */
public class TestEmbeddableSuperclass extends AnnotationTestCase
{

	public TestEmbeddableSuperclass(String name)
	{
		super(name, "annotationcactusapp");
	}

    public void setUp() {
        deleteAll(EmbeddableSuperSub.class);
    }

    /*public void testSuperclassEmbeddedOnly() {
        ClassMapping cls = ((JDBCConfiguration) getConfiguration()).
            getMappingRepositoryInstance().getMapping(EmbeddableSuper.class,
            null, true);
        assertTrue(cls.isEmbeddedOnly());
        assertEquals(NoneClassStrategy.getInstance(), cls.getStrategy());
    }

    public void testSubclassMappingDefaultsAndOverrides() {
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        ClassMapping cls = conf.getMappingRepositoryInstance().
            getMapping(EmbeddableSuperSub.class, null, true);
        assertTrue(!cls.isEmbeddedOnly());
        assertTrue(cls.getStrategy() instanceof FullClassStrategy);
        assertEquals(ClassMapping.ID_APPLICATION, cls.getIdentityType());
        assertTrue(cls.isOpenJPAIdentity());

        FieldMapping fm = cls.getFieldMapping("pk");
        assertTrue(fm.isPrimaryKey());
        assertEquals(ValueStrategies.SEQUENCE, fm.getValueStrategy());
        assertEquals("ID", fm.getColumns()[0].getName());

        assertNull(cls.getField("trans"));

        fm = cls.getFieldMapping("clob");
        assertEquals("CC", fm.getColumns()[0].getName());
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

    public void testSubclassDiscriminatorMapping() {
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        ClassMapping cls = conf.getMappingRepositoryInstance().
            getMapping(EmbeddableSuperSub.class, null, true);
        assertEquals("DISC", cls.getDiscriminator().getColumns()[0].
            getName());
    }

    public void testVersionOverrideMapping() {
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        ClassMapping cls = conf.getMappingRepositoryInstance().
            getMapping(EmbeddableSuperSub.class, null, true);
        assertEquals("VERSVAL", cls.getVersion().getColumns()[0].getName());
    }

    public void testRelationMappings() {
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        ClassMapping cls = conf.getMappingRepositoryInstance().
            getMapping(EmbeddableSuperSub.class, null, true);
        FieldMapping fm = cls.getFieldMapping("sub");
        assertTrue(fm.getStrategy() instanceof RelationFieldStrategy);

        fm = cls.getFieldMapping("sup");
        assertTrue(fm.getStrategy() instanceof RelationFieldStrategy);
    }
*/
    public void testPersistAndFind() {
        EmbeddableSuperSub parent = new EmbeddableSuperSub();
        parent.setClob("parent");
        EmbeddableSuperSub sub = new EmbeddableSuperSub();
        sub.setClob("sub");
        EmbeddableSuperSub sup = new EmbeddableSuperSub();
        sup.setClob("sup");
        parent.setSub(sub);
        parent.setSup(sup);

        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        em.persistAll(parent, sub, sup);
        endTx(em);
        long pk = parent.getPK();
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        parent = em.find(EmbeddableSuperSub.class, pk);
        assertEquals("parent", parent.getClob());
        assertEquals("sub", parent.getSub().getClob());
        assertEquals("sup", parent.getSup().getClob());
        endEm(em);
    }
}
