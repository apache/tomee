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
package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@EnableServices("jax-rs")
@Classes(value = SingletonProviderTest.ApplicationSample.class, context = "app")
@RunWith(ApplicationComposer.class)
public class SingletonProviderTest {
    @RandomPort("http")
    private URL base;

    @Test
    public void check() throws Exception {
        final HttpURLConnection conn = HttpURLConnection.class.cast(new URL(base.toExternalForm() + "app/need-provider").openConnection());
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
