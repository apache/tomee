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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.arquillian.tests.jms;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;


@RunWith(Arquillian.class)
public class MDBWithWildcardTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getArchive() {

        return ShrinkWrap.create(WebArchive.class, "jsf-jms-test.war")
                .addClasses(WildcardMdb.class,
                        /* For some reason, we need to include the test here otherwise deployment fails with NoClassDefFoundError */
                        MDBWithWildcardTest.class)
                .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
                        .version("3.0")
                        .createServlet()
                            .servletName("jmx")
                            .servletClass(JmxServlet.class.getName())
                        .up()
                        .createServletMapping()
                            .servletName("jmx")
                            .urlPattern("/jmx")
                        .up()
                        .exportAsString()));
    }

    @Test
    public void test() throws Exception {
        final URL url = new URL(this.url.toExternalForm() + "/jmx");
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(200, urlConnection.getResponseCode());
        final InputStream inputStream = urlConnection.getInputStream();
        final String result = IO.slurp(inputStream);
        Assert.assertEquals("dest.*.event", result.trim());
    }


    @MessageDriven(
            activationConfig = {
                    @ActivationConfigProperty(propertyName = "destination", propertyValue = "dest.*.event"),
                    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
            }
    )
    public static class WildcardMdb implements MessageListener {
        @Override
        public void onMessage(final Message message) {
            System.out.println("Message received");
        }
    }

    public static class JmxServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            try {
                final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
                final String name = "openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=Resource,name=\"dest.\\*.event\"";
                final PrintWriter writer = resp.getWriter();
                writer.println(platformMBeanServer.getAttribute(new ObjectName(name), "physicalName"));
                writer.flush();
            } catch (Throwable t) {
                throw new ServletException(t);
            }
        }
    }
}
