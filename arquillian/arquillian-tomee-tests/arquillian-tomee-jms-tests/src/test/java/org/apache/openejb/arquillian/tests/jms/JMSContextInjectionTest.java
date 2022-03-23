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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class JMSContextInjectionTest {

    @ArquillianResource
    private URL url;

    @Inject
    private JMSSenderBean senderBean;

    @Inject
    private MessageCounter messageCounter;

    @Resource
    private ConnectionFactory connectionFactory;

    @Deployment
    public static WebArchive getArchive() {
        return ShrinkWrap.create(WebArchive.class, "jms-context.war")
                .addClasses(JMSContextInjectionTest.class, JMSSenderBean.class, JMSReceiverBean.class, MessageCounter.class, XACancellingException.class);
    }

    @Test
    public void testShouldSendAndReceiveTwoHundredMessages() throws Exception {
        messageCounter.reset();
        for (int i = 0; i < 200; i++) {
            senderBean.sendToQueue("test", "Hello world");
        }
        int waitingCount = 0;
        while (senderBean.countMessagesInQueue("test") > 0 && waitingCount++ < 15) {
            Thread.sleep(10L);
        }
        if (waitingCount >= 15) {
            Assert.fail("Hit max wait time");
        }
        assertEquals(200, messageCounter.getValue());
    }

    @Test
    public void testTransactionShouldRollback() throws Exception {
        messageCounter.reset();
        boolean fail = true;
        try {
            senderBean.sendToQueue("test", "Hello world", true);
        } catch (XACancellingException e) {
            fail = false;
        }
        if (fail) {
            Assert.fail("Did not catch XACancellingException and we needed to");
        }
        Thread.sleep(100L);
        assertEquals(0, messageCounter.getValue());
    }
}
