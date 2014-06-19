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
package org.superbiz.deltaspike.service;

import org.superbiz.deltaspike.domain.User;
import org.superbiz.deltaspike.repository.UserRepository;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import java.util.List;

//usually services contain additional logic or delegate to multiple repositories.
//here the service just delegates to one repository to allow tests which use a mocked user-repository.
//currently it isn't supported to mock intercepted beans (see DELTASPIKE-605)
//-> @Repository as well as JpaUserRepository can't use an interceptor like @Transactional
@DomainService
@Typed(UserService.class)
public class UserService implements UserRepository
{
    @Inject
    private UserRepository userRepository;

    /*
     * generated
     */

    @Override
    public User loadUser(String userName)
    {
        return userRepository.loadUser(userName);
    }

    @Override
    public void save(User entity)
    {
        userRepository.save(entity);
    }

    @Override
    public void remove(User entity)
    {
        userRepository.remove(entity);
    }

    @Override
    public List<User> loadAll()
    {
        return userRepository.loadAll();
    }

    @Override
    public User loadById(Long id)
    {
        return userRepository.loadById(id);
    }

    @Override
    public User createNewEntity()
    {
        return userRepository.createNewEntity();
    }
}
