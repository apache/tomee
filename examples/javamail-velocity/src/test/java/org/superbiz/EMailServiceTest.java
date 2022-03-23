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
package org.superbiz;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.NetworkUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

@RunWithApplicationComposer
public class EMailServiceTest {

    private static final int SMTP_TEST_PORT = NetworkUtil.getNextAvailablePort();
    private static final String USER_PASSWORD = "s3cr3t";
    private static final String USER_NAME = "admin@localhost";
    private static final String EMAIL_USER_ADDRESS = "admin@localhost";

    private static GreenMail mailServer;
    private static CountDownLatch started = new CountDownLatch(1);

    @Module
    @Classes(cdi = true, value = {EMailServiceImpl.class})
    public EjbJar beans() {
        return new EjbJar("javamail-velocity");
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

    @Inject
    private EMailServiceImpl eMailService;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        mailServer = new CustomGreenMailServer(new ServerSetup(SMTP_TEST_PORT, null, "smtp"));
        mailServer.start();

        //wait for the server startup...
        started.await();

        // create user on mail server
        mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
    }

    @AfterAll
    public static void tearDown() {
        if (mailServer != null) {
            mailServer.stop();
        }
    }

    @Test
    public void testSendMailHTMLTemplate() throws Exception {
        // prepare
        String eMailTemplateName = "email-html-template.vm";
        Map<String, String> mailTemplateProps = new HashMap<>();
        mailTemplateProps.put("name", "Jon Doe");

        String fromMail = "admin@localhost";
        String toEmail = "john@localhost.com";
        String subject = "Template HTML email!";

        Collection<String> toRecipients = new ArrayList<>();
        toRecipients.add(toEmail);

        EMail eMail = new EMail(MailType.MAIL_HTML,toRecipients, subject, "", Collections.emptyList(),Collections.emptyList());
        eMail.setMailFrom(fromMail);
        // test
        assertNull(eMail.getSentDate());
        eMailService.sendMail(eMail, eMailTemplateName,  mailTemplateProps);
        assertNotNull(eMail.getSentDate());

        // fetch messages from server
        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(1, messages.length);
        MimeMessage msg = messages[0];
        assertTrue(msg.getContentType().contains("multipart/mixed;"));

        assertEquals(subject, msg.getSubject());

        MimeMultipart message = (MimeMultipart) msg.getContent();
        BodyPart bodyPart = message.getBodyPart(0);
        assertEquals("text/html; charset=UTF-8", bodyPart.getHeader("Content-Type")[0]);
        String receivedMailContent = String.valueOf(bodyPart.getContent());

        assertTrue(receivedMailContent.contains("Dear Jon Doe"));
        assertTrue(receivedMailContent.contains("templated"));
        assertEquals(fromMail, msg.getFrom()[0].toString());
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