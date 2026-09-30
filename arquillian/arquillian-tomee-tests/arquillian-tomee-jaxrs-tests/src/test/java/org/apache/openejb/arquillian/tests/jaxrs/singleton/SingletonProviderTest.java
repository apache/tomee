/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.singleton;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class SingletonProviderTest {
    @ArquillianResource
    private URL base;

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(SingletonProviderTest.class);
    }

    @Test
    public void check() throws Exception {
        final HttpURLConnection conn = HttpURLConnection.class.cast(new URL(base.toExternalForm() + "need-provider").openConnection());
        assertEquals("ok", IO.slurp(conn.getInputStream()));
        conn.getInputStream().close();
        assertTrue(ApplicationSample.count > 0);
    }

    @Path("need-provider")
    public static class NeedAProvider {
        @GET
        public String providers() {
            throw new IllegalArgumentException();
        }
    }

    @Provider
    public static class DontLetResourcesFail implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(final IllegalArgumentException throwable) {
            return Response.ok("ok").build();
        }
    }

    public static class ApplicationSample extends Application {
        public static volatile int count = 0;

        @Override
        public Set<Object> getSingletons() {
            count++;
            return new HashSet<Object>() {{
                add(new NeedAProvider());
                add(new DontLetResourcesFail());
            }};
        }
    }
}
