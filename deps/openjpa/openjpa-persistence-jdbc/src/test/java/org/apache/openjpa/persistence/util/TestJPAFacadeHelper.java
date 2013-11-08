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
package org.apache.openjpa.persistence.util;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.EmbeddedIdClass;
import org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.EmbeddedIdEntity;
import org.apache.openjpa.persistence.derivedid.EBigDecimalID;
import org.apache.openjpa.persistence.derivedid.EBigIntegerID;
import org.apache.openjpa.persistence.derivedid.EDBigDecimalID;
import org.apache.openjpa.persistence.derivedid.EDBigIntegerID;
import org.apache.openjpa.persistence.derivedid.EDDateID;
import org.apache.openjpa.persistence.derivedid.EDSQLDateID;
import org.apache.openjpa.persistence.derivedid.EDateID;
import org.apache.openjpa.persistence.derivedid.ESQLDateID;
import org.apache.openjpa.persistence.jdbc.common.apps.mappingApp.CompositeId;
import org.apache.openjpa.persistence.jdbc.common.apps.mappingApp.EntityWithCompositeId;
import org.apache.openjpa.persistence.relations.BasicEntity;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.util.Id;
import org.apache.openjpa.util.LongId;
import org.apache.openjpa.util.ObjectId;
import org.apache.openjpa.util.UserException;

public class TestJPAFacadeHelper extends SingleEMFTestCase {
    MetaDataRepository repo = null;

    public void setUp() {
        setUp(EmbeddedIdEntity.class, EmbeddedIdClass.class, EBigDecimalID.class, EDBigDecimalID.class,
            EBigIntegerID.class, EDBigIntegerID.class, EDateID.class, EDDateID.class, ESQLDateID.class,
            EDSQLDateID.class, EntityWithCompositeId.class, AllFieldTypes.class, BasicEntity.class);

        repo = emf.getConfiguration().getMetaDataRepositoryInstance();
    }

    public void testEmbeddedId() throws Exception {
        ClassMetaData cmd = repo.getMetaData(EmbeddedIdEntity.class, null, true);
        try {
            JPAFacadeHelper.toOpenJPAObjectId(cmd, new EmbeddedIdEntity());
            fail("Didn't fail!");
        } catch (UserException re) {
            // expected

        }
        EmbeddedIdClass id = new EmbeddedIdClass();
        assertEquals(ObjectId.class, JPAFacadeHelper.toOpenJPAObjectId(cmd, id).getClass());
    }

    public void testCompositeId() throws Exception {
        ClassMetaData cmd = repo.getMetaData(EntityWithCompositeId.class, null, true);
        try {
            JPAFacadeHelper.toOpenJPAObjectId(cmd, new EntityWithCompositeId());
            fail("Didn't fail!");
        } catch (UserException re) {
            // expected

        }
        CompositeId id = new CompositeId(12, "name");
        assertEquals(ObjectId.class, JPAFacadeHelper.toOpenJPAObjectId(cmd, id).getClass());
    }

    public void testBasic() throws Exception {
        ClassMetaData cmd = repo.getMetaData(BasicEntity.class, null, true);
        try {
            JPAFacadeHelper.toOpenJPAObjectId(cmd, new BasicEntity());
            fail("Didn't fail!");
        } catch (UserException re) {
            // expected
        }
        try {
            JPAFacadeHelper.toOpenJPAObjectId(cmd, "a");
            fail("Didn't fail!");
        } catch (UserException re) {
            // expected

        }
        assertEquals(LongId.class, JPAFacadeHelper.toOpenJPAObjectId(cmd, Long.valueOf(1)).getClass());
        Object o = JPAFacadeHelper.toOpenJPAObjectId(cmd, Long.valueOf(1));
        assertEquals(o, JPAFacadeHelper.toOpenJPAObjectId(cmd, o));
    }

    public void testDerivedId() throws Exception {
        ClassMetaData cmd = repo.getMetaData(EDSQLDateID.class, null, true);
        try {
            JPAFacadeHelper.toOpenJPAObjectId(cmd, new EDSQLDateID());
            fail("Didn't fail!");
        } catch (UserException re) {
            // expected

        }
        ESQLDateID id = new ESQLDateID();
        assertEquals(ObjectId.class, JPAFacadeHelper.toOpenJPAObjectId(cmd, id).getClass());
    }

    public void testNoId() throws Exception {
        ClassMetaData cmd = repo.getMetaData(AllFieldTypes.class, null, true);
        try {
            // Don't parameterize this collection to force the JVM to use the 
            // ...(ClassMetaData meta, Collection<Object> oids) method sig.
            Collection ids = new ArrayList<AllFieldTypes>();
            ids.add(new AllFieldTypes());
            JPAFacadeHelper.toOpenJPAObjectIds(cmd, ids);
            fail("Didn't fail!");
        } catch (UserException re) {
            // expected

        }
        try {
            JPAFacadeHelper.toOpenJPAObjectId(cmd, "a");
            fail("Didn't fail!");
        } catch (UserException re) {
            // expected

        }
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        em.getTransaction().begin();
        AllFieldTypes type = new AllFieldTypes();
        em.persist(type);
        em.getTransaction().commit();
        Object oid = em.getObjectId(type);
        assertEquals(Id.class, JPAFacadeHelper.toOpenJPAObjectId(cmd, oid).getClass());
    }
}
