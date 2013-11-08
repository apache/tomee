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

import org.apache.openjpa.meta.*;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.JPAFacadeHelper;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;

public class TestAnnotationBasics extends AnnotationTestCase {

	public TestAnnotationBasics(String name)
	{
		super(name, "annotationcactusapp");
	}

    public void testSingleFieldIdentity()
    {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        ClassMetaData meta = JPAFacadeHelper.getMetaData(em, Entity1.class);
        assertTrue("Entity1 should use application identity",
            ClassMetaData.ID_APPLICATION == meta.getIdentityType());
        assertTrue("Entity1 should use single-field identity",
            meta.isOpenJPAIdentity());
        endEm(em);
    }
/*
    public void testVersionField() {
        ClassMapping mapping = (ClassMapping) getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(Entity1.class,
            null, true);
        FieldMapping fm = mapping.getFieldMapping("versionField");
        assertTrue(fm.isVersion());
        String col = mapping.getVersion().getColumns()[0].getName();
        assertTrue(col, "VERSIONFIELD".equalsIgnoreCase(col));
    }
*/}
