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
package org.apache.openejb.arquillian.tests.jaxrs.notfound;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.ClientBuilder;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class NotFoundTest {
    @Deployment(testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class, "notfound.war")
                .addClasses(App.class, Endpoint.class, CustomHandler.class)
                .addAsWebInfResource(new StringAsset(
                        "<openejb-jar>" +
                        "   <pojo-deployment class-name=\"jaxrs-application\">" +
                        "       <properties>" +
                        "           cxf.jaxrs.out-interceptors = " + CustomHandler.class.getName() + "\n" +
                        "       </properties>" +
                        "   </pojo-deployment>" +
                        "</openejb-jar>"), "openejb-jar.xml");
    }

    @ArquillianResource
    private URL base;

    @Test
    public void run() {
        //assertEquals("failed", ClientBuilder.newClient().target(base.toExternalForm() + "api/missing").request().get().readEntity(String.class));
        assertEquals("t", ClientBuilder.newClient().target(base.toExternalForm() + "api/there").request().get().readEntity(String.class));
    }
}
