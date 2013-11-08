/*
 * Copyright 2013 Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openjpa.persistence.jdbc.strategy;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.jdbc.strategy.MappedEntity.Key;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 */
public class TestHandlerStrategy extends SingleEMFTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp(MappedEntity.class, MapperEntity.class, "openjpa.jdbc.MappingDefaults",
            "ForeignKeyDeleteAction=restrict, JoinForeignKeyDeleteAction=restrict", CLEAR_TABLES);
    }

    /**
     * @see JIRA ticket OPENJPA-2328 for more explanation
     */
    public void testIssue_OPENJPA2328() {
        // Not all databases support GenerationType.IDENTITY column(s)
        if (!((JDBCConfiguration) emf.getConfiguration()).getDBDictionaryInstance().supportsAutoAssign) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        MapperEntity ae = new MapperEntity();
        for (Key key : Key.values()) {
            ae.get(key).setValue(System.nanoTime());
        }

        // First step : persist some data into database
        em.getTransaction().begin();
        em.persist(ae);
        em.getTransaction().commit();

        // Second step : update & remove some data from collection
        em.getTransaction().begin();
        ae.get(Key.A).setValue(10L); // Required
        ae.remove(Key.B); // Required*
        em.getTransaction().commit();
    }
}
