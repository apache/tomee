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
package org.apache.openjpa.persistence.xml;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestToOneLazyXmlOverride extends SQLListenerTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp(CLEAR_TABLES, XmlOverrideToOneEntity.class);
    }

    @Override
    protected String getPersistenceUnitName() {
        return "to-one-xml-override";
    }

    public void testToManyLazyOverride() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            XmlOverrideToOneEntity x = new XmlOverrideToOneEntity();
            x.setOtherM2O(x);
            x.setOtherO2O(x);
            em.persist(x);
            em.getTransaction().commit();

            em.clear();
            resetSQL();

            em.find(XmlOverrideToOneEntity.class, x.getId());

            assertTrue(sql.size() == 1);
            String lastSql = sql.get(0);
            // Make sure we don't have any joins!
            assertFalse("Shouldn't have found any instances of join or JOIN in last sql, but did. Last SQL = "
                + lastSql, lastSql.contains("join") || lastSql.contains("JOIN"));

            // Make sure that we selected lazy join columns.
            assertTrue(lastSql.contains("o2o"));
            assertTrue(lastSql.contains("m2o"));

        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
}
