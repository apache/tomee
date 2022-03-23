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


package org.apache.openejb.arquillian.tests.jms;

import jakarta.annotation.Resource;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import java.lang.IllegalStateException;
import java.util.List;

@Singleton
@Lock(LockType.READ)
public class MessageBean {

    @Resource
    private ConnectionFactory cf;

    @Resource(name = "red")
    private Topic red;

    @Resource(name = "blue")
    private Topic blue;

    @Resource(name = "nocolor")
    private Topic noColor;

    @Inject
    private Color color;

    public void callRed() {
        try {
            process(cf, red, "red", Session.SESSION_TRANSACTED);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void callBlue() {
        try {
            process(cf, blue, "blue", Session.SESSION_TRANSACTED);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void callNoColor() {
        try {
            process(cf, noColor, "nocolor", Session.SESSION_TRANSACTED);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void clear() {
        color.clear();
    }

    public List<String> getColors() {
        return color.getColors();
    }

    protected void process(final ConnectionFactory cf, final Topic topic, final String payload, final int acknowledgeMode) throws JMSException {
        Connection connection = null;
        Session session = null;

        try {
            connection = cf.createConnection();
            connection.start();

            session = connection.createSession(false, acknowledgeMode);
            final MessageProducer producer = session.createProducer(null);

            final TextMessage textMessage = session.createTextMessage(payload);
            producer.send(topic, textMessage);
        } finally {
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
    }


}
