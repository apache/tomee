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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.security.DenyAll;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.Singleton;
import jakarta.security.jacc.Policy;
import jakarta.security.jacc.PolicyFactory;
import javax.security.auth.Subject;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
@ContainerProperties(
        @ContainerProperties.Property(
                name = "jakarta.security.jacc.policy.provider",
                value = "org.apache.openejb.core.security.PolicyProviderTest$PermitEverythingPolicy"))
public class PolicyProviderTest {

    private static volatile boolean throwOnDecision;

    @EJB
    private Guarded guarded;

    @After
    public void resetDecisionFailure() {
        throwOnDecision = false;
    }

    @AfterClass
    public static void resetPolicy() {
        PolicyFactory.getPolicyFactory().setPolicy(null);
    }

    @Test
    public void policyIsInstalled() {
        assertTrue(PolicyFactory.getPolicyFactory().getPolicy() instanceof PermitEverythingPolicy);
    }

    @Test
    public void policyDecidesEjbAuthorization() {
        final PermitEverythingPolicy policy =
                (PermitEverythingPolicy) PolicyFactory.getPolicyFactory().getPolicy();
        assertTrue("Policy was not refreshed after its configuration was committed", policy.refreshCount.get() > 0);
        assertEquals("ok", guarded.restricted());
    }

    @Test
    public void securityExceptionFromPolicyIsTreatedAsDenial() {
        throwOnDecision = true;
        try {
            guarded.restricted();
            fail("A policy SecurityException must deny the EJB invocation");
        } catch (final EJBAccessException expected) {
            // expected
        }
    }

    public static class PermitEverythingPolicy implements Policy {
        private final AtomicInteger refreshCount = new AtomicInteger();

        @Override
        public boolean implies(final Permission permissionToBeChecked, final Subject subject) {
            if (throwOnDecision) {
                throw new SecurityException("policy decision failed");
            }
            return refreshCount.get() > 0;
        }

        @Override
        public PermissionCollection getPermissionCollection(final Subject subject) {
            return new Permissions();
        }

        @Override
        public void refresh() {
            refreshCount.incrementAndGet();
        }
    }

    @Singleton
    public static class Guarded {
        @DenyAll
        public String restricted() {
            return "ok";
        }
    }
}
