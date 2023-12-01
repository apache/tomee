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
package org.apache.openejb.arquillian.tests.tomcat;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp25.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import org.junit.Assert;

@RunWith(Arquillian.class)
public class TrailersTest {

    @ArquillianResource(SwallowBodyServlet.class)
    private URL url;

    @Deployment(testable = false)
    public static WebArchive war() {

        final WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class);
        descriptor
                .createServlet()
                .servletName("swallow")
                .servletClass(SwallowBodyServlet.class.getName())
                .up()
                .createServletMapping()
                .servletName("swallow")
                .urlPattern("/swallow")
                .up()
                .createServlet()
                .servletName("noswallow")
                .servletClass(NoSwallowBodyServlet.class.getName())
                .up()
                .createServletMapping()
                .servletName("noswallow")
                .urlPattern("/noswallow")
                .up();

        return ShrinkWrap.create(WebArchive.class, "ROOT.war")
                .addClasses(SwallowBodyServlet.class, NoSwallowBodyServlet.class, TestServlet.class)
                .setWebXML(new StringAsset(descriptor.exportAsString()));
    }


    @Test
    public void testTrailerHeaderNameNotTokenThrowException() throws Exception {
        doTestTrailerHeaderNameNotToken(false);
    }

    @Test
    public void testTrailerHeaderNameNotTokenSwallowException() throws Exception {
        doTestTrailerHeaderNameNotToken(true);
    }

    private void doTestTrailerHeaderNameNotToken(boolean swallowException) throws Exception {
        final String launchProfile = System.getProperty("arquillian.launch");
        if ("tomee-embedded".equals(launchProfile)) {
            System.out.println("Skipping this test in TomEE embedded");
            return;
        }

        final String path = (swallowException) ? "/swallow" : "/noswallow";

        final String[] request = new String[]{
                "POST " + path + " HTTP/1.1" + SimpleHttpClient.CRLF +
                        "Host: localhost" + SimpleHttpClient.CRLF +
                        "Transfer-encoding: chunked" + SimpleHttpClient.CRLF +
                        "Content-Type: application/x-www-form-urlencoded" + SimpleHttpClient.CRLF +
                        "Connection: close" + SimpleHttpClient.CRLF +
                        SimpleHttpClient.CRLF +
                        "3" + SimpleHttpClient.CRLF +
                        "a=0" + SimpleHttpClient.CRLF +
                        "4" + SimpleHttpClient.CRLF +
                        "&b=1" + SimpleHttpClient.CRLF +
                        "0" + SimpleHttpClient.CRLF +
                        "x@trailer: Test" + SimpleHttpClient.CRLF +
                        SimpleHttpClient.CRLF };

        TrailerClient client = new TrailerClient(url.getPort());
        client.setRequest(request);

        client.connect();
        client.processRequest();
        // Expected to fail because of invalid trailer header name
        Assert.assertTrue(client.getResponseLine(), client.isResponse400());
    }

    public static abstract class TestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        private final boolean swallowException;

        public TestServlet(boolean swallowException) {
            this.swallowException = swallowException;
        }

        @Override
        protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            resp.setContentType("text/plain");
            final PrintWriter pw = resp.getWriter();

            // Read the body
            final InputStream is = req.getInputStream();
            try {
                while (is.read() > -1) {
                }
                pw.write("OK");
            } catch (IOException ioe) {
                if (!swallowException) {
                    throw ioe;
                }
            }
        }
    }

    public static class SwallowBodyServlet extends TestServlet {
        public SwallowBodyServlet() {
            super(true);
        }
    }

    public static class NoSwallowBodyServlet extends TestServlet {
        public NoSwallowBodyServlet() {
            super(false);
        }
    }

    public static class TrailerClient extends SimpleHttpClient {

        public TrailerClient(int port) {
            setPort(port);
        }

        @Override
        public boolean isResponseBodyOK() {
            return getResponseBody().contains("TestTestTest");
        }
    }

}
