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
package org.apache.openejb.arquillian.tests.jaxrs.ejb;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class RsWithInterfaceTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(RsWithInterfaceTest.class);
    }

    @Test
    public void rest() throws IOException {
        final String response = ClientBuilder.newClient()
                .target(base.toExternalForm())
                .path("itf/check")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class);
        assertEquals("true", response);
    }

    @ApplicationPath("/")
    public static class App extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return new HashSet<Class<?>>() {{
                add(RsImpl.class);
            }};
        }

    }

    @Path("/itf")
    @Singleton
    public static class RsImpl implements Rs {
        public boolean check(final SecurityContext sc) {
            return sc != null;
        }
    }

    public interface Rs {
        @GET
        @Path("/check")
        boolean check(@Context final SecurityContext sc);
    }
}
