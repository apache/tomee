/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.arquillian.tests.persistence.ejb;

import jakarta.ejb.AfterBegin;
import jakarta.ejb.BeforeCompletion;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;

@Stateful
public class StatefulBean implements Serializable {
    public static final int ENTITY_ID_AFTER_BEGIN = 1234;
    public static final int ENTITY_ID_BEFORE_COMPLETION = 5678;
    public static final int ENTITY_ID_BUSINESS_METHOD = 9876;

    @PersistenceContext
    EntityManager em;

    @AfterBegin
    public void afterBegin() {
        testPersist(ENTITY_ID_AFTER_BEGIN);
    }

    @BeforeCompletion
    public void beforeCompletion() {
        testPersist(ENTITY_ID_BEFORE_COMPLETION);
        requireThatTestEntityExists(ENTITY_ID_AFTER_BEGIN);
    }

    public void doPersist() {
        testPersist(ENTITY_ID_BUSINESS_METHOD);
        requireThatTestEntityExists(ENTITY_ID_AFTER_BEGIN);
    }

    private void testPersist(int entityId) {
        requireThatTestEntityDoesNotExist(entityId);
        persistEntity(entityId);
        requireThatTestEntityExists(entityId);
    }

    private void requireThatTestEntityDoesNotExist(int id) {
        final TestEntity testEntity = em.find(TestEntity.class, Integer.valueOf(id));
        if (null != testEntity) {
            throw new IllegalStateException("The DB must not contain test entity with id=" + id);
        }
    }

    private void requireThatTestEntityExists(int id) {
        final TestEntity testEntity = em.find(TestEntity.class, Integer.valueOf(id));
        if (null == testEntity) {
            throw new IllegalStateException("The DB must contain test entity with id=" + id);
        }
    }

    private void persistEntity(int id) {
        final TestEntity testEntity = new TestEntity();
        testEntity.setId(id);
        em.persist(testEntity);
    }
}
