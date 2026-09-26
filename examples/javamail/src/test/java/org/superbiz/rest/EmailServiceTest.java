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
package org.superbiz.rest;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.util.NetworkUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class EmailServiceTest {

    private static final int SMTP_TEST_PORT = NetworkUtil.getNextAvailablePort();

    private static final String USER_PASSWORD = "s3cr3t";
    private static final String USER_NAME = "admin@localhost";
    private static final String EMAIL_USER_ADDRESS = "admin@localhost";

    private static GreenMail mailServer;
    private static CountDownLatch started = new CountDownLatch(1);

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive app() {
        //Note: We can also configure this via tomee.xml
        return ShrinkWrap.create(WebArchive.class)
                .addClass(EmailService.class)
                .addAsWebInfResource(new StringAsset(
                        "<resources>\n" +
                        "  <Resource id=\"tomee/mail/mySMTP\" type=\"jakarta.mail.Session\">\n" +
                        "    mail.debug = false\n" +
                        "    mail.transport.protocol = smtp\n" +
                        "    mail.smtp.host = localhost\n" +
                        "    mail.smtp.port = " + SMTP_TEST_PORT + "\n" +
                        "    mail.smtp.auth = true\n" +
                        "    mail.smtp.user = " + USER_NAME + "\n" +
                        "    password = " + USER_PASSWORD + "\n" +
                        "  </Resource>\n" +
                        "</resources>"), "resources.xml");
    }

    @BeforeClass
    public static void setUp() throws InterruptedException {
        mailServer = new CustomGreenMailServer(new ServerSetup(SMTP_TEST_PORT, null, "smtp"));
        mailServer.start();

        //wait for the server startup...
        started.await();

        // create user on mail server
        mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
    }

    @AfterClass
    public static void tearDown() {
        if (mailServer != null) {
            mailServer.stop();
        }
    }

    @Test
    public void post() throws IOException {
        final String message = WebClient.create(base.toExternalForm()).path("email/").post("Hello TomEE", String.class);
        assertEquals("Sent", message);
    }

    public static class CustomGreenMailServer extends GreenMail {

        public CustomGreenMailServer(ServerSetup config) {
            super(new ServerSetup[]{config});
        }

        public synchronized void start() {
            super.start();
            started.countDown();
        }
    }
}
