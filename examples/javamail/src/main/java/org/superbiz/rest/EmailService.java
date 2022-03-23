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

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.Date;
import java.util.Properties;

@Path("/email")
public class EmailService {

    @POST
    public String lowerCase(final String message) {

        try {

            //Create some properties and get the default Session
            final Properties props = new Properties();
            props.put("mail.smtp.host", "your.mailserver.host");
            props.put("mail.debug", "true");

            final Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("MyUsername", "MyPassword");
                }
            });

            //Set this just to see some internal logging
            session.setDebug(true);

            //Create a message
            final MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("your@email.address"));
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
