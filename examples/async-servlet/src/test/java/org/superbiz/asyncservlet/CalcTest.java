/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.asyncservlet;

import org.apache.ziplock.IO;
import org.apache.ziplock.maven.Mvn;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URL;

@RunWith(Arquillian.class)
@RunAsClient
public class CalcTest {

    @ArquillianResource
    private URL base;

    @Deployment
    public static WebArchive createDeployment() {

        final Archive<?> war = Mvn.war();
        System.out.println(war.toString(true));
        return (WebArchive) war;
    }

    @Test
    public void testAddSync() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "add")
                .queryParam("x", "10")
                .queryParam("y", "20")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(200, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);

        Assert.assertEquals("30", responsePayload);
    }

    @Test
    public void tesSubtractSync() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "subtract")
                .queryParam("x", "50")
                .queryParam("y", "34")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(200, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);

        Assert.assertEquals("16", responsePayload);
    }

    @Test
    public void testMultiplySync() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "multiply")
                .queryParam("x", "5")
                .queryParam("y", "3")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(200, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);

        Assert.assertEquals("15", responsePayload);
    }

    @Test
    public void testDivideSync() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "divide")
                .queryParam("x", "40")
                .queryParam("y", "4")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(200, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);

        Assert.assertEquals("10", responsePayload);
    }

    @Test
    public void testAddASync() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "add")
                .queryParam("x", "10")
                .queryParam("y", "20")
                .queryParam("async", "true")
                .queryParam("delay", "2000")
                .queryParam("timeout", "10000")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(200, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);

        Assert.assertEquals("30", responsePayload);
    }

    @Test
    public void tesSubtractASync() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "subtract")
                .queryParam("x", "50")
                .queryParam("y", "34")
                .queryParam("async", "true")
                .queryParam("delay", "2000")
                .queryParam("timeout", "10000")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(200, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);

        Assert.assertEquals("16", responsePayload);
    }

    @Test
    public void testMultiplyASync() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "multiply")
                .queryParam("x", "5")
                .queryParam("y", "3")
                .queryParam("async", "true")
                .queryParam("delay", "2000")
                .queryParam("timeout", "10000")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(200, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);

        Assert.assertEquals("15", responsePayload);
    }

    @Test
    public void testDivideASync() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "divide")
                .queryParam("x", "40")
                .queryParam("y", "4")
                .queryParam("async", "true")
                .queryParam("delay", "2000")
                .queryParam("timeout", "10000")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(200, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);

        Assert.assertEquals("10", responsePayload);
    }

    @Test
    public void testAddASyncTimeout() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "add")
                .queryParam("x", "10")
                .queryParam("y", "20")
                .queryParam("async", "true")
                .queryParam("delay", "10000")
                .queryParam("timeout", "1000")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(500, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);
        System.out.println(responsePayload);
    }

    @Test
    public void tesSubtractASyncTimeout() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "subtract")
                .queryParam("x", "50")
                .queryParam("y", "34")
                .queryParam("async", "true")
                .queryParam("delay", "10000")
                .queryParam("timeout", "1000")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(500, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);
        System.out.println(responsePayload);
    }

    @Test
    public void testMultiplyASyncTimeout() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "multiply")
                .queryParam("x", "5")
                .queryParam("y", "3")
                .queryParam("async", "true")
                .queryParam("delay", "10000")
                .queryParam("timeout", "1000")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(500, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);
        System.out.println(responsePayload);
    }

    @Test
    public void testDivideASyncTimeout() throws Exception {
        final WebTarget webTarget = ClientBuilder.newClient().target(base.toURI());

        final Response response = webTarget
                .queryParam("op", "divide")
                .queryParam("x", "40")
                .queryParam("y", "4")
                .queryParam("async", "true")
                .queryParam("delay", "10000")
                .queryParam("timeout", "1000")
                .request()
                .accept(MediaType.MEDIA_TYPE_WILDCARD)
                .get();

        Assert.assertEquals(500, response.getStatus());
        final InputStream is = (InputStream) response.getEntity();
        final String responsePayload = IO.slurp(is);
        System.out.println(responsePayload);
    }

}