/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jms;

import java.util.Enumeration;

import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;

@Singleton
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class JMSSenderBean {
    @Inject
    private JMSContext jmsContext;

    public void sendToQueue(final String queueName, final String message) {
        sendToQueue(queueName, message, false);
    }

    public void sendToQueue(final String queueName, final String message, final boolean rollback) {
        final Queue queue = jmsContext.createQueue(queueName);
        jmsContext.createProducer().send(queue, message);

        if (rollback) {
            throw new XACancellingException();
        }
    }

    public int countMessagesInQueue(final String queueName) throws Exception {
        final Queue queue = jmsContext.createQueue(queueName);
        final QueueBrowser browser = jmsContext.createBrowser(queue);
        final Enumeration<?> msgEnumeration = browser.getEnumeration();
        int count = 0;
        while (msgEnumeration.hasMoreElements()) {
            msgEnumeration.nextElement();
            count++;
        }
        return count;
    }
}
