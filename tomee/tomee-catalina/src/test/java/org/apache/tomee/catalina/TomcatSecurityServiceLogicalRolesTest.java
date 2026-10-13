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
package org.apache.tomee.catalina;

import org.apache.catalina.Wrapper;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.junit.Test;
import org.mockito.Mockito;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TomcatSecurityServiceLogicalRolesTest {

    // getLogicalRoles uses no instance state, skip the constructor which needs a running server
    private final TomcatSecurityService service =
            Mockito.mock(TomcatSecurityService.class, Mockito.CALLS_REAL_METHODS);

    @Test
    public void userNamedLikeRoleIsNotGrantedThatRole() {
        final GenericPrincipal generic = new GenericPrincipal("admin", List.of("user"), new NamedPrincipal("admin"));
        final Principal[] principals = {
                generic,
                new TomcatSecurityService.TomcatUser(new UserRoleRealm(), generic),
                generic.getUserPrincipal()
        };

        assertEquals(Set.of("user"), service.getLogicalRoles(principals, Set.of("admin", "user")));
    }

    @Test
    public void groupPrincipalIsMappedToRoleByName() {
        final GenericPrincipal generic = new GenericPrincipal("bob", List.of("user"), new NamedPrincipal("bob"));
        final Principal[] principals = {
                generic,
                new TomcatSecurityService.TomcatUser(new UserRoleRealm(), generic),
                generic.getUserPrincipal(),
                new GroupPrincipal("admin")
        };

        assertEquals(Set.of("admin", "user"), service.getLogicalRoles(principals, Set.of("admin", "user")));
    }

    @Test
    public void groupPrincipalWithoutRealmIsMappedToRoleByName() {
        final Principal[] principals = {new GroupPrincipal("admin")};

        assertEquals(Set.of("admin"), service.getLogicalRoles(principals, Set.of("admin", "user")));
    }

    private static class NamedPrincipal implements Principal {
        private final String name;

        NamedPrincipal(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class GroupPrincipal extends NamedPrincipal {
        GroupPrincipal(final String name) {
            super(name);
        }
    }

    public static class UserRoleRealm extends RealmBase {
        @Override
        public boolean hasRole(final Wrapper wrapper, final Principal principal, final String role) {
            return "user".equals(role);
        }

        @Override
        protected String getPassword(final String username) {
            return null;
        }

        @Override
        protected Principal getPrincipal(final String username) {
            return null;
        }
    }
}
