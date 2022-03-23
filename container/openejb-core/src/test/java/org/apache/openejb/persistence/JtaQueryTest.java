/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.persistence;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.openjpa.persistence.QueryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class JtaQueryTest {
    @Module
    public Persistence unit() {
        final Persistence persistence = new Persistence();
        {
            final PersistenceUnit persistenceUnit = new PersistenceUnit();
            persistenceUnit.setName("testWrapped");
            persistenceUnit.setExcludeUnlistedClasses(true);
            persistence.getPersistenceUnit().add(persistenceUnit);
        }
        {
            final PersistenceUnit persistenceUnit = new PersistenceUnit();
            persistenceUnit.setName("testNotWrapped");
            persistenceUnit.setExcludeUnlistedClasses(true);
            persistenceUnit.setProperty("openejb.jpa.query.wrap-no-tx", "false");
            persistence.getPersistenceUnit().add(persistenceUnit);
        }
        return persistence;
    }

    @PersistenceContext(unitName = "testWrapped")
    private EntityManager wrapped;

    @PersistenceContext(unitName = "testNotWrapped")
    private EntityManager notWrapped;

    @Test
    public void jtaUnwrap() {
        for (int i = 0; i < 2; i++) { // no exception already closed
            final Query query = wrapped.createNativeQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
            assertTrue(query instanceof JtaQuery);
            final JtaQuery q = query.unwrap(JtaQuery.class);
            assertNotNull(Reflections.get(q, "entityManager"));
            query.getResultList();
            assertFalse(EntityManager.class.cast(Reflections.get(q, "entityManager")).isOpen());
        }
    }

    @Test
    public void raw() {
        for (int i = 0; i < 2; i++) { // no exception already closed
            final Query query = notWrapped.createNativeQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
            assertTrue(query instanceof QueryImpl);
            query.getResultList();
            // actually true, that is why we don't have it by default: it leaks
            // assertFalse(EntityManager.class.cast(Reflections.get(query, "_em")).isOpen());
        }
    }
}
