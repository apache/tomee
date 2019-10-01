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

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
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
