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
package org.superbiz.dynamic;

import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Map;

@Stateless
@PersistenceContext(name = "dynamic")
public interface UserDao {

    User findById(long id);

    Collection<User> findByName(String name);

    Collection<User> findByNameAndAge(String name, int age);

    Collection<User> findAll();

    Collection<User> findAll(int first, int max);

    Collection<User> namedQuery(String name, Map<String, ?> params, int first, int max);

    Collection<User> namedQuery(String name, int first, int max, Map<String, ?> params);

    Collection<User> namedQuery(String name, Map<String, ?> params);

    Collection<User> namedQuery(String name);

    Collection<User> query(String value, Map<String, ?> params);

    void save(User u);

    void delete(User u);

    User update(User u);
}
