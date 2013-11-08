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
package org.apache.openjpa.jdbc.kernel;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestJDBCStoreOptSelect extends SQLListenerTestCase {
    Object[] props = new Object[] { CLEAR_TABLES, OptSelectEntity.class
        };
    OptSelectEntity e1, e2;

    @Override
    public void setUp() throws Exception {
        super.setUp(props);
        createData();
    }

    public void test() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        StoreManager store = ((EntityManagerImpl) em).getBroker().getStoreManager().getDelegate();

        FetchPlan fp = getFetchPlan(em);
        try {
            sql.clear();

            if (store instanceof JDBCStoreManager == false) {
                fail("StoreManager is not an instanceof JDBCStoreManager");
            }
            // Set this JDBCFetchPlan property so that we will select FKs for fields that are in the DFG, but not
            // included in the current load. If this property isn't set, the FK for eagerOneToOneOwner will not be
            // selected.
             ((JDBCFetchPlan)fp).setIgnoreDfgForFkSelect(true);
 
            // Remove all relationships
            fp.removeField(OptSelectEntity.class, "eagerOneToOne");
            fp.removeField(OptSelectEntity.class, "eagerOneToOneOwner");
            fp.removeField(OptSelectEntity.class, "lazyOneToOne");
            fp.removeField(OptSelectEntity.class, "lazyOneToOneOwner");

            OptSelectEntity ee1 = em.find(OptSelectEntity.class, e1.getId());

            // Make sure our sql has no joins
            assertEquals(1, sql.size());
            String s = sql.get(0);
            assertFalse(s.contains("JOIN") && s.contains("join"));

            // Check to see how many fks(intermediate fields) we selected.
            StateManagerImpl smi = ((StateManagerImpl) ((PersistenceCapable) ee1).pcGetStateManager());
            ClassMetaData cmd =
                em.getConfiguration().getMetaDataRepositoryInstance().getMetaData(OptSelectEntity.class, null, true);
            int fks = 0;
            for (FieldMetaData fmd : cmd.getFields()) {
                if (smi.getIntermediate(fmd.getIndex()) != null) {
                    fks++;
                }
            }
            // We expected to find 2 FKs. One for each of the owners (lazyOneToOneOwner and eagerOneToOneOwner)
            assertEquals(2, fks);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    private FetchPlan getFetchPlan(OpenJPAEntityManagerSPI em) {
        MetaDataRepository mdr = em.getConfiguration().getMetaDataRepositoryInstance();
        FetchPlan fp = em.pushFetchPlan();
        fp.removeFetchGroups(fp.getFetchGroups());
        for (Class<?> cls : new Class<?>[] { OptSelectEntity.class }) {
            ClassMetaData cmd = mdr.getMetaData(cls, null, true);
            for (FieldMetaData fmd : cmd.getFields()) {
                fp.addField(cls, fmd.getName());
            }
        }
        return fp;
    }

    void createData() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            e1 = new OptSelectEntity();
            e2 = new OptSelectEntity();

            e1.setEagerOneToOne(e2);
            e2.setEagerOneToOneOwner(e2);

            e1.setLazyOneToOne(e2);
            e2.setLazyOneToOneOwner(e1);

            em.persistAll(e1, e2);

            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (em.isOpen())
                em.close();
        }
    }
}
