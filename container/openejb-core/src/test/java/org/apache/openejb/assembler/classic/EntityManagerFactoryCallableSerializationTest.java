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
package org.apache.openejb.assembler.classic;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Classes(cdi = true)
@PersistenceUnitDefinition
@RunWith(ApplicationComposer.class)
public class EntityManagerFactoryCallableSerializationTest {
    @PersistenceContext
    private EntityManager em;

    @Test
    public void serializationRoundTrip() {
        final Object em = SerializationUtils.deserialize(SerializationUtils.serialize(Serializable.class.cast(this.em)));
        assertTrue(EntityManager.class.isInstance(em));
        final ReloadableEntityManagerFactory factory = ReloadableEntityManagerFactory.class.cast(Reflections.get(em, "entityManagerFactory"));
        assertNotNull(factory.getDelegate());
        assertNotNull(Reflections.get(factory, "entityManagerFactoryCallable"));
    }
}
