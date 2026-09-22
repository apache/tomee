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

import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.openejb.util.reflection.Reflections;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.net.URL;
import jakarta.ejb.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Providers;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.annotation.XmlRootElement;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class AdvancedProviderConfigTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "AdvancedProviderConfigTest.war")
            .addClass(AdvancedProviderConfigTest.class)
            .addAsWebInfResource(new StringAsset(
                "<resources>\n" +
                "  <Service id=\"xml\" class-name=\"" + JAXBElementProvider.class.getName() + "\">\n" +
                "    eventHandler = $handler\n" +
                "  </Service>\n" +
                "  <Service id=\"handler\" class-name=\"" + MyValidator.class.getName() + "\" />\n" +
                "</resources>\n"), "resources.xml")
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <pojo-deployment class-name=\"jaxrs-application\">\n" +
                "    <properties>\n" +
                "      cxf.jaxrs.providers = xml\n" +
                "    </properties>\n" +
                "  </pojo-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void check() throws Exception {
        assertEquals("true", ClientBuilder.newClient()
                .target(base.toExternalForm())
                .path("advanced-provider-config")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class));
    }

    @Singleton
    @Path("advanced-provider-config")
    public static class AdvancedBean {
        @Context
        private Providers providers;

        @GET
        public boolean providers() {
            final JAXBElementProvider<?> mbr = JAXBElementProvider.class.cast(providers.getMessageBodyReader(Pojo.class, Pojo.class, new Annotation[0], MediaType.APPLICATION_XML_TYPE));
            return MyValidator.class.isInstance(Reflections.get(mbr, "eventHandler"));
        }

    }

    @XmlRootElement
    public static class Pojo {

    }

    public static class MyValidator implements ValidationEventHandler {
        @Override
        public boolean handleEvent(final ValidationEvent event) {
            return false;
        }
    }
}
