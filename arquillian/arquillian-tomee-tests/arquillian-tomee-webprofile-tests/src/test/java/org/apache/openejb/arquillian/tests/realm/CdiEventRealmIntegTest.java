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

import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.openejb.loader.IO;
import org.apache.tomee.catalina.realm.CdiEventRealm;
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

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CdiEventRealmIntegTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "realm-test.war")
                .addClasses(MultiAuthenticator.class, MyService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("<Context preemptiveAuthentication=\"true\" antiJARLocking=\"true\">\n" +
                        "<Valve className=\"" + BasicAuthenticator.class.getName() + "\" />\n" +
                        "<Realm className=\"" + CdiEventRealm.class.getName() + "\" />\n" +
                        "</Context>"), "context.xml");
    }

    @ArquillianResource
    private URL webapp;

    @Test
    public void success() throws IOException {
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        IO.copy(connection("test", "admin", "secret").getInputStream(), res);
        assertEquals("ok", new String(res.toByteArray()));
    }

    @Test
    public void notAuthorized() throws IOException {
        assertEquals(403, connection("test", "user", "secret").getResponseCode());
    }

    @Test
    public void notAuthenticated() throws IOException {
        assertEquals(401, connection("test", "admin", "bla bla").getResponseCode());
    }

    private HttpURLConnection connection(final String path, final String username, final String password) throws IOException {
        final HttpURLConnection con = (HttpURLConnection) new URL(webapp.toExternalForm() + path).openConnection();
        String userCredentials = username + ":" + password;
        String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
        con.setRequestProperty("Authorization", basicAuth);
        con.setUseCaches(false);
        return con;
    }

}
