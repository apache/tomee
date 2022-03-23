/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openejb.configuration;

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.api.configuration.PersistenceUnitDefinitions;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.persistence.JtaEntityManager;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SimpleLog
@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class PersistenceUnitDefinitionTest {
    @PersistenceContext(unitName = "jpa")
    private EntityManager em1;

    @PersistenceContext(unitName = "jpa2")
    private EntityManager em2;

    @Test
    public void run() {
        assertNotNull(em1);
        assertNotNull(em2);
        assertTrue(JtaEntityManager.class.isInstance(em1));
    }

    @PersistenceUnitDefinitions({
        @PersistenceUnitDefinition,
        @PersistenceUnitDefinition(unitName = "jpa2", jta = false)
    })
    public static class MyConfig {
    }

    @Entity
    public static class AnEntity {
        @Id
        @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }

        public void setId(final long id) {
            this.id = id;
        }
    }
}
