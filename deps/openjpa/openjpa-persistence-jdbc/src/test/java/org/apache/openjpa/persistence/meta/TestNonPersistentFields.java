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


import org.apache.openjpa.persistence.meta.common.apps.NonPersistentFieldsPC;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;

/**
 * <p>Tests that fields that should not be persistent actually aren't.</p>
 *
 * @author Abe White
 */
public class TestNonPersistentFields
    extends AbstractTestCase {

    private ClassMetaData _meta = null;

    public TestNonPersistentFields(String test) {
        super(test, "metacactusapp");
    }

    public void setUp() {
        MetaDataRepository repos = new OpenJPAConfigurationImpl().
            newMetaDataRepositoryInstance();
        _meta = repos.getMetaData(NonPersistentFieldsPC.class, null, true);
    }

    public void testNonPersistentFields() {
        FieldMetaData[] fmds = _meta.getFields();
        assertEquals(5, fmds.length);
        assertEquals("persistentField", fmds[0].getName());
        assertEquals("persistentInterfaceField", fmds[1].getName());
        assertEquals("persistentObjectField", fmds[2].getName());
        assertEquals("persistentUserInterfaceField", fmds[3].getName());
        assertEquals("persistentUserObjectField", fmds[4].getName());
    }
}
