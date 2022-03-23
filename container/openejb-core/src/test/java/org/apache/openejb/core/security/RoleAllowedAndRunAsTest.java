/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.security;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.testing.Classes;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Singleton;
import javax.security.auth.login.LoginException;

import static org.junit.Assert.assertEquals;

@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class RoleAllowedAndRunAsTest {
    @EJB
    private DefaultRoles bean;

    @Test
    public void run() throws LoginException {
        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        final Object id = securityService.login("jonathan", "secret");
        securityService.associate(id);
        try {
            assertEquals("jonathan > role1", bean.stack());
        } finally {
            securityService.disassociate();
            securityService.logout(id);
        }
    }

    @Singleton
    public static class Identity {
        @Resource
        private EJBContext context;

        @RolesAllowed("role1")
        public String name() {
            return context.getCallerPrincipal().getName();
        }
    }

    @Singleton
    @RunAs("role1")
    @RolesAllowed("committer")
    public static class DefaultRoles {
        @Resource
        private EJBContext context;

        @EJB
        private Identity identity;

        public String stack() {
            return context.getCallerPrincipal().getName() + " > " + identity.name();
        }
    }
}
