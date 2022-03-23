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

package org.apache.openejb.junit5;

import org.apache.openejb.api.LocalClient;
import org.apache.openejb.junit.ContextConfig;
import org.apache.openejb.junit.Property;
import org.apache.openejb.junit.TestSecurity;
import org.apache.openejb.junit5.ejbs.BasicEjbLocal;
import org.apache.openejb.junit5.ejbs.SecuredEjbLocal;
import org.apache.openejb.junit5.security.RunWithOpenEjbTestSecurity;
import org.apache.openejb.junit5.security.TestSecurityTemplateInvocationContextProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfig(properties = {
        @Property("openejb.deployments.classpath.include=.*openejb-junit5-backward.*"),
        @Property("java.naming.factory.initial=org.apache.openejb.core.LocalInitialContextFactory")
})
@RunWithOpenEjbTestSecurity
@TestSecurity(
        authorized = {"RoleA"}
)
@LocalClient
public class TestEjbSecurity {
    @EJB
    private BasicEjbLocal basicEjb;

    @EJB
    private SecuredEjbLocal securedEjb;

    public TestEjbSecurity() {
    }

    @TestTemplate
    public void testEjbInjection() {
        assertNotNull(basicEjb);
        assertNotNull(securedEjb);
    }

    @TestTemplate
    public void testClassLevelSecurity() {
        assertNotNull(securedEjb);

        assertEquals("Unsecured Works", basicEjb.concat("Unsecured", "Works"));
        assertEquals("Dual Role Works", securedEjb.dualRole());
        assertEquals("RoleA Works", securedEjb.roleA());
    }

    @TestTemplate
    public void testClassLevelSecurityUnauthorized() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleB();
        });
    }

    @TestTemplate
    @TestSecurity(
            authorized = {"RoleB"}
    )
    public void testMethodLevelSecurity() {
        assertNotNull(securedEjb);

        assertEquals("Unsecured Works", basicEjb.concat("Unsecured", "Works"));
        assertEquals("Dual Role Works", securedEjb.dualRole());
        assertEquals("RoleB Works", securedEjb.roleB());
    }

    @TestTemplate
    @TestSecurity(
            authorized = {"RoleB"}
    )
    public void testMethodLevelSecurityUnauthorized() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleA();
        });
    }

    @TestTemplate
    @TestSecurity(
            authorized = {"RoleA"},
            unauthorized = {"RoleB"}
    )
    public void testMultipleSecurityRoles_RoleA() {
        assertNotNull(securedEjb);
        securedEjb.roleA();
    }

    @TestTemplate
    @TestSecurity(
            authorized = {"RoleB"},
            unauthorized = {"RoleA"}
    )
    public void testMultipleSecurityRoles_RoleB() {
        assertNotNull(securedEjb);
        securedEjb.roleB();
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @TestTemplate
    @TestSecurity(
            authorized = {"RoleB"}
    )
    public void testRoleAFailAuthorized() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleA();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @TestTemplate
    @TestSecurity(
            authorized = {"RoleA"}
    )
    public void testRoleBFailAuthorized() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleB();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Disabled(value = "TODO: How to fix this in JUnit5?")
    @TestTemplate
    @TestSecurity(
            unauthorized = {"RoleA"}
    )
    public void testRoleAFailUnauthorized() {
        assertThrows(AssertionError.class, () -> {
            securedEjb.roleA();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Disabled(value = "TODO: How to fix this in JUnit5?")
    @TestTemplate
    @TestSecurity(
            unauthorized = {"RoleB"}
    )
    public void testRoleBFailUnauthorized() {
        assertThrows(AssertionError.class, () -> {
            securedEjb.roleB();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @TestTemplate
    @TestSecurity(
            unauthorized = {TestSecurity.UNAUTHENTICATED}
    )
    public void testUnauthenticated() {
        securedEjb.roleA();
    }
}
