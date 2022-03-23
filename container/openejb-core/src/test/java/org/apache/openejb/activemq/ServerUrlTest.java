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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.resource.ResourceException;
import java.util.Properties;

@SimpleLog
@RunWith(ApplicationComposer.class)
@Classes(cdi = true, innerClassesAsBean = true)
public class ServerUrlTest {

    @EJB
    private ConnectionTestBean testBean;

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
                .p("amq", "new://Resource?type=ActiveMQResourceAdapter")
                .p("amq.BrokerXmlConfig", "broker:(vm://broker)?useJmx=true")
                .p("amq.ServerUrl", "vm://broker")
                .p("amqcf", "new://Resource?type=jakarta.jms.ConnectionFactory")
                .p("amqcf.ResourceAdapter", "amq")

                .build();
    }

    @Test
    public void test() throws Exception {
        try {
            testBean.testConnection();
            Assert.fail("Expected exception not thrown");
        } catch (JMSException e) {
            Assert.assertTrue(e.getMessage().contains("Broker named 'broker' does not exist"));
        }
    }

    @Singleton
    public static class ConnectionTestBean {

        @Resource
        private ConnectionFactory cf;

        public void testConnection() throws Exception {
            final Connection connection = cf.createConnection();
            connection.close();
        }
    }
}
