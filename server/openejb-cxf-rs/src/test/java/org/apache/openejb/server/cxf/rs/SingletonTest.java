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

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class SingletonTest {
    @RandomPort("httpejbd")
    private URL base;

    @Module
    @Classes(cdi = true, innerClassesAsBean = true)
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo")
            .addServlet("REST Application", Application.class.getName())
            .addInitParam("REST Application", "jakarta.ws.rs.Application", MyCdiRESTApplication.class.getName());
    }

    @Test
    public void checkStateStays() {
        for (int i = 0; i < 3; i++) {
            assertEquals(i, WebClient.create(base.toExternalForm() + "foo/foo").accept(TEXT_PLAIN_TYPE).get(Integer.class).intValue());
        }
    }

    @Test
    public void checkCdiInjections() {
        for (int i = 0; i < 3; i++) {
            assertEquals(i, WebClient.create(base.toExternalForm() + "foo/cdi").accept(TEXT_PLAIN_TYPE).get(Integer.class).intValue());
        }
    }

    @Path("foo")
    public static class TheResource {
        private int i;

        @GET
        public int get() {
            return i++;
        }
    }

    @Path("cdi")
    public static class TheCdiResource {
        @Inject
        private Incr incr;
        private int destroy;

        @GET
        public int get() {
            return incr.get() + destroy;
        }

        @PreDestroy
        public void incr() {
            destroy++;
        }
    }

    public static class Incr {
        private int i;

        public int get() {
            return i++;
        }
    }

    public static class MyCdiRESTApplication extends Application {
        @Override
        public Set<Object> getSingletons() {
            return new HashSet<>(asList(new TheResource(), new TheCdiResource()));
        }
    }
}
