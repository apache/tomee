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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.core;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import java.util.Map;
import java.util.Properties;

public class MailSessionFactory {
    private final Properties properties = new Properties();
    private boolean useDefault;

    public Session create() {
        final String password = properties.getProperty("password");

        Authenticator auth = null;
        if (password != null) {
            final String protocol = properties.getProperty("mail.transport.protocol", "smtp");

            String user = properties.getProperty("mail." + protocol + ".user");
            if (user == null) {
                user = properties.getProperty("mail.user");
            }

            if (user != null) {
                final PasswordAuthentication pa = new PasswordAuthentication(user, password);
                auth = new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return pa;
                    }
                };
            }
        }

        if (useDefault) {
            if (auth != null) {
                return Session.getDefaultInstance(properties, auth);
            }
            return Session.getDefaultInstance(properties);
        }

        if (auth != null) {
            return Session.getInstance(properties, auth);
        }
        return Session.getInstance(properties);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties.clear();
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                this.properties.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setUseDefault(final boolean useDefault) {
        this.useDefault = useDefault;
    }
}
