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
package org.apache.openejb.activemq;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.jms.JMSConnectionFactoryDefinition;
import jakarta.jms.JMSConnectionFactoryDefinitions;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.JMSDestinationDefinitions;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SimpleLog
@RunWith(ApplicationComposer.class)
@Classes(innerClassesAsBean = true, cdi = true)
public class JMSDestinationTest {
    @Resource(name = "q")
    private Queue queue;

    @Resource(name = "t")
    private Topic topic;

    @Test
    public void created() throws JMSException {
        assertNotNull(queue);
        assertNotNull(topic);
        assertTrue(ActiveMQQueue.class.isInstance(queue));
        assertTrue(ActiveMQTopic.class.isInstance(topic));
        assertEquals("queuetest", ActiveMQQueue.class.cast(queue).getQueueName());
        assertEquals("topictest", ActiveMQTopic.class.cast(topic).getTopicName());
    }

    @JMSDestinationDefinitions({
            @JMSDestinationDefinition(name = "q", interfaceName = "jakarta.jms.Queue", resourceAdapter = "Default JMS Resource Adapter", properties = {"PhysicalName=queuetest"}),
            @JMSDestinationDefinition(name = "t", interfaceName = "jakarta.jms.Topic", resourceAdapter = "Default JMS Resource Adapter", properties = {"PhysicalName=topictest"})
    })
    @JMSConnectionFactoryDefinitions({
            @JMSConnectionFactoryDefinition(
                    name = "cf",
                    className = "org.apache.openejb.resource.activemq.ActiveMQResourceAdapter",
                    resourceAdapter = "Default JMS Resource Adapter",
                    interfaceName = "jakarta.jms.MessageListener"
            )
    })
    public static class Define {
    }
}
