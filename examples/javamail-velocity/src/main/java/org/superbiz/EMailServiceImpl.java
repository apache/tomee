/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

@Stateless
public class EMailServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMailServiceImpl.class);

    private static final String HEADER_HTML_EMAIL = "text/html; charset=UTF-8";
    private static final String TEMPLATE_DIRECTORY = "templates/";
    private static final String VELOCITY_RESOURCE_CLASS_LOADER_KEY = "resource.loader.class.class";
    private static final String VELOCITY_RESOURCE_CLASS_LOADER = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
    private static final String VELOCITY_RESOURCE_LOADER_KEY = "resource.loaders";
    private static final String VELOCITY_RESOURCE_LOADER = "class";

    @Resource(mappedName = "java:comp/env/tomee/mail/exampleSMTP")
    private Session mailSession;

    private VelocityEngine velocityEngine;

    @PostConstruct
    public void init() {
        // Properties documented here: https://wiki.apache.org/velocity/VelocityAndWeblogic
        final Properties prop = new Properties();
        prop.setProperty(VELOCITY_RESOURCE_LOADER_KEY, VELOCITY_RESOURCE_LOADER);
        prop.setProperty(VELOCITY_RESOURCE_CLASS_LOADER_KEY, VELOCITY_RESOURCE_CLASS_LOADER);

        velocityEngine = new VelocityEngine();
        velocityEngine.init(prop);

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
            LOGGER.warn("Using EMailService without SMTP auth configured. This might be valid, but could also be dangerous!");
        }

    }

    public void sendMail(EMail eMail, String htmlTemplate, Map<String, String> templateResources) {
        if (!eMail.getMailType().equals(MailType.MAIL_HTML)) {
            throw new RuntimeException("You can't send an HTML eMail with the Mail instance provided: '" + eMail.getMailType().toString() + "'!");
        } else {
            htmlTemplate = TEMPLATE_DIRECTORY + htmlTemplate;
            try {
                MimeMessage message = createMimeMessage(eMail);

                if (!velocityEngine.resourceExists(htmlTemplate)) {
                    throw new RuntimeException("Could not find the given email template '" + htmlTemplate + "' in the classpath.");
                } else {
                    final Template template = velocityEngine.getTemplate(htmlTemplate);
                    final VelocityContext velocityContext = new VelocityContext();
                    for (Map.Entry<String, String> templateEntry : templateResources.entrySet()) {
                        velocityContext.put(templateEntry.getKey(), templateEntry.getValue());
                    }
                    final StringWriter stringWriter = new StringWriter();
                    template.merge(velocityContext, stringWriter);
                    // setting the eMail's content as HTML mail body
                    final Multipart mp = new MimeMultipart();
                    final MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(stringWriter.toString(), HEADER_HTML_EMAIL);
                    mp.addBodyPart(htmlPart);
                    message.setContent(mp);

                    Transport.send(message);
                    // mark this eMail as sent with the current date
                    eMail.setSentDate(new Date());
                }

            } catch (MessagingException ex) {
                LOGGER.warn("Could not send template HTML eMail: {}", ex.getLocalizedMessage());
                throw new RuntimeException(ex.getLocalizedMessage(), ex);
            }
        }
    }

    private MimeMessage createMimeMessage(EMail eMail) throws MessagingException {
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(eMail.getMailFrom()));
        for (String mailTo : eMail.getMailTo()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
        }

        message.setSubject(eMail.getMailSubject());
        message.setSentDate(new Date());

        for (String ccRecipient : eMail.getMailCc()) {
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccRecipient));
        }
        for (String bccRecipient : eMail.getMailBcc()) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccRecipient));
        }
        return message;
    }

    @PreDestroy
    public void close() {
        if (mailSession != null) {
            mailSession = null;
        }
    }
}