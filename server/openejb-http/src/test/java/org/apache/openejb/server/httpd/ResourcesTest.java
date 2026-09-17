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
package org.apache.openejb.server.httpd;

import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.WebResource;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@EnableServices("http")
@WebResource("src/test/web")
@Classes
@ContainerProperties(@ContainerProperties.Property(name = "httpejbd.useJetty", value = "fase"))
public class ResourcesTest {
    @Rule
    public final ApplicationComposerRule container = new ApplicationComposerRule(this);

    @RandomPort("http")
    private URL context;

    @Test
    public void classloader() throws IOException {
        assertTrue(IO.slurp(new URL(context.toExternalForm() + "openejb/foo.txt")).contains("from classloader"));
        assertTrue(IO.slurp(new URL(context.toExternalForm() + "openejb/other/foo.txt")).contains("from classloader2"));
    }

    @Test
    public void folder() throws IOException {
        assertTrue(IO.slurp(new URL(context.toExternalForm() + "openejb/bar.txt")).contains("from web"));
        assertTrue(IO.slurp(new URL(context.toExternalForm() + "openejb/sub/bar.txt")).contains("from web2"));
    }

    @Test
    public void parentSegmentsAreNotServed() throws IOException {
        // src/test/web/../../../pom.xml resolves to the module pom
        final String body = slurpQuietly(new URL(context.toExternalForm() + "openejb/%2e%2e/%2e%2e/%2e%2e/pom.xml"));
        assertFalse(body, body.contains("<project"));
    }

    private static String slurpQuietly(final URL url) throws IOException {
        final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());
        try {
            final InputStream stream = connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream();
            return stream == null ? "" : IO.slurp(stream);
        } finally {
            connection.disconnect();
        }
    }
}
