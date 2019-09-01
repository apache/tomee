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
package org.apache.openejb.arquillian.tests.jms;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@Ignore // we know these are failing
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

    @Deployment(testable = false)
    public static WebArchive getArchive() {

        return ShrinkWrap.create(WebArchive.class, "jms-context.war")
                .addClasses(JMSSenderBean.class, JMSReceiverBean.class, MessageCounter.class);
    }

    @Test
    public void testShouldSendAndReceiveTwoHundredMessages() throws Exception {
        messageCounter.reset();

        for (int i = 0; i < 200; i++) {
            senderBean.sendToQueue("test", "Hello world");
        }

        assertEquals(200, messageCounter.getValue());
    }

    @Test
    public void testTransactionShouldRollback() throws Exception {
        messageCounter.reset();

        try {
            senderBean.sendToQueue("test", "Hello world", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(0, messageCounter.getValue());
    }


}
