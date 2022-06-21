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
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.NetworkUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
public class EmailServiceTest {

    private static final int SMTP_TEST_PORT = NetworkUtil.getNextAvailablePort();

    private static final String USER_PASSWORD = "s3cr3t";
    private static final String USER_NAME = "admin@localhost";
    private static final String EMAIL_USER_ADDRESS = "admin@localhost";

    private static GreenMail mailServer;
    private static CountDownLatch started = new CountDownLatch(1);

    @Module
    @Classes(EmailService.class)
    public WebApp app() {
        return new WebApp().contextRoot("test");
    }

    @Configuration
    public Properties config() {
        //Note: We can also configure this via a resource.xml or via tomee.xml
        Properties properties = new Properties();
        properties.put("tomee/mail/mySMTP", "new://Resource?type=jakarta.mail.Session");
        properties.put("tomee/mail/mySMTP.mail.debug", "false");
        properties.put("tomee/mail/mySMTP.mail.transport.protocol", "smtp");
        properties.put("tomee/mail/mySMTP.mail.smtp.host", "localhost");
        properties.put("tomee/mail/mySMTP.mail.smtp.port", SMTP_TEST_PORT);
        properties.put("tomee/mail/mySMTP.mail.smtp.auth", "true");
        properties.put("tomee/mail/mySMTP.mail.smtp.user", USER_NAME);
        properties.put("tomee/mail/mySMTP.password", USER_PASSWORD);
        return properties;
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
        final String message = WebClient.create("http://localhost:4204").path("/test/email/").post("Hello TomEE", String.class);
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
