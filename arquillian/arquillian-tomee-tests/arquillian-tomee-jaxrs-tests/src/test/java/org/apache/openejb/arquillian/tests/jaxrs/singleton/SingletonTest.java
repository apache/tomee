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

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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

@RunWith(Arquillian.class)
public class SingletonTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(TheResource.class, TheCdiResource.class, Incr.class, MyCdiRESTApplication.class)
            .addAsWebInfResource(new StringAsset("<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" bean-discovery-mode=\"all\" version=\"4.0\"/>"), "beans.xml")
            .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                "<servlet>" +
                "<servlet-name>REST Application</servlet-name>" +
                "<servlet-class>" + Application.class.getName() + "</servlet-class>" +
                "<init-param>" +
                "<param-name>jakarta.ws.rs.Application</param-name>" +
                "<param-value>" + MyCdiRESTApplication.class.getName() + "</param-value>" +
                "</init-param>" +
                "</servlet>" +
                "</web-app>"));
    }

    @Test
    public void checkStateStays() {
        for (int i = 0; i < 3; i++) {
            assertEquals(i, WebClient.create(base.toExternalForm() + "foo").accept(TEXT_PLAIN_TYPE).get(Integer.class).intValue());
        }
    }

    @Test
    public void checkCdiInjections() {
        for (int i = 0; i < 3; i++) {
            assertEquals(i, WebClient.create(base.toExternalForm() + "cdi").accept(TEXT_PLAIN_TYPE).get(Integer.class).intValue());
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
