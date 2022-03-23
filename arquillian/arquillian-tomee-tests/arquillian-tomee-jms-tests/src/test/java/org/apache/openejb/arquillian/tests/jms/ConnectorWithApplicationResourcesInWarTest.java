/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jms;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.ziplock.maven.Mvn;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ConnectorWithApplicationResourcesInWarTest {

    @ArquillianResource
    private URL webappUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebAppDescriptor webAppDescriptor = Descriptors.create(WebAppDescriptor.class);
        webAppDescriptor.version("3.0");

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "connector-sample-war.war");
        webArchive.addPackage("org.superbiz.connector.api");
        webArchive.addPackage("org.superbiz.connector.adapter");
        webArchive.addPackage("org.superbiz.application");
        webArchive.addAsWebInfResource(ConnectorWithApplicationResourcesInEarTest.class.getResource("/connector/resources.xml"), "resources.xml");
        webArchive.setWebXML(new StringAsset(webAppDescriptor.exportAsString()));
        System.out.println("Webapp:\n" + webArchive.toString(true));

        return webArchive;
    }

    @Test
    public void testShouldMakeSureWebappIsWorking() throws Exception {
        final WebClient webClient = WebClient.create(webappUrl.toURI());
        final Response response = webClient.path("sender").type(MediaType.TEXT_PLAIN_TYPE).post("Hello, world");

        assertEquals(200, response.getStatus());
        final String result = webClient.path("").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);

        assertEquals("Hello, world", result);
    }

}
