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
package org.apache.openejb.server.httpd;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@EnableServices({"httpejbd"})
@RunWith(ApplicationComposer.class)
public class OpenEJBHttpServerTest {
    private int nextAvailablePort = -1;

    @Test
    public void requestStartsWithoutCallerIdentity() throws Exception {
        final List<String> callersAtRequestStart = new CopyOnWriteArrayList<>();
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        registry.addHttpListener(new HttpListener() {
            @Override
            public void onMessage(final HttpRequest request, final HttpResponse response) throws Exception {
                final Principal principal = SystemInstance.get().getComponent(SecurityService.class).getCallerPrincipal();
                callersAtRequestStart.add(principal == null ? null : principal.getName());
                request.login("jonathan", "secret"); // intentionally never logged out
                response.getOutputStream().write("ok".getBytes());
            }
        }, "/login");
        try {
            final List<Integer> statuses = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                final URL url = new URL("http://localhost:" + nextAvailablePort + "/login");
                final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());
                statuses.add(connection.getResponseCode());
                connection.disconnect();
            }
            assertEquals(10, callersAtRequestStart.size());
            assertFalse(callersAtRequestStart.toString(), callersAtRequestStart.contains("jonathan"));
            assertEquals(Collections.nCopies(10, 200), statuses);
        } finally {
            registry.removeHttpListener("/login");
        }
    }

    @Configuration
    public Properties props() {
        nextAvailablePort = NetworkUtil.getNextAvailablePort();
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(nextAvailablePort))
                .p("httpejbd.threadsCore", "2")
                .p("httpejbd.threads", "2")
                .build();
    }

    @Module
    public EjbJar jar() {
        return new EjbJar();
    }
}
