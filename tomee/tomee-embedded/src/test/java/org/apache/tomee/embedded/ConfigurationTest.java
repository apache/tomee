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
package org.apache.tomee.embedded;

import org.apache.catalina.realm.JAASRealm;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationTest {
    @Test
    public void autoConfig() {
        final Configuration configuration = new Configuration();
        configuration.loadFromProperties(new PropertiesBuilder()
                // plain params
                .p("http", "1234")
                .p("stop", "1235")
                .p("host", "here")
                .p("dir", "target/dirtmp")
                .p("quickSession", "false")
                .p("webResourceCached", "false")
                .p("withEjbRemote", "true")
                .p("deployOpenEjbApp", "true")
                .p("users.u1", "p1")
                .p("users.u2", "p2")
                .p("roles.admin", "u1,u2")
                .p("roles.simple", "u1")
                // more complex structures
                .p("realm", "org.apache.catalina.realm.JAASRealm")
                .p("realm.appName", "app")
                .p("realm.configFile", "configuration.jaas")
                // there we ensure our builders are xbean friendly
                .p("login", "")
                .p("login.realmName", "app")
                .p("login.authMethod", "BASIC")
                .p("securityConstraint", "")
                .p("securityConstraint.authConstraint", "true")
                .p("securityConstraint.authRole", "**")
                .p("securityConstraint.collection", "api:/api/*")
                .build());

        assertEquals(1234, configuration.getHttpPort());
        assertEquals(1235, configuration.getStopPort());
        assertEquals("target/dirtmp", configuration.getDir());
        assertFalse(configuration.isQuickSession());
        assertTrue(configuration.isWithEjbRemote());
        assertTrue(configuration.isDeployOpenEjbApp());
        assertEquals(new HashMap<String, String>() {{
            put("u1", "p1");
            put("u2", "p2");
        }}, configuration.getUsers());
        assertEquals(new HashMap<String, String>() {{
            put("admin", "u1,u2");
            put("simple", "u1");
        }}, configuration.getRoles());

        assertNotNull(configuration.getRealm());
        assertTrue(JAASRealm.class.isInstance(configuration.getRealm()));
        final JAASRealm realm = JAASRealm.class.cast(configuration.getRealm());
        assertEquals("app", realm.getAppName());
        assertEquals("configuration.jaas", realm.getConfigFile());

        assertNotNull(configuration.getLoginConfig());
        final LoginConfig loginConfig = configuration.getLoginConfig().build();
        assertEquals("app", loginConfig.getRealmName());
        assertEquals("BASIC", loginConfig.getAuthMethod());

        final Collection<SecurityConstaintBuilder> securityConstraints = configuration.getSecurityConstraints();
        assertNotNull(securityConstraints);
        assertEquals(1, securityConstraints.size());
        final SecurityConstraint constraint = securityConstraints.iterator().next().build();
        assertTrue(constraint.getAuthConstraint());
        assertTrue(constraint.getAuthenticatedUsers());
        assertEquals("/api/*", constraint.findCollection("api").findPatterns()[0]);
    }
}
