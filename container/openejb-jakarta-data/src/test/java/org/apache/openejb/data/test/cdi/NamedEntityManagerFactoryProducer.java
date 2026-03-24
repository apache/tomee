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
package org.apache.openejb.data.test.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

/**
 * CDI producer that exposes the EntityManagerFactory as a named CDI bean.
 * This enables testing the named CDI lookup path in EntityManagerLookup
 * where bean.getName() matches the dataStore value.
 */
@ApplicationScoped
public class NamedEntityManagerFactoryProducer {

    @PersistenceUnit(unitName = "person-unit")
    private EntityManagerFactory emf;

    @Produces
    @Named("my-data-store")
    public EntityManagerFactory produceNamedEntityManagerFactory() {
        return emf;
    }
}
