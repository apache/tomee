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
package org.apache.openejb.arquillian.tests.jaxrs.provider;

import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.message.Message;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import jakarta.ejb.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CustomContextTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "CustomContextTest.war")
            .addClass(CustomContextTest.class)
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <pojo-deployment class-name=\"jaxrs-application\">\n" +
                "    <properties>\n" +
                "      cxf.jaxrs.providers = " + CustomProvider.class.getName() + "\n" +
                "    </properties>\n" +
                "  </pojo-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void rest() throws IOException {
        final String response = ClientBuilder.newClient()
                .target(base.toExternalForm())
                .path("custom-context/check")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class);
        assertEquals("true", response);
    }

    @Singleton
    @Path("/custom-context")
    public static class CustomContextInjectedBean {
        @Context
        private IFoo foo;

        @GET
        @Path("/check")
        public boolean check() {
            return foo != null && foo.getMsg() != null;
        }
    }

    public static interface IFoo {
        Message getMsg();
    }

    public static class Foo implements IFoo {
        private final Message msg;

        public Foo(final Message message) {
            msg = message;
        }

        public Message getMsg() {
            return msg;
        }
    }

    @Provider
    public static class CustomProvider implements ContextProvider<IFoo> {
        @Override
        public IFoo createContext(final Message message) {
            return new Foo(message);
        }
    }
}
