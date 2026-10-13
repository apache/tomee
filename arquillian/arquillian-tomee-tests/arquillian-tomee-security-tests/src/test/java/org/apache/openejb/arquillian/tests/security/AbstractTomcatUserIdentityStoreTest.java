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
package org.apache.openejb.arquillian.tests.security;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public abstract class AbstractTomcatUserIdentityStoreTest {

    @ArquillianResource
    private URL base;

    protected static WebArchive war(final String name, final Class<?> config) {
        return ShrinkWrap.create(WebArchive.class, name + ".war")
                .addClasses(CallerServlet.class, config)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    protected void assertAuthenticated(final String user, final String password) throws IOException {
        final HttpURLConnection connection = call(user, password);
        try {
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            try (final InputStream in = connection.getInputStream()) {
                assertEquals(user, new String(in.readAllBytes(), UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    protected void assertUnauthorized(final String user, final String password) throws IOException {
        final HttpURLConnection connection = call(user, password);
        try {
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection call(final String user, final String password) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(base, "caller").openConnection();
        connection.setInstanceFollowRedirects(false);
        if (user != null) {
            connection.setRequestProperty("Authorization", "Basic "
                    + Base64.getEncoder().encodeToString((user + ':' + password).getBytes(UTF_8)));
        }
        return connection;
    }
}
