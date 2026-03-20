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
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.apache.openejb.data.test.entity.Person;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonCustomRepository extends DataRepository<Person, Long> {

    // Method-name finder
    List<Person> findByName(String name);

    // Method-name finder with multiple conditions
    List<Person> findByNameAndAge(String name, int age);

    // Method-name finder with ordering
    List<Person> findByAgeGreaterThanOrderByNameAsc(int age);

    // @Query annotation
    @Query("SELECT p FROM Person p WHERE p.name = ?1")
    List<Person> queryByName(String name);

    // @Query with named parameter
    @Query("SELECT p FROM Person p WHERE p.age > :minAge")
    List<Person> queryByMinAge(@Param("minAge") int minAge);

    // @Find with @By
    @Find
    Optional<Person> findByIdAndName(@By("id") Long id, @By("name") String name);

    // @Find returning list
    @Find
    List<Person> findAllByAge(@By("age") int age);

    // Count by method name
    long countByName(String name);

    // Exists by method name
    boolean existsByEmail(String email);

    // Delete by method name
    int deleteByName(String name);
}
