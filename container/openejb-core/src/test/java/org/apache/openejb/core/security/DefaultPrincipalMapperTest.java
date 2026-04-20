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
package org.apache.openejb.core.security;

import jakarta.security.jacc.PrincipalMapper;
import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.spi.CallerPrincipal;
import org.junit.Test;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DefaultPrincipalMapperTest {

    private final PrincipalMapper mapper = new AbstractSecurityService.DefaultPrincipalMapper();

    @Test
    public void nullSubjectReturnsNull() {
        assertNull(mapper.getCallerPrincipal((Subject) null));
    }

    @Test
    public void emptySubjectReturnsNull() {
        assertNull(mapper.getCallerPrincipal(new Subject()));
    }

    @Test
    public void jakartaCallerPrincipalIsPreferredOverPlainPrincipal() {
        final Principal plain = new NamedPrincipal("plain");
        final Principal jakarta = new JakartaCaller("jakarta-user");

        final Subject subject = subjectOf(plain, jakarta);
        assertSame(jakarta, mapper.getCallerPrincipal(subject));
    }

    @Test
    public void jakartaCallerPrincipalPreferredOverAnnotatedCallerPrincipal() {
        final Principal annotated = new AnnotatedPrincipal("annotated");
        final Principal jakarta = new JakartaCaller("jakarta-user");

        final Subject subject = subjectOf(annotated, jakarta);
        assertSame(jakarta, mapper.getCallerPrincipal(subject));
    }

    @Test
    public void annotatedCallerPrincipalPickedOverPlain() {
        final Principal plain = new NamedPrincipal("plain");
        final Principal annotated = new AnnotatedPrincipal("annotated");

        final Subject subject = subjectOf(plain, annotated);
        assertSame(annotated, mapper.getCallerPrincipal(subject));
    }

    @Test
    public void groupsAreSkippedWhenFallingBack() {
        final Principal group = new AbstractSecurityService.Group("admins");
        final Principal groupPrincipal = new GroupPrincipal("users");
        final Principal plain = new NamedPrincipal("plain");

        final Subject subject = subjectOf(group, groupPrincipal, plain);
        assertSame(plain, mapper.getCallerPrincipal(subject));
    }

    @Test
    public void onlyGroupsReturnsNull() {
        final Principal group = new AbstractSecurityService.Group("admins");
        final Principal groupPrincipal = new GroupPrincipal("users");

        final Subject subject = subjectOf(group, groupPrincipal);
        assertNull(mapper.getCallerPrincipal(subject));
    }

    @Test
    public void mappedRolesExtractGroupNames() {
        final Principal group = new AbstractSecurityService.Group("admins");
        final Principal groupPrincipal = new GroupPrincipal("users");
        final Principal plain = new NamedPrincipal("plain");

        final Subject subject = subjectOf(group, groupPrincipal, plain);
        final Set<String> roles = mapper.getMappedRoles(subject);
        assertEquals(new HashSet<>(java.util.Arrays.asList("admins", "users")), roles);
    }

    private static Subject subjectOf(final Principal... principals) {
        final Set<Principal> set = new HashSet<>();
        for (final Principal p : principals) {
            set.add(p);
        }
        return new Subject(true, set, new HashSet<>(), new HashSet<>());
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

    @CallerPrincipal
    private static class AnnotatedPrincipal implements Principal {
        private final String name;

        AnnotatedPrincipal(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class JakartaCaller extends jakarta.security.enterprise.CallerPrincipal {
        JakartaCaller(final String name) {
            super(name);
        }
    }
}
