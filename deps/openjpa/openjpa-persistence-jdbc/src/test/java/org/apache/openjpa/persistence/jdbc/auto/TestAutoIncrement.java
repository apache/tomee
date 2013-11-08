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
package org.apache.openjpa.persistence.jdbc.auto;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.jdbc.sql.SQLServerDictionary;
import org.apache.openjpa.jdbc.sql.SybaseDictionary;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestAutoIncrement extends SingleEMTestCase {
    boolean disabled = false;
    public void setUp() {
        super.setUp(DROP_TABLES, AutoIncrementEntity.class);
        DBDictionary dic = ((JDBCConfiguration)emf.getConfiguration()).getDBDictionaryInstance();
        if (!dic.supportsAutoAssign) {
            disabled = true;
            return;
        }
        if (dic instanceof SQLServerDictionary || dic instanceof OracleDictionary || dic instanceof SybaseDictionary) {
            disabled = true;
            return;
        }
        createZeroIdEntity();
    }

    public void test() {
        if (disabled) {
            return;
        }
        em.getTransaction().begin();
        AutoIncrementEntity e1 = em.find(AutoIncrementEntity.class, 0);
        assertNotNull(e1);
        AutoIncrementEntity e2 = new AutoIncrementEntity();
        assertEquals(null, e2.getId());
        em.persist(e2);
        em.getTransaction().commit();
        assertNotEquals(null, e2.getId());
    }
    
    public void testMergeNewEntity() {
        if (disabled) {
            return;
        }
        AutoIncrementEntity e2 = new AutoIncrementEntity();
        
        em.getTransaction().begin();
        AutoIncrementEntity e3 = em.merge(e2);
        em.getTransaction().commit();
        
        assertNotNull(e3);
        
    }
    
    /**
     * A private worker method that will synthesize an Entity which has an auto generated id that starts at zero.
     */
    private void createZeroIdEntity() {
        em.getTransaction().begin();
        AutoIncrementEntity aie = new AutoIncrementEntity();
        em.persist(aie);
        em.flush();
        // If the created Entity has a non-zero id, update the Entity to have a zero id.
        if (aie.getId() != 0) {
            em.createQuery("UPDATE AutoIncrementEntity a SET a.id = 0 WHERE a.id = :id")
                .setParameter("id", aie.getId()).executeUpdate();
        }
        em.getTransaction().commit();
    }
}
