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
package org.apache.openjpa.persistence.datacache;

import java.util.Arrays;
import javax.persistence.EntityManager;

import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.kernel.PCData;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.StoreCacheImpl;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestArrayFieldsInDataCache
    extends SingleEMFTestCase {

    private static final String[] STRINGS = new String[]{ "a", "b", "c" };
    private static final int[] INTS = new int[]{ 1, 2, 3 };

    private Object jpaOid;
    private Object internalOid;

    public void setUp() {
        setUp("openjpa.DataCache", "true", 
            "openjpa.RemoteCommitProvider", "sjvm", 
            AllFieldTypes.class);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AllFieldTypes aft = new AllFieldTypes();
        aft.setArrayOfStrings(STRINGS);
        aft.setArrayOfInts(INTS);
        em.persist(aft);
        em.getTransaction().commit();

        // get the external and internal forms of the ID for cache
        // interrogation and data validation
        jpaOid = OpenJPAPersistence.cast(em).getObjectId(aft);
        internalOid = JPAFacadeHelper.toBroker(em).getObjectId(aft);

        em.close();
    }

    public void testArrayOfStrings() {
        // check that the data cache contains an efficient representation
        StoreCacheImpl storeCache = (StoreCacheImpl)
            OpenJPAPersistence.cast(emf).getStoreCache();
        DataCache cache = storeCache.getDelegate();
        PCData data = cache.get(internalOid);
        ClassMetaData meta = JPAFacadeHelper.getMetaData(emf,
            AllFieldTypes.class);
        Object cachedFieldData =
            data.getData(meta.getField("arrayOfStrings").getIndex());
        assertTrue(cachedFieldData.getClass().isArray());
        assertEquals(String.class,
            cachedFieldData.getClass().getComponentType());

        // make sure that the returned results are correct
        EntityManager em = emf.createEntityManager();
        AllFieldTypes aft = em.find(AllFieldTypes.class, jpaOid);
        assertTrue(Arrays.equals(STRINGS, aft.getArrayOfStrings()));
        assertNotSame(STRINGS, aft.getArrayOfStrings());
        em.close();
    }

    public void testArrayOfInts() {
        // check that the data cache contains an efficient representation
        StoreCacheImpl storeCache = (StoreCacheImpl)
            OpenJPAPersistence.cast(emf).getStoreCache();
        DataCache cache = storeCache.getDelegate();
        PCData data = cache.get(internalOid);
        ClassMetaData meta = JPAFacadeHelper.getMetaData(emf,
            AllFieldTypes.class);
        Object cachedFieldData =
            data.getData(meta.getField("arrayOfInts").getIndex());
        assertTrue(cachedFieldData.getClass().isArray());
        assertEquals(int.class, cachedFieldData.getClass().getComponentType());

        // make sure that the returned results are correct
        EntityManager em = emf.createEntityManager();
        AllFieldTypes aft = em.find(AllFieldTypes.class, jpaOid);
        assertTrue(Arrays.equals(INTS, aft.getArrayOfInts()));
        assertNotSame(INTS, aft.getArrayOfInts());
        em.close();
    }
}
