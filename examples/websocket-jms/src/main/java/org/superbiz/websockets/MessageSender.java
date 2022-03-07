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
 *
 */
package org.superbiz.websockets;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import java.util.logging.Logger;

@Singleton
@Startup
@Lock(LockType.READ)
public class MessageSender {

    private static final Logger LOG = Logger.getLogger(MessageSender.class.getName());

    @Inject
    private JMSContext jmsContext;

    @Resource(name = "messageReceived")
    private Topic messageReceived;

    @PostConstruct
    public void postConstruct() {
        LOG.info("Message sender started");
    }

    @PreDestroy
    public void preDestroy() {
        LOG.info("Message sender stopped");
    }

    public void send(final String message) {
        jmsContext.createProducer().send(messageReceived, message);
    }
}
