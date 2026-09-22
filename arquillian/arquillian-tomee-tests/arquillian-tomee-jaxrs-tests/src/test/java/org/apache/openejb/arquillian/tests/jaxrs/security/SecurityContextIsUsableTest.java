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
package org.apache.openejb.arquillian.tests.jaxrs.security;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.openejb.loader.IO;
import org.apache.tomee.catalina.realm.LazyRealm;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class SecurityContextIsUsableTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        // the user logs in with BASIC auth against a realm of the webapp
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(RsImpl.class, RestUserRealm.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsManifestResource(new StringAsset("<Context>" +
                "<Realm className=\"" + LazyRealm.class.getName() + "\" realmClass=\"" + RestUserRealm.class.getName() + "\"/>" +
                "</Context>"), "context.xml")
            .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                "<security-constraint>" +
                "<web-resource-collection>" +
                "<web-resource-name>all</web-resource-name>" +
                "<url-pattern>/*</url-pattern>" +
                "</web-resource-collection>" +
                "<auth-constraint><role-name>rest</role-name></auth-constraint>" +
                "</security-constraint>" +
                "<login-config><auth-method>BASIC</auth-method></login-config>" +
                "<security-role><role-name>rest</role-name></security-role>" +
                "</web-app>"));
    }

    @Test
    public void rest() throws IOException {
        final HttpURLConnection connection = HttpURLConnection.class.cast(new URL(base.toExternalForm() + "SecurityContextIsUsableTest").openConnection());
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("rest:tomee".getBytes(StandardCharsets.UTF_8)));
        final String response = IO.slurp(connection.getInputStream());
        assertEquals("rest", response);
    }

    @Path("SecurityContextIsUsableTest")
    @Singleton
    public static class RsImpl {
        @Context
        private SecurityContext sc;

        @GET
        public String check() {
            return sc.getUserPrincipal().getName();
        }
    }

    public static class RestUserRealm extends RealmBase {
        @Override
        protected String getPassword(final String username) {
            return "rest".equals(username) ? "tomee" : null;
        }

        @Override
        protected Principal getPrincipal(final String username) {
            return new GenericPrincipal(username, singletonList("rest"));
        }
    }
}
