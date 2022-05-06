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

import jakarta.annotation.Resource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.Date;

@Path("/email")
public class EmailService {

    @Resource(mappedName = "java:comp/env/tomee/mail/exampleSMTP")
    private Session mailSession;

    @POST
    public String lowerCase(final String message) {

        try {

            /* Ensures that smtp authentication mechanism works as configured */
            boolean authenticate = "true".equals(mailSession.getProperty("mail.smtp.auth"));
            if (authenticate) {
                final String username = mailSession.getProperty("mail.smtp.user");
                final String password = mailSession.getProperty("mail.smtp.password");

                final URLName url = new URLName(
                        mailSession.getProperty("mail.transport.protocol"),
                        mailSession.getProperty("mail.smtp.host"), -1, null,
                        username, null);

                mailSession.setPasswordAuthentication(url, new PasswordAuthentication(username, password));
            } else {
                return "Using EMailService without SMTP auth configured. This might be valid, but could also be dangerous!";
            }

            //Set this just to see some internal logging
            mailSession.setDebug(true);

            //Create a message
            final MimeMessage msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress("admin@localhost")); //your e-mail address
            final InternetAddress[] address = {new InternetAddress("user@provider.com")};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject("JavaMail API test");
            msg.setSentDate(new Date());
            msg.setText(message, "UTF-8");

            Transport.send(msg);
        } catch (final MessagingException e) {
            return "Failed to send message: " + e.getMessage();
        }

        return "Sent";
    }
}
