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
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.meta.FetchGroup;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.UpdateStrategies;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <p>Test the parsing of Kodo metadata extension annotations.</p>
 *
 * @author Abe White
 */
public class TestExtensionAnnotations
    extends SingleEMFTestCase {

    private ClassMapping _mapping;

    public void setUp() {
        setUp(ExtensionsEntity.class);
        _mapping = ((JDBCConfiguration) emf.getConfiguration()).
            getMappingRepositoryInstance().getMapping(ExtensionsEntity.class,
            null, true);
    }

    public void testDataStoreId() {
        assertEquals(ClassMapping.ID_DATASTORE, _mapping.getIdentityType());
        assertEquals(ValueStrategies.SEQUENCE, _mapping.getIdentityStrategy());
        assertEquals("system", _mapping.getIdentitySequenceName());
    }

    public void testDataCache() {
        assertNull(_mapping.getDataCacheName());
    }

    public void testFetchGroups() {
        FetchGroup fg = _mapping.getFetchGroup("detail");
        assertNotNull(fg);
        assertFalse(fg.isPostLoad());
        FieldMapping fm = _mapping.getFieldMapping("rel");
        assertEquals(1, fm.getCustomFetchGroups().length);
        assertEquals("detail", fm.getCustomFetchGroups()[0]);
        assertEquals(-1, fg.getRecursionDepth(fm));
        fm = _mapping.getFieldMapping("seq");
        assertEquals(1, fm.getCustomFetchGroups().length);
        assertEquals("detail", fm.getCustomFetchGroups()[0]);
        assertEquals(1, fg.getRecursionDepth(fm));
    }

    public void testValueStrategy() {
        assertEquals(ValueStrategies.UUID_HEX,
            _mapping.getField("uuid").getValueStrategy());
        assertEquals(ValueStrategies.UUID_STRING,
            _mapping.getField("uuidString").getValueStrategy());
        assertEquals(ValueStrategies.UUID_TYPE4_HEX,
            _mapping.getField("uuidT4Hex").getValueStrategy());
        assertEquals(ValueStrategies.UUID_TYPE4_STRING,
            _mapping.getField("uuidT4String").getValueStrategy());
        FieldMapping seq = _mapping.getFieldMapping("seq");
        assertEquals(ValueStrategies.SEQUENCE, seq.getValueStrategy());
        assertEquals("system", seq.getValueSequenceName());
    }

    public void testReadOnly() {
        assertEquals(UpdateStrategies.RESTRICT,
            _mapping.getField("seq").getUpdateStrategy());
    }

    public void testInverseLogical() {
        assertEquals("owner", _mapping.getField("rel").getInverse());
        assertNull(_mapping.getField("owner").getInverse());
    }

    public void testDependent() {
        assertEquals(ValueMetaData.CASCADE_AUTO,
            _mapping.getField("rel").getCascadeDelete());
        assertEquals(ValueMetaData.CASCADE_NONE,
            _mapping.getField("eager").getCascadeDelete());
        assertEquals(ValueMetaData.CASCADE_AUTO,
            _mapping.getField("eager").getElement().getCascadeDelete());
    }

    public void testLRS() {
        assertTrue(_mapping.getField("lrs").isLRS());
        assertFalse(_mapping.getField("eager").isLRS());
    }

    public void testClassCriteria() {
        assertTrue(_mapping.getFieldMapping("eager").getElementMapping().
            getUseClassCriteria());
        assertFalse(_mapping.getFieldMapping("eager").getUseClassCriteria());
        assertFalse(_mapping.getFieldMapping("lrs").getElementMapping().
            getUseClassCriteria());
    }

    public void testExternalValues() {
        FieldMapping externalValues = _mapping.getFieldMapping
            ("externalValues");
        assertEquals(JavaTypes.CHAR, externalValues.getDeclaredTypeCode());
        assertEquals(JavaTypes.INT, externalValues.getTypeCode());
        assertEquals(new Integer(1), externalValues.getExternalValueMap().
            get(new Character('M')));
        assertEquals(new Integer(2), externalValues.getExternalValueMap().
            get(new Character('F')));
    }

    public void testExternalizer() {
        FieldMapping externalizer = _mapping.getFieldMapping("externalizer");
        assertEquals(JavaTypes.OBJECT, externalizer.getDeclaredTypeCode());
        assertEquals(JavaTypes.STRING, externalizer.getTypeCode());
        assertEquals("java.lang.String", externalizer.getExternalValue
            (String.class, null));
        assertEquals(String.class, externalizer.getFieldValue
            (String.class.getName(), null));
    }
}
