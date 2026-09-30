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
package org.apache.openejb.arquillian.tests.jaxrs.application;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.arquillian.tests.jaxrs.beans.HookedRest;
import org.apache.openejb.arquillian.tests.jaxrs.beans.MyExpertRestClass;
import org.apache.openejb.arquillian.tests.jaxrs.beans.MyFirstRestClass;
import org.apache.openejb.arquillian.tests.jaxrs.beans.MyRESTApplication;
import org.apache.openejb.arquillian.tests.jaxrs.beans.MySecondRestClass;
import org.apache.openejb.arquillian.tests.jaxrs.beans.RestWithInjections;
import org.apache.openejb.arquillian.tests.jaxrs.beans.SimpleEJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import java.net.URL;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class SimpleApplicationWithMappingTest {
    public static String BASE_URL = "undefined";

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "SimpleApplicationWithMappingTest.war")
            .addClasses(MySecondRestClass.class, HookedRest.class, RestWithInjections.class, SimpleEJB.class, MyExpertRestClass.class, MyFirstRestClass.class,
                MyRESTApplication.class)
            .addAsWebInfResource(new StringAsset("<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"4.0\" bean-discovery-mode=\"all\"/>"), "beans.xml")
            .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                "<servlet>" +
                "<servlet-name>REST Application</servlet-name>" +
                "<servlet-class>" + Application.class.getName() + "</servlet-class>" +
                "<init-param>" +
                "<param-name>jakarta.ws.rs.Application</param-name>" +
                "<param-value>" + MyRESTApplication.class.getName() + "</param-value>" +
                "</init-param>" +
                "</servlet>" +
                "<servlet-mapping>" +
                "<servlet-name>REST Application</servlet-name>" +
                "<url-pattern>/mapping/*</url-pattern>" +
                "</servlet-mapping>" +
                "</web-app>"));
    }

    @Before
    public void initBaseUrl() {
        BASE_URL = base.toExternalForm() + "mapping";
    }

    @Test
    public void first() {
        final String hi = WebClient.create(BASE_URL).path("/first/hi").accept(TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("Hi from REST World!", hi);
    }

    @Test
    public void second() {
        final String hi = WebClient.create(BASE_URL).path("/second/hi2/2nd").accept(TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("hi 2nd", hi);
    }

    @Test(expected = WebApplicationException.class)
    public void nonListed() {
        WebClient.create(BASE_URL).path("/non-listed/yata/foo").accept(TEXT_PLAIN_TYPE).get(String.class);
    }

    @Test
    public void hooked() {
        assertEquals(true, WebClient.create(BASE_URL).path("/hooked/post").accept(TEXT_PLAIN_TYPE).get(Boolean.class));
    }

    @Test
    public void injectEjb() {
        assertEquals(true, WebClient.create(BASE_URL).path("/inject/ejb").accept(TEXT_PLAIN_TYPE).get(Boolean.class));
    }
}
