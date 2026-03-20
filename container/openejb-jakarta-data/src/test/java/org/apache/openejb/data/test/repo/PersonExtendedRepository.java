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
package org.apache.openejb.data.test.repo;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import org.apache.openejb.data.test.entity.Person;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PersonExtendedRepository extends CrudRepository<Person, Long> {

    // Stream return type via method-name finder
    Stream<Person> findByAgeGreaterThan(int age);

    // Optional return type via method-name finder
    Optional<Person> findByEmail(String email);

    // Single entity return via method-name finder
    Person findByName(String name);

    // Method name finder with LessThanEqual
    List<Person> findByAgeLessThanEqual(int age);

    // Method name finder with Contains
    List<Person> findByNameContains(String part);

    // Method name delete
    int deleteByName(String name);

    // @Query returning count
    @Query("SELECT COUNT(p) FROM Person p WHERE p.age > ?1")
    long countOlderThan(int age);

    // @Query returning boolean-like (exists check)
    @Query("SELECT p FROM Person p WHERE p.email = ?1")
    Optional<Person> findOneByEmail(String email);

    // @Find with @By returning Optional
    @Find
    Optional<Person> lookupByIdAndName(@By("id") Long id, @By("name") String name);

    // Custom @Insert annotated method
    @Insert
    Person customInsert(Person person);

    // Custom @Update annotated method
    @Update
    Person customUpdate(Person person);

    // Custom @Save annotated method
    @Save
    Person customSave(Person person);

    // Custom @Delete annotated method
    @Delete
    void customDelete(Person person);

    // @Delete with no args: delete all entities
    @Delete
    void deleteAllEntities();

    // deleteAll with no args: bulk delete all entities (exercises handleDeleteAll else branch)
    void deleteAll();
}
