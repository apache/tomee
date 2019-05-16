/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.tck.openapi;


import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.loader.IO;
import org.apache.tomee.microprofile.tck.openapi.sampleapp.ApplicationConfig;
import org.apache.tomee.microprofile.tck.openapi.sampleapp.User;
import org.apache.tomee.microprofile.tck.openapi.sampleapp.UserResource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URL;

public class OpenAPIApplicationTest extends Arquillian {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "openapi-test.war")
                .addClasses(ApplicationConfig.class, User.class, UserResource.class);
    }

    @ArquillianResource
    private URL serviceUrl;

    @Test
    @RunAsClient
    public void testSimpleApplicationAnnotations() throws Exception {
        final Response response = WebClient.create(serviceUrl.toURI()).path("api/openapi").get();
        Assert.assertEquals(200, response.getStatus());
        final InputStream entity = (InputStream) response.getEntity();

        final String responseBody = IO.slurp(entity);
        Assert.assertTrue(responseBody.contains("title: \"OpenAPI Sample Project\""));
    }
}