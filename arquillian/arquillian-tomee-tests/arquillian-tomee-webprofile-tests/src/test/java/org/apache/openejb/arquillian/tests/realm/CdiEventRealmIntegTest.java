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

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.tomee.catalina.realm.CdiEventRealm;
import org.apache.tomee.catalina.realm.event.UserPasswordAuthenticationEvent;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Singleton;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@Ignore
public class CdiEventRealmIntegTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "realm-test.war")
                .addClasses(MultiAuthenticator.class, MyService.class)
                .addAsWebResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("<Context preemptiveAuthentication=\"true\" antiJARLocking=\"true\">\n" +
                        "<Valve className=\"" + BasicAuthenticator.class.getName() + "\" />\n" +
                        "<Realm className=\"" + CdiEventRealm.class.getName() + "\" />\n" +
                        "</Context>"), "context.xml");
    }

    @ArquillianResource
    private URL webapp;

    @Test
    public void success() {
        final String val = WebClient.create(webapp.toExternalForm(), "admin", "secret", null)
                .path("/test").get(String.class);

        assertEquals("ok", val);
    }

    @Test
    public void notAuthorized() {
        final Response val = WebClient.create(webapp.toExternalForm(), "user", "secret", null)
                .path("/test").get();

        assertEquals(403, val.getStatus());
    }

    @Test
    public void notAuthenticated() {
        final Response val = WebClient.create(webapp.toExternalForm(), "admin", "bla bla", null)
                .path("/test").get();

        assertEquals(401, val.getStatus());
    }


    @Path("/test")
    @Singleton
    public static class MyService {
        @Inject
        private MultiAuthenticator authenticator;

        @GET
        @RolesAllowed("admin")
        public String hello() {
            return authenticator.isStacked() ? "ok" : "ko";
        }
    }

    @RequestScoped
    public static class MultiAuthenticator {
        private boolean stacked = false;

        public void authenticate(@Observes final UserPasswordAuthenticationEvent event) {
            if (!"secret".equals(event.getCredential())) {
                return; // not authenticated
            }
            event.setPrincipal(new GenericPrincipal(event.getUsername(), "", Arrays.asList(event.getUsername())));
        }

        public void stacked(@Observes final UserPasswordAuthenticationEvent event) {
            stacked = true;
        }

        public boolean isStacked() {
            return stacked;
        }
    }

}
