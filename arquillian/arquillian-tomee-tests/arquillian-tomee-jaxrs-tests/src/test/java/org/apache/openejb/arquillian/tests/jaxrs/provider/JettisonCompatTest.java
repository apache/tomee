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
package org.apache.openejb.arquillian.tests.jaxrs.provider;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

@RunWith(Arquillian.class)
public class JettisonCompatTest {
    // jettison is not shipped with TomEE, only the test classpath (the container classpath of tomee-embedded) has it
    @ClassRule
    public static final ExternalResource EMBEDDED_ONLY = new ExternalResource() {
        @Override
        protected void before() {
            assumeTrue("needs jettison in the container",
                    System.getProperty("openejb.arquillian.adapter", "embedded").contains("embedded"));
        }
    };

    @ArquillianResource
    private URL root;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "JettisonCompatTest.war")
            .addClass(JettisonCompatTest.class)
            .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\" />"), "beans.xml")
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <pojo-deployment class-name=\"jaxrs-application\">\n" +
                "    <properties>\n" +
                "      cxf.jaxrs.providers = org.apache.cxf.jaxrs.provider.json.JSONProvider\n" +
                "    </properties>\n" +
                "  </pojo-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void run() throws IOException {
        assertEquals("{\"jet\":{\"name\":\"test\"}}", IO.slurp(new URL(root.toExternalForm() + "jettison")));
    }

    @Path("jettison")
    @ApplicationScoped
    public static class JettisonEndpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Jet get() {
            return new Jet();
        }
    }

    @XmlRootElement
    public static class Jet {
        private String name = "test";

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}
