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

import org.apache.openejb.assembler.classic.ComparableValidationConfig;
import org.apache.openejb.assembler.classic.EntityManagerFactoryCallable;
import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.junit.Test;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.ProviderUtil;
import jakarta.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class JtaEntityManagerTest {
    @Test
    public void isJpa21() {
        final PersistenceUnitInfoImpl info = new PersistenceUnitInfoImpl();
        info.setProperties(new Properties());

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        assertTrue(JtaEntityManager.isJPA21(new ReloadableEntityManagerFactory(
                loader,
                new EntityManagerFactoryCallable(Jpa21Provider.class.getName(), info, loader, new HashMap<>(), false),
                info)));
    }

    public static class Jpa21Provider implements PersistenceProvider {
        @Override
        public EntityManagerFactory createEntityManagerFactory(final String emName, final Map map) {
            return null;
        }

        @Override
        public EntityManagerFactory createContainerEntityManagerFactory(final PersistenceUnitInfo info, final Map map) {
            return null;
        }

        @Override
        public void generateSchema(final PersistenceUnitInfo info, final Map map) {
            // no-op
        }

        @Override
        public boolean generateSchema(final String persistenceUnitName, final Map map) {
            return false;
        }

        @Override
        public ProviderUtil getProviderUtil() {
            return null;
        }
    }
}
