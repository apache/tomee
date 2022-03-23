/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.embedded;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon31.WebAppVersionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.ClientBuilder;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class JAXRSWebXmlMappingTest {
    @Deployment(testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class, JAXRSWebXmlMappingTest.class.getSimpleName() + ".war")
            .addClass(ARestService.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .setWebXML(new StringAsset(
                Descriptors.create(WebAppDescriptor.class)
                    .version(WebAppVersionType._3_1)
                    .getOrCreateServlet().servletName("jakarta.ws.rs.core.Application")
                    .getOrCreateInitParam().paramName("jakarta.ws.rs.Application").paramValue(SimpleApp.class.getName()).up()
                    .up()
                    .getOrCreateServletMapping().servletName("jakarta.ws.rs.core.Application").urlPattern("/rs/*").up()
                    .exportAsString()
            ));
    }

    @ArquillianResource
    private URL base;

    @Test
    public void noNpe() throws MalformedURLException { // TOMEE-1718
        assertEquals(
            "foo",
            ClientBuilder.newBuilder().build().target(new URL(base, "rs/rest/foo").toExternalForm()).request().get(String.class));
    }
}
