/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.datasourcerealm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

public class AddUser {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void add(@Observes @Initialized(ApplicationScoped.class) Object init) {
        final User user = new User();
        user.setUserName("test");
        user.setUserPass("9003d1df22eb4d3820015070385194c8"); // md5(pwd)
        em.persist(user);

        final RoleId roleId = new RoleId();
        roleId.setUserName(user.getUserName());
        roleId.setUserRole("arquillian");

        final Role role = new Role();
        role.setId(roleId);
        em.persist(role);
    }
}
