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
package org.apache.openejb.arquillian.tests.realm;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomee.catalina.realm.CdiEventRealm;
import org.apache.tomee.catalina.realm.event.DigestAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.FindSecurityConstraintsEvent;
import org.apache.tomee.catalina.realm.event.GssAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.SslAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.UserPasswordAuthenticationEvent;
import org.ietf.jgss.GSSContext;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.event.Observes;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("either fix test setup (@Category(Embedded.class)) or rewrite this test to use arquillian but ATM it breaks the whole suite cause of AppComposer/Arq lifecycles")
@RunWith(ApplicationComposer.class)
public class CdiEventRealmTest {

    @Module
    @Classes(cdi = true, innerClassesAsBean = true)
    public WebApp app() {
        return new WebApp();
    }

    @Test
    public void userPassword() {
        final GenericPrincipal gp = getGenericPrincipal(new CdiEventRealm().authenticate("john", "secret"));
        assertEquals("john", gp.getName());
        assertEquals("", gp.getPassword());
        assertEquals(1, gp.getRoles().length);
        assertEquals("admin", gp.getRoles()[0]);
    }

    @Test
    public void digest() {
        final GenericPrincipal gp = getGenericPrincipal(new CdiEventRealm().authenticate("ryan", "md5", "nonce", "nc", "cnonce", "qop", "realm", "md5a2"));
        final String[] actual = gp.getRoles();
        final String[] expected = new String[] {"ryan", "md5", "nonce", "nc", "cnonce", "qop", "realm", "md5a2"};

        Arrays.sort(actual);
        Arrays.sort(expected);

        assertArrayEquals(actual, expected);
    }

    @Test
    public void gss() {
        final GenericPrincipal gp = getGenericPrincipal(new CdiEventRealm().authenticate(mock(GSSContext.class), false));
        assertEquals("gss", gp.getName());
        assertEquals("", gp.getPassword());
        assertEquals(1, gp.getRoles().length);
        assertEquals("dummy", gp.getRoles()[0]);
    }

    @Test
    public void ssl() {
        X509Certificate cert = mock(X509Certificate.class);
        GenericPrincipal expected = new GenericPrincipal("john", "doe", Arrays.asList("test"));
        when(cert.getSubjectDN()).thenReturn(expected);
        final GenericPrincipal gp = getGenericPrincipal(new CdiEventRealm().authenticate(new X509Certificate[] { cert }));
        assertEquals(expected, gp);
        assertEquals("john", gp.getName());
        assertEquals("doe", gp.getPassword());
        assertEquals(1, gp.getRoles().length);
        assertEquals("test", gp.getRoles()[0]);
    }

    @Test
    public void find() {
        final SecurityConstraint[] securityConstraints = new CdiEventRealm().findSecurityConstraints(mock(Request.class), mock(Context.class));
        assertEquals(1, securityConstraints.length);
        final SecurityConstraint c = securityConstraints[0];
        assertEquals("CONFIDENTIAL", c.getUserConstraint());
        assertEquals(2, c.findAuthRoles().length);
        assertEquals(1, c.findCollections().length);
        SecurityCollection sc = c.findCollections()[0];
        assertTrue(sc.findPattern("/*"));
    }

    private GenericPrincipal getGenericPrincipal(Principal principal) {
        assertNotNull(principal);
        assertTrue(GenericPrincipal.class.isInstance(principal));
        return GenericPrincipal.class.cast(principal);
    }

    public static class MultiAuthenticator {

        public void authenticate(@Observes final UserPasswordAuthenticationEvent event) {
            assertEquals("john", event.getUsername());
            assertEquals("secret", event.getCredential());
            event.setPrincipal(new GenericPrincipal(event.getUsername(), "", Arrays.asList("admin")));
        }

        public void authenticate(@Observes final DigestAuthenticationEvent event) {
            final List<String> roles = new ArrayList<>();
            roles.add(event.getCnonce());
            roles.add(event.getDigest());
            roles.add(event.getMd5a2());
            roles.add(event.getNc());
            roles.add(event.getNonce());
            roles.add(event.getQop());
            roles.add(event.getRealm());
            roles.add(event.getUsername());
            event.setPrincipal(new GenericPrincipal(event.getUsername(), "", roles));
        }

        public void authenticate(@Observes final GssAuthenticationEvent event) {
            assertNotNull(event.getGssContext());
            event.setPrincipal(new GenericPrincipal("gss", "", Arrays.asList("dummy")));
        }

        public void authenticate(@Observes final SslAuthenticationEvent event) {
            event.setPrincipal(event.getCerts()[0].getSubjectDN());
        }

        public void findSecurityConstraints(@Observes FindSecurityConstraintsEvent event) {
            event.addRoles("admin", "user");
            event.setUserConstraint("CONFIDENTIAL");
        }

    }

}
