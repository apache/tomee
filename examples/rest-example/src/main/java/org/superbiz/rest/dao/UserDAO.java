/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.rest.dao;

import org.superbiz.rest.model.User;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.util.List;

@Singleton
@Lock(LockType.READ)
public class UserDAO {

    @Inject
    private DAO dao;

    public User create(String name, String pwd, String mail) {
        User user = new User();
        user.setFullname(name);
        user.setPassword(pwd);
        user.setEmail(mail);
        return dao.create(user);
    }

    public List<User> list(int first, int max) {
        return dao.namedFind(User.class, "user.list", first, max);
    }

    public User find(long id) {
        return dao.find(User.class, id);
    }

    public void delete(long id) {
        dao.delete(User.class, id);
    }

    public User update(long id, String name, String pwd, String mail) {
        User user = dao.find(User.class, id);
        if (user == null) {
            throw new IllegalArgumentException("setUser id " + id + " not found");
        }

        user.setFullname(name);
        user.setPassword(pwd);
        user.setEmail(mail);
        return dao.update(user);
    }
}
