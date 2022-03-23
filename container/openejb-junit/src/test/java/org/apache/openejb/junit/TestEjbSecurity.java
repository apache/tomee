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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.junit;

import org.apache.openejb.api.LocalClient;
import org.apache.openejb.junit.ejbs.BasicEjbLocal;
import org.apache.openejb.junit.ejbs.SecuredEjbLocal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;

@ContextConfig(properties = {
    @Property("openejb.deployments.classpath.include=.*openejb-junit.*"),
    @Property("java.naming.factory.initial=org.apache.openejb.core.LocalInitialContextFactory")
})
@RunWith(OpenEjbRunner.class)
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

    @Test
    public void testEjbInjection() {
        assertNotNull(basicEjb);
        assertNotNull(securedEjb);
    }

    @Test
    public void testClassLevelSecurity() {
        assertNotNull(securedEjb);

        assertEquals("Unsecured Works", basicEjb.concat("Unsecured", "Works"));
        assertEquals("Dual Role Works", securedEjb.dualRole());
        assertEquals("RoleA Works", securedEjb.roleA());
    }

    @Test(expected = EJBAccessException.class)
    public void testClassLevelSecurityUnauthorized() {
        assertNotNull(securedEjb);

        securedEjb.roleB();
    }

    @Test
    @TestSecurity(
        authorized = {"RoleB"}
    )
    public void testMethodLevelSecurity() {
        assertNotNull(securedEjb);

        assertEquals("Unsecured Works", basicEjb.concat("Unsecured", "Works"));
        assertEquals("Dual Role Works", securedEjb.dualRole());
        assertEquals("RoleB Works", securedEjb.roleB());
    }

    @Test(expected = EJBAccessException.class)
    @TestSecurity(
        authorized = {"RoleB"}
    )
    public void testMethodLevelSecurityUnauthorized() {
        assertNotNull(securedEjb);

        securedEjb.roleA();
    }

    @Test
    @TestSecurity(
        authorized = {"RoleA"},
        unauthorized = {"RoleB"}
    )
    public void testMultipleSecurityRoles_RoleA() {
        assertNotNull(securedEjb);

        securedEjb.roleA();
    }

    @Test
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
    @Test(expected = EJBAccessException.class)
    @TestSecurity(
        authorized = {"RoleB"}
    )
    public void testRoleAFailAuthorized() {
        assertNotNull(securedEjb);

        securedEjb.roleA();
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test(expected = EJBAccessException.class)
    @TestSecurity(
        authorized = {"RoleA"}
    )
    public void testRoleBFailAuthorized() {
        assertNotNull(securedEjb);

        securedEjb.roleB();
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test(expected = AssertionError.class)
    @TestSecurity(
        unauthorized = {"RoleA"}
    )
    public void testRoleAFailUnauthorized() {
        securedEjb.roleA();
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test(expected = AssertionError.class)
    @TestSecurity(
        unauthorized = {"RoleB"}
    )
    public void testRoleBFailUnauthorized() {
        securedEjb.roleB();
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test
    @TestSecurity(
        unauthorized = {TestSecurity.UNAUTHENTICATED}
    )
    public void testUnauthenticated() {
        securedEjb.roleA();
    }
}
