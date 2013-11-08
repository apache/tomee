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
package org.apache.openjpa.persistence.meta;

import java.util.Map;
import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.meta.common.apps.MetaTest1;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest2;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest3;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest5;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest6;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.JPAFacadeHelper;

/**
 * <p>Tests the {@link ClassMetaData} type, and in so doing tests parts of
 * the {@link MetaDataRepository} and {@link FieldMetaData} types.</p>
 *
 * @author Abe White
 */
public class TestClassMetaData
    extends AbstractTestCase {

    private MetaDataRepository _repos = null;

    private ClassMetaData _metaTest1 = null;
    private ClassMetaData _metaTest2 = null;
    private ClassMetaData _metaTest3 = null;
    private ClassMetaData _metaTest5 = null;
    private ClassMetaData _metaTest6 = null;

    public TestClassMetaData(String test) {
        super(test, "metacactusapp");
    }

    public void setUp()
        throws Exception {
        _repos = getRepository();
        _metaTest5 = _repos.getMetaData(MetaTest5.class, null, true);
        _metaTest3 = _repos.getMetaData(MetaTest3.class, null, true);
        _metaTest2 = _repos.getMetaData(MetaTest2.class, null, true);
        _metaTest1 = _repos.getMetaData(MetaTest1.class, null, true);
        _metaTest6 = _repos.getMetaData(MetaTest6.class, null, true);
    }

    protected MetaDataRepository getRepository()
        throws Exception {
        //return new OpenJPAConfigurationImpl().newMetaDataRepositoryInstance();
        //return getConfiguration().newMetaDataRepositoryInstance();
        EntityManager em = currentEntityManager();
        Broker broker = JPAFacadeHelper.toBroker(em);
        return broker.getConfiguration().newMetaDataRepositoryInstance();
    }

    /**
     * Test the class-level defaults.
     */
    public void testClassDefaults() {
        assertEquals(MetaTest1.class.getName(),
            _metaTest1.getDescribedType().getName());
        assertNull(_metaTest1.getPCSuperclass());
        assertEquals(ClassMetaData.ID_DATASTORE,
            _metaTest1.getIdentityType());
        assertTrue(_metaTest1.getRequiresExtent());
    }

    public void testGetProxyFields(){
        FieldMetaData[] proxies = _metaTest3.getProxyFields();
        assertEquals(2, proxies.length);
        
        proxies = _metaTest1.getProxyFields();
        assertEquals(2, proxies.length);
        
        proxies = _metaTest5.getProxyFields();
        assertEquals(0, proxies.length);

    }
    /**
     * Test non-persistent fields.
     */
    public void testDefaultNonPersistentFields() {
        assertNull(_metaTest1.getField("staticField"));
        assertNull(_metaTest1.getField("finalfield"));
        assertNull(_metaTest1.getField("transientfield"));
        assertNull(_metaTest1.getField("metaTest4Field"));
        assertNull(_metaTest1.getField("metaTest4ArrayField"));
        assertNull(_metaTest1.getField("objectField"));
        assertNull(_metaTest1.getField("longWrapperField"));

        FieldMetaData fmd = _metaTest1.getField("doubleField");
        assertEquals(FieldMetaData.MANAGE_TRANSACTIONAL, fmd.getManagement());
    }

    /**
     * Test basics on persistent fields.
     */
    public void testBasicFields() {
        FieldMetaData fmd;
        fmd = _metaTest1.getField("stringField");
        assertEquals(JavaTypes.STRING, fmd.getTypeCode());
        assertEquals(JavaTypes.STRING, fmd.getDeclaredTypeCode());
        assertNull(fmd.getTypeMetaData());
        assertNull(fmd.getDeclaredTypeMetaData());
        fmd = _metaTest1.getField("intWrapperField");
        assertEquals(JavaTypes.INT_OBJ, fmd.getTypeCode());
        assertEquals(JavaTypes.INT_OBJ, fmd.getDeclaredTypeCode());
        fmd = _metaTest1.getField("intField");
        assertEquals(JavaTypes.INT, fmd.getTypeCode());
        assertEquals(JavaTypes.INT, fmd.getDeclaredTypeCode());
        fmd = _metaTest1.getField("metaTest2Field");
        assertEquals(JavaTypes.PC, fmd.getTypeCode());
        assertEquals(JavaTypes.PC, fmd.getDeclaredTypeCode());
        assertEquals(_metaTest2, fmd.getTypeMetaData());
        assertEquals(_metaTest2, fmd.getDeclaredTypeMetaData());
        fmd = _metaTest1.getField("metaTest2ArrayField");
        assertEquals(JavaTypes.ARRAY, fmd.getTypeCode());
        assertEquals(JavaTypes.PC, fmd.getElement().getTypeCode());
        assertEquals(JavaTypes.ARRAY, fmd.getDeclaredTypeCode());
        assertEquals(JavaTypes.PC,
            fmd.getElement().getDeclaredTypeCode());
        fmd = _metaTest1.getField("intArrayField");
        assertEquals(JavaTypes.ARRAY, fmd.getTypeCode());
        assertEquals(JavaTypes.INT, fmd.getElement().getTypeCode());
        assertEquals(JavaTypes.ARRAY, fmd.getDeclaredTypeCode());
        assertEquals(JavaTypes.INT,
            fmd.getElement().getDeclaredTypeCode());
        fmd = _metaTest1.getField("intField");
        assertEquals(FieldMetaData.NULL_EXCEPTION, fmd.getNullValue());
        assertTrue(!fmd.isInDefaultFetchGroup());
        assertTrue(!fmd.isEmbedded());
        fmd = _metaTest1.getField("stringField");
        assertEquals(FieldMetaData.NULL_UNSET, fmd.getNullValue());
        assertTrue(fmd.isInDefaultFetchGroup());
        assertTrue(fmd.isEmbedded());
    }

    /**
     * Test collection and map fields.
     */
    public void testCollectionFields() {
        FieldMetaData fmd;
        fmd = _metaTest2.getField("collectionField1");
        assertEquals(JavaTypes.COLLECTION, fmd.getTypeCode());
        assertEquals(JavaTypes.OBJECT, fmd.getElement().getTypeCode());
        assertEquals(Object.class, fmd.getElement().getType());
        assertNull(fmd.getElement().getTypeMetaData());
        assertEquals(JavaTypes.COLLECTION,
            fmd.getDeclaredTypeCode());
        assertEquals(JavaTypes.OBJECT,
            fmd.getElement().getDeclaredTypeCode());
        assertEquals(Object.class, fmd.getElement().getDeclaredType());
        assertNull(fmd.getElement().getTypeMetaData());
        assertTrue(fmd.getElement().isEmbedded());
        fmd = _metaTest2.getField("collectionField2");
        assertEquals(JavaTypes.COLLECTION, fmd.getTypeCode());
        assertEquals(JavaTypes.PC, fmd.getElement().getTypeCode());
        assertEquals(MetaTest3.class, fmd.getElement().getType());
        assertEquals(_metaTest3, fmd.getElement().getTypeMetaData());
        assertEquals(JavaTypes.COLLECTION,
            fmd.getDeclaredTypeCode());
        assertEquals(JavaTypes.PC,
            fmd.getElement().getDeclaredTypeCode());
        assertEquals(MetaTest3.class, fmd.getElement().getDeclaredType());
        assertEquals(_metaTest3, fmd.getElement().getDeclaredTypeMetaData());
        assertTrue(!fmd.getElement().isEmbedded());

        fmd = _metaTest2.getField("mapField1");
        assertEquals(JavaTypes.MAP, fmd.getTypeCode());
        assertEquals(JavaTypes.OBJECT, fmd.getKey().getTypeCode());
        assertEquals(JavaTypes.OBJECT, fmd.getElement().getTypeCode());
        assertEquals(Object.class, fmd.getKey().getType());
        assertNull(fmd.getKey().getTypeMetaData());
        assertEquals(Object.class, fmd.getElement().getType());
        assertEquals(JavaTypes.MAP, fmd.getDeclaredTypeCode());
        assertEquals(JavaTypes.OBJECT, fmd.getKey().getDeclaredTypeCode());
        assertEquals(JavaTypes.OBJECT,
            fmd.getElement().getDeclaredTypeCode());
        assertEquals(Object.class, fmd.getKey().getDeclaredType());
        assertNull(fmd.getKey().getDeclaredTypeMetaData());
        assertEquals(Object.class, fmd.getElement().getDeclaredType());
        assertTrue(fmd.getKey().isEmbedded());
        assertTrue(fmd.getElement().isEmbedded());
        fmd = _metaTest2.getField("mapField2");
        assertEquals(JavaTypes.MAP, fmd.getTypeCode());
        assertEquals(JavaTypes.STRING, fmd.getKey().getTypeCode());
        assertEquals(JavaTypes.INT_OBJ, fmd.getElement().getTypeCode());
        assertEquals(String.class, fmd.getKey().getType());
        assertEquals(Integer.class, fmd.getElement().getType());
        assertEquals(JavaTypes.MAP, fmd.getDeclaredTypeCode());
        assertEquals(JavaTypes.STRING, fmd.getKey().getDeclaredTypeCode());
        assertEquals(JavaTypes.INT_OBJ,
            fmd.getElement().getDeclaredTypeCode());
        assertEquals(String.class, fmd.getKey().getDeclaredType());
        assertEquals(Integer.class, fmd.getElement().getDeclaredType());
        assertTrue(fmd.getKey().isEmbedded());
        assertTrue(!fmd.getElement().isEmbedded());
    }

    /**
     * Test the basic class-level meta data.
     */
    public void testBasicClass() {
        assertEquals(_metaTest1, _metaTest2.getPCSuperclassMetaData());
        assertTrue(!_metaTest2.getRequiresExtent());
    }

    /**
     * Test application identity.
     */
    public void testApplicationIdentity() {
        assertEquals(ClassMetaData.ID_APPLICATION,
            _metaTest5.getIdentityType());
        assertEquals(MetaTest5.MetaTest5Id.class.getName(),
            _metaTest5.getObjectIdType().getName());
        assertEquals(ClassMetaData.ID_APPLICATION,
            _metaTest6.getIdentityType());
        assertEquals(MetaTest5.MetaTest5Id.class.getName(),
            _metaTest6.getObjectIdType().getName());
    }

    /**
     * Test absolute field numbering.
     */
    public void testAbsoluteFieldNumbering() {
        assertEquals(0, _metaTest1.getField("doubleField").getIndex());
        assertEquals(1, _metaTest1.getField("intArrayField").getIndex());
        assertEquals(2, _metaTest1.getField("intField").getIndex());
        assertEquals(3, _metaTest1.getField("intWrapperField").getIndex());
        assertEquals(4, _metaTest1.getField("metaTest2ArrayField").
            getIndex());
        assertEquals(5, _metaTest1.getField("metaTest2Field").getIndex());
        assertEquals(6, _metaTest1.getField("stringField").getIndex());
        assertEquals(7, _metaTest2.getField("collectionField1").getIndex());
        assertEquals(8, _metaTest2.getField("collectionField2").getIndex());
        assertEquals(9, _metaTest2.getField("mapField1").getIndex());
        assertEquals(10, _metaTest2.getField("mapField2").getIndex());
    }

    /**
     * Test the methods to get fields.
     */
    public void testGetFields() {
        FieldMetaData[] fmds = _metaTest2.getFields();
        assertEquals("doubleField", fmds[0].getName());
        assertEquals("intField", fmds[2].getName());
        assertEquals("collectionField2", fmds[8].getName());
    }

    /**
     * Test that metadata on inner classes is available.
     */
    public void testStaticInnerClasses() {
        assertNotNull(_repos.getMetaData(MetaTest1.Inner.class, null, true));
    }

    /**
     * Test extensions for external values and declared vs external types.
     */
    public void testExternalTypes() {
        // note that below, declared type code is promoted
        FieldMetaData fmd = _metaTest3.getField("pcField");
        assertEquals(JavaTypes.PC_UNTYPED, fmd.getTypeCode());
        assertEquals(JavaTypes.PC_UNTYPED, fmd.getDeclaredTypeCode());
        assertEquals(PersistenceCapable.class, fmd.getType());
        assertEquals(Object.class, fmd.getDeclaredType());
        assertNull(fmd.getDeclaredTypeMetaData());
        assertNull(fmd.getTypeMetaData());

        // note that below, declared type code is promoted
        fmd = _metaTest3.getField("metaField");
        assertEquals(JavaTypes.PC, fmd.getTypeCode());
        assertEquals(JavaTypes.PC, fmd.getDeclaredTypeCode());
        assertEquals(MetaTest2.class, fmd.getType());
        assertEquals(Object.class, fmd.getDeclaredType());
        assertEquals(_metaTest2, fmd.getDeclaredTypeMetaData());
        assertEquals(_metaTest2, fmd.getTypeMetaData());

        fmd = _metaTest3.getField("externalField");
        assertTrue(fmd.isExternalized());
        assertEquals(JavaTypes.MAP, fmd.getTypeCode());
        assertEquals(JavaTypes.OBJECT, fmd.getDeclaredTypeCode());
        assertEquals(Map.class, fmd.getType());
        assertEquals(Object.class, fmd.getDeclaredType());
        assertEquals(JavaTypes.STRING, fmd.getKey().getTypeCode());
        assertEquals(JavaTypes.OBJECT, fmd.getKey().getDeclaredTypeCode());
        assertEquals(String.class, fmd.getKey().getType());
        assertEquals(Object.class, fmd.getKey().getDeclaredType());
        assertEquals(JavaTypes.PC, fmd.getElement().getTypeCode());
        assertEquals(JavaTypes.OBJECT,
            fmd.getElement().getDeclaredTypeCode());
        assertEquals(MetaTest2.class, fmd.getElement().getType());
        assertEquals(Object.class, fmd.getElement().getDeclaredType());
        assertEquals(_metaTest2, fmd.getElement().getTypeMetaData());
        assertNull(fmd.getElement().getDeclaredTypeMetaData());
    }
}
