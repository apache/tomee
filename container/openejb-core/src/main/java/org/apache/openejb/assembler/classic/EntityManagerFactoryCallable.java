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

import org.apache.openejb.persistence.PersistenceUnitInfoImpl;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class EntityManagerFactoryCallable implements Callable<EntityManagerFactory> {
    private final String persistenceProviderClassName;
    private final PersistenceUnitInfoImpl unitInfo;

    public EntityManagerFactoryCallable(String persistenceProviderClassName, PersistenceUnitInfoImpl unitInfo) {
        this.persistenceProviderClassName = persistenceProviderClassName;
        this.unitInfo = unitInfo;
    }

    @Override
    public EntityManagerFactory call() throws Exception {
        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(persistenceProviderClassName);
        PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();

        // Create entity manager factories with the validator factory
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("javax.persistence.validator.ValidatorFactory", new ValidatorFactoryWrapper());
        EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, properties);
        return emf;
    }
}
