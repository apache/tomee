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
package org.apache.openejb.arquillian.tests.jaxrs.event;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.rs.event.ExtensionProviderRegistration;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ExtensionProviderRegistrationTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "foo.war")
                .addClasses(ExtensionProviderRegistrationTest.class, ServerCreatedEndpoint.class, MyMapper.class, Observer.class, ObserverRegistration.class)
                // MyMapper must only come from the observer, not from provider scanning
                .addAsWebInfResource(new StringAsset("openejb.jaxrs.providers.auto = false"), "application.properties");
    }

    @Test
    public void checkEvent() throws IOException {
        assertEquals("foo", IO.slurp(new URL(base.toExternalForm() + "ExtensionProviderRegistrationTest/")));
    }

    @Path("ExtensionProviderRegistrationTest")
    public static class ServerCreatedEndpoint {
        @GET
        public String useless() {
            throw new IllegalArgumentException("foo");
        }
    }

    @Provider
    public static class MyMapper implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(final IllegalArgumentException e) {
            return Response.ok(e.getMessage()).build();
        }
    }

    public static class Observer {
        public void obs(@Observes final ExtensionProviderRegistration event) {
            event.getProviders().add(new MyMapper());
        }
    }

    // registers the observer before the JAX-RS deployment of this webapp, removes it on undeploy
    @WebListener
    public static class ObserverRegistration implements ServletContextListener {
        private final Observer observer = new Observer();

        @Override
        public void contextInitialized(final ServletContextEvent sce) {
            SystemInstance.get().addObserver(observer);
        }

        @Override
        public void contextDestroyed(final ServletContextEvent sce) {
            SystemInstance.get().removeObserver(observer);
        }
    }
}
