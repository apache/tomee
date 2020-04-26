/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.myfaces.repository.jpa;

import org.superbiz.myfaces.domain.User;
import org.superbiz.myfaces.repository.Repository;
import org.superbiz.myfaces.repository.UserRepository;

import jakarta.persistence.Query;
import java.util.List;

@Repository
public class JpaUserRepository extends AbstractGenericJpaRepository<User> implements UserRepository {

    private static final long serialVersionUID = 672568789774892077L;

    public User loadUser(String userName) {
        Query query = this.entityManager.createNamedQuery("findUserByName");
        query.setParameter("currentUser", userName);

        //just for the demo:
        List result = query.getResultList();
        if (result != null && result.size() == 1) {
            return (User) result.iterator().next();
        }
        return null;
    }
}