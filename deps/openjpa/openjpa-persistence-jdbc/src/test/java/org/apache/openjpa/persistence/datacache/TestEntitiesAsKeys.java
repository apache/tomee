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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestEntitiesAsKeys extends AbstractPersistenceTestCase {

    private OpenJPAEntityManagerFactorySPI emf;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        emf = createEMF(MapHolder.class, MapEmbeddable.class, "openjpa.DataCache", "true",
            "openjpa.RemoteCommitProvider", "sjvm", "openjpa.RuntimeUnenhancedClasses", "unsupported");
        populate();
    }

    @Override
    public void tearDown() throws Exception {
        closeEMF(emf);
        emf = null;
        super.tearDown();
    }
    
    public void populate() {
        EntityManager em = emf.createEntityManager();
        // clean up before execution
        em.getTransaction().begin();
        em.createQuery("Delete from MapHolder").executeUpdate();
        em.getTransaction().commit();

        em.getTransaction().begin();
        MapHolder mh = new MapHolder();
        mh.setId(10);
        mh.setEmbeddableMap(getEmbeddableMap(1, 2, 3, 4, 5, 6, 7, 8));
        em.persist(mh);
        em.getTransaction().commit();
        em.close();
    }

    public void testMapContents() {
        EntityManager em = emf.createEntityManager();

        MapHolder mh = em.find(MapHolder.class, 10);
        mh.getEmbeddableMap();
        assertNotNull(mh);

        for (Object o : mh.getEmbeddableMap().keySet()) {
            assertTrue("Expected key to be instanceof MapEmbeddable but was " + o.getClass().getCanonicalName(),
                o instanceof MapEmbeddable);
        }

        for (Object o : mh.getEmbeddableMap().values()) {
            assertTrue("Expected value to be instanceof MapEmbeddable but was " + o.getClass().getCanonicalName(),
                o instanceof MapEmbeddable);
        }
        em.close();
    }

    private Map<MapEmbeddable, MapEmbeddable> getEmbeddableMap(Integer... integers) {
        Map<MapEmbeddable, MapEmbeddable> rval = new HashMap<MapEmbeddable, MapEmbeddable>();
        assertEquals(0, integers.length % 2);

        for (int i = 0; i < integers.length; i += 2) {
            rval.put(new MapEmbeddable(integers[i]), new MapEmbeddable(integers[i + 1]));
        }

        return rval;
    }
}
