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
package org.apache.tomee.itests.ejb;

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Topic;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@Lock(LockType.READ)
public class MessageSender {

    @Resource
    private ConnectionFactory cf;

    @Inject
    private JMSContext jmsContext;

    @Resource(name = "target")
    private Topic eventTopic;


    public void sendMessage() {
        final String message = "hello world";
        for (int i = 0; i < 1000; i++) {
            sendMessage(eventTopic, message);
        }
    }

    private void sendMessage(final Topic topic, final String message) {
        jmsContext.createProducer().send(topic, jmsContext.createTextMessage(message));
    }

}
