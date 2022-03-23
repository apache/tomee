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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.webxmloverride;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class WebXmlOverrideTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "WebXmlOverrideTest.war")
            .addClasses(TheResource.class, RSApp.class)
            .setWebXML(new StringAsset(
                Descriptors.create(WebAppDescriptor.class)
                    .version(WebAppVersionType._3_0)
                    .createServlet()
                    .servletName(RSApp.class.getName())
                    .createInitParam().paramName(Application.class.getName()).paramValue(RSApp.class.getName()).up()
                    .up()
                    .createServletMapping()
                    .servletName(RSApp.class.getName())
                    .urlPattern("/xml/*")
                    .up()
                    .exportAsString()));
    }

    @ArquillianResource
    private URL base;

    @Test
    public void overriden() {
        assertEquals("resource", ClientBuilder.newClient().target(base.toExternalForm() + "xml/touch").request().get(String.class));
    }
}
