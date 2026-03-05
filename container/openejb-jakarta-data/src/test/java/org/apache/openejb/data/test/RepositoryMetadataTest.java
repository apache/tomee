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
package org.apache.openejb.data.test;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Repository;
import org.apache.openejb.data.meta.RepositoryMetadata;
import org.apache.openejb.data.test.entity.Person;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryMetadataTest {

    // -- Test interfaces --

    @Repository
    interface PersonCrudRepo extends CrudRepository<Person, Long> {
    }

    @Repository
    interface PersonBasicRepo extends BasicRepository<Person, Long> {
    }

    @Repository
    interface PersonDataRepo extends DataRepository<Person, Long> {
    }

    @Repository(dataStore = "myPU")
    interface PersonWithDataStore extends CrudRepository<Person, Long> {
    }

    @Repository
    interface StringKeyRepo extends DataRepository<Person, String> {
    }

    // Intermediate interface (no type args, just extends)
    interface IntermediateRepo extends CrudRepository<Person, Long> {
    }

    @Repository
    interface ExtendedIntermediateRepo extends IntermediateRepo {
    }

    @Repository(dataStore = "")
    interface EmptyDataStoreRepo extends DataRepository<Person, Long> {
    }

    // -- Tests: CrudRepository --

    @Test
    void resolvesEntityClassFromCrudRepository() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonCrudRepo.class);
        assertEquals(Person.class, meta.getEntityClass());
    }

    @Test
    void resolvesKeyClassFromCrudRepository() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonCrudRepo.class);
        assertEquals(Long.class, meta.getKeyClass());
    }

    @Test
    void returnsRepositoryInterface() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonCrudRepo.class);
        assertEquals(PersonCrudRepo.class, meta.getRepositoryInterface());
    }

    // -- Tests: BasicRepository --

    @Test
    void resolvesEntityClassFromBasicRepository() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonBasicRepo.class);
        assertEquals(Person.class, meta.getEntityClass());
    }

    @Test
    void resolvesKeyClassFromBasicRepository() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonBasicRepo.class);
        assertEquals(Long.class, meta.getKeyClass());
    }

    // -- Tests: DataRepository --

    @Test
    void resolvesEntityClassFromDataRepository() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonDataRepo.class);
        assertEquals(Person.class, meta.getEntityClass());
    }

    @Test
    void resolvesKeyClassFromDataRepository() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonDataRepo.class);
        assertEquals(Long.class, meta.getKeyClass());
    }

    // -- Tests: dataStore --

    @Test
    void extractsDataStoreValue() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonWithDataStore.class);
        assertEquals("myPU", meta.getDataStore());
    }

    @Test
    void defaultDataStoreIsEmpty() {
        final RepositoryMetadata meta = new RepositoryMetadata(PersonCrudRepo.class);
        assertEquals("", meta.getDataStore());
    }

    @Test
    void emptyDataStoreAnnotation() {
        final RepositoryMetadata meta = new RepositoryMetadata(EmptyDataStoreRepo.class);
        assertEquals("", meta.getDataStore());
    }

    // -- Tests: different key types --

    @Test
    void resolvesStringKeyType() {
        final RepositoryMetadata meta = new RepositoryMetadata(StringKeyRepo.class);
        assertEquals(String.class, meta.getKeyClass());
    }

    // -- Tests: intermediate interface hierarchy --

    @Test
    void resolvesTypesFromIntermediateInterface() {
        final RepositoryMetadata meta = new RepositoryMetadata(ExtendedIntermediateRepo.class);
        assertEquals(Person.class, meta.getEntityClass());
        assertEquals(Long.class, meta.getKeyClass());
    }
}
