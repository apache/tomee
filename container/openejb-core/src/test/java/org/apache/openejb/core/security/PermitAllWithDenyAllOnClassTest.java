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
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.Singleton;

@RunWith(ApplicationComposer.class)
public class PermitAllWithDenyAllOnClassTest {
    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{DenyAllow.class, AllowDeny.class};
    }

    @EJB
    private DenyAllow denyAllow;

    @EJB
    private AllowDeny allowDeny;

    @Test
    public void allowed() {
        denyAllow.allowed();
    }

    @Test(expected = EJBAccessException.class)
    public void forbidden() {
        denyAllow.forbidden();
    }

    @Test
    public void allowed2() {
        allowDeny.allowed();
    }

    @Test(expected = EJBAccessException.class)
    public void forbidden2() {
        allowDeny.forbidden();
    }

    @Singleton
    @DenyAll
    public static class DenyAllow {
        @PermitAll
        public void allowed() {
            // no-op
        }

        public void forbidden() {
            // no-op
        }
    }

    @Singleton
    @PermitAll
    public static class AllowDeny {
        public void allowed() {
            // no-op
        }

        @DenyAll
        public void forbidden() {
            // no-op
        }
    }
}
