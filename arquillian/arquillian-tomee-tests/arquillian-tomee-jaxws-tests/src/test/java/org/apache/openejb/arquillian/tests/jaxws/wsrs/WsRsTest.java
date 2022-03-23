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
package org.apache.openejb.arquillian.tests.jaxws.wsrs;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.core.Application;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class WsRsTest {

    @ArquillianResource
    private URL url;

    protected HttpClient client = new DefaultHttpClient();

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, WsRsTest.class.getName().concat(".war"))
                .addClasses(Bean.class)
                // jaxws and jaxrs are "servlets" so if one (jaxrs here) binds to /* then the other one is not accessible depending deployment order
                .setWebXML(new StringAsset(
                        Descriptors.create(WebAppDescriptor.class)
                            .version(WebAppVersionType._3_0)
                            .getOrCreateServlet()
                                .servletName("jaxrs")
                                .servletClass(Application.class.getName())
                                .createInitParam()
                                    .paramName(Application.class.getName())
                                    .paramValue(Application.class.getName())
                                .up()
                            .up()
                            .getOrCreateServletMapping()
                                .servletName("jaxrs")
                                .urlPattern("/api")
                            .up()
                            .exportAsString()));
    }

    @Test
    public void invokeWebService() throws Exception {
        final URI uri = new URI(url.toExternalForm() + "webservices/Bean");

        final HttpPost post = new HttpPost(uri);
        post.setEntity(new StringEntity("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <ns1:hello xmlns:ns1=\"http://wsrs.jaxws.tests.arquillian.openejb.apache.org/\"/>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>"));

        final HttpResponse response = client.execute(post);
        final String body = asString(response);

        final String expected = "" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<ns:helloResponse xmlns:ns=\"http://wsrs.jaxws.tests.arquillian.openejb.apache.org/\">" +
                "<return>hola</return>" +
                "</ns:helloResponse>" +
                "</soap:Body>" +
                "</soap:Envelope>";

        Assert.assertEquals(expected, body.replaceAll("ns[0-9]*", "ns"));
    }

    @Test
    public void invokeRest() throws Exception {
        final URI uri = new URI(url.toExternalForm() + "api/rest/bean");

        final HttpGet get = new HttpGet(uri);
        final HttpResponse response = client.execute(get);
        final String body = asString(response);

        Assert.assertEquals("hola", body);
    }

    public static String asString(final HttpResponse execute) throws IOException {
        final InputStream in = execute.getEntity().getContent();
        try {
            return IO.slurp(in);
        } finally {
            in.close();
        }
    }
}
