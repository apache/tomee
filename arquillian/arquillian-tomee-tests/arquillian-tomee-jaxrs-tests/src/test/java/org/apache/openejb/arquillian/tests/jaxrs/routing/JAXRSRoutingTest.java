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
package org.apache.openejb.arquillian.tests.jaxrs.routing;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.arquillian.tests.jaxrs.beans.MyExpertRestClass;
import org.apache.openejb.arquillian.tests.jaxrs.beans.MyFirstRestClass;
import org.apache.openejb.arquillian.tests.jaxrs.beans.RestWithInjections;
import org.apache.openejb.arquillian.tests.jaxrs.beans.SimpleEJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class JAXRSRoutingTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "JAXRSRoutingTest.war")
            .addClasses(RestWithInjections.class, FirstService.class, SimpleEJB.class, MyExpertRestClass.class, MyFirstRestClass.class,
                NoClassAtPathApplication.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                "<servlet>" +
                "<servlet-name>REST Application</servlet-name>" +
                "<servlet-class>" + Application.class.getName() + "</servlet-class>" +
                "<init-param>" +
                "<param-name>jakarta.ws.rs.Application</param-name>" +
                "<param-value>" + NoClassAtPathApplication.class.getName() + "</param-value>" +
                "</init-param>" +
                "</servlet>" +
                "</web-app>"));
    }

    @Test
    public void routing() {
        assertEquals("routing", WebClient.create(base.toExternalForm()).path("routing").get(String.class));
    }

    public static class NoClassAtPathApplication extends Application {
        private final Set<Class<?>> classes = new HashSet<Class<?>>();

        public NoClassAtPathApplication() {
            classes.add(FirstService.class);
        }

        @Override
        public Set<Class<?>> getClasses() {
            return classes;
        }
    }

    public static class FirstService {
        @Path("routing")
        @GET
        public String routing() {
            return "routing";
        }
    }
}
