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


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerRegistry;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.TransAttribute;
import org.junit.Assert;
import org.junit.Test;

import jakarta.annotation.Resource;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.List;
import java.util.Properties;

public class ConnectionFactoryTxTest {

    @Test
    public void testTxSupportNoneBeanTransAttributeMandatoryRequiredNoRollback() throws Exception {
        runTest("none", TransAttribute.MANDATORY, false);
        checkQueue(0); // method doesn't run at all
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeMandatoryRequiredWithRollback() throws Exception {
        runTest("none", TransAttribute.MANDATORY, true);
        checkQueue(0); // method doesn't run at all
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeRequiredRequiredNoRollback() throws Exception {
        runTest("none", TransAttribute.REQUIRED, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeRequiredRequiredWithRollback() throws Exception {
        runTest("none", TransAttribute.REQUIRED, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeRequires_newRequiredNoRollback() throws Exception {
        runTest("none", TransAttribute.REQUIRES_NEW, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeRequires_newRequiredWithRollback() throws Exception {
        runTest("none", TransAttribute.REQUIRES_NEW, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeSupportsRequiredNoRollback() throws Exception {
        runTest("none", TransAttribute.SUPPORTS, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeSupportsRequiredWithRollback() throws Exception {
        runTest("none", TransAttribute.SUPPORTS, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeNot_supportedRequiredNoRollback() throws Exception {
        runTest("none", TransAttribute.NOT_SUPPORTED, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeNot_supportedRequiredWithRollback() throws Exception {
        runTest("none", TransAttribute.NOT_SUPPORTED, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeNeverRequiredNoRollback() throws Exception {
        runTest("none", TransAttribute.NEVER, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportNoneBeanTransAttributeNeverRequiredWithRollback() throws Exception {
        runTest("none", TransAttribute.NEVER, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeMandatoryRequiredNoRollback() throws Exception {
        runTest("local", TransAttribute.MANDATORY, false);
        checkQueue(0); // No tx present, so this this shouldn't process
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeMandatoryRequiredWithRollback() throws Exception {
        runTest("local", TransAttribute.MANDATORY, true);
        checkQueue(0); // No tx present, so this this shouldn't process
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeRequiredRequiredNoRollback() throws Exception {
        runTest("local", TransAttribute.REQUIRED, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeRequiredRequiredWithRollback() throws Exception {
        runTest("local", TransAttribute.REQUIRED, true);
        checkQueue(0);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeRequires_newRequiredNoRollback() throws Exception {
        runTest("local", TransAttribute.REQUIRES_NEW, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeRequires_newRequiredWithRollback() throws Exception {
        runTest("local", TransAttribute.REQUIRES_NEW, true);
        checkQueue(0);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeSupportsRequiredNoRollback() throws Exception {
        runTest("local", TransAttribute.SUPPORTS, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeSupportsRequiredWithRollback() throws Exception {
        runTest("local", TransAttribute.SUPPORTS, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeNot_supportedRequiredNoRollback() throws Exception {
        runTest("local", TransAttribute.NOT_SUPPORTED, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeNot_supportedRequiredWithRollback() throws Exception {
        runTest("local", TransAttribute.NOT_SUPPORTED, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeNeverRequiredNoRollback() throws Exception {
        runTest("local", TransAttribute.NEVER, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportLocalBeanTransAttributeNeverRequiredWithRollback() throws Exception {
        runTest("local", TransAttribute.NEVER, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeMandatoryRequiredNoRollback() throws Exception {
        runTest("xa", TransAttribute.MANDATORY, false);
        checkQueue(0); // No tx present, so this this shouldn't process
    }

    @Test
    public void testTxSupportXaBeanTransAttributeMandatoryRequiredWithRollback() throws Exception {
        runTest("xa", TransAttribute.MANDATORY, true);
        checkQueue(0); // No tx present, so this this shouldn't process
    }

    @Test
    public void testTxSupportXaBeanTransAttributeRequiredRequiredNoRollback() throws Exception {
        runTest("xa", TransAttribute.REQUIRED, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeRequiredRequiredWithRollback() throws Exception {
        runTest("xa", TransAttribute.REQUIRED, true);
        checkQueue(0);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeRequires_newRequiredNoRollback() throws Exception {
        runTest("xa", TransAttribute.REQUIRES_NEW, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeRequires_newRequiredWithRollback() throws Exception {
        runTest("xa", TransAttribute.REQUIRES_NEW, true);
        checkQueue(0);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeSupportsRequiredNoRollback() throws Exception {
        runTest("xa", TransAttribute.SUPPORTS, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeSupportsRequiredWithRollback() throws Exception {
        runTest("xa", TransAttribute.SUPPORTS, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeNot_supportedRequiredNoRollback() throws Exception {
        runTest("xa", TransAttribute.NOT_SUPPORTED, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeNot_supportedRequiredWithRollback() throws Exception {
        runTest("xa", TransAttribute.NOT_SUPPORTED, true);
        checkQueue(1);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeNeverRequiredNoRollback() throws Exception {
        runTest("xa", TransAttribute.NEVER, false);
        checkQueue(1);
    }

    @Test
    public void testTxSupportXaBeanTransAttributeNeverRequiredWithRollback() throws Exception {
        runTest("xa", TransAttribute.NEVER, true);
        checkQueue(1);
    }
    private void checkQueue(final int expected) throws Exception {
        final BrokerService broker = BrokerRegistry.getInstance().lookup("localhost");
        final org.apache.activemq.broker.region.Destination testQueue = broker.getDestination(new ActiveMQQueue("TEST"));

        Assert.assertEquals(expected, testQueue.browse().length);
    }

    private void runTest(final String transactionSupport, final TransAttribute transactionAttribute, final boolean rollback) throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();

        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));


        final Properties raProps = new Properties();
        raProps.setProperty("BrokerXmlConfig", "broker:(vm://broker)?useJmx=true");
        raProps.setProperty("ServerUrl", "vm://localhost");
        assembler.createResource(config.configureService(ResourceInfo.class, "MyJmsResourceAdapter",
                raProps, "Default JMS Resource Adapter", "ActiveMQResourceAdapter"));

        final Properties cfProps = new Properties();
        cfProps.setProperty("ResourceAdapter", "MyJmsResourceAdapter");
        cfProps.setProperty("TransactionSupport", transactionSupport);
        assembler.createResource(config.configureService(ResourceInfo.class, "MyJmsConnectionFactory",
                cfProps, "Default JMS Connection Factory", "jakarta.jms.ConnectionFactory"));

        final EjbJar ejbJar = new EjbJar("tx-singleton");
        final SingletonBean singletonBean = new SingletonBean(TxSingletonBean.class);
        ejbJar.addEnterpriseBean(singletonBean);

        final List<ContainerTransaction> declared = ejbJar.getAssemblyDescriptor().getContainerTransaction();
        declared.add(new ContainerTransaction(transactionAttribute, "*", "*", "*"));

        assembler.createApplication(config.configureApplication(ejbJar));

        // explicitly clear this thing out
        consumeMessagesFromQueue(BrokerRegistry.getInstance().lookup("localhost").getVmConnectorURI().toString(),"TEST");

        final TxSingletonBean bean = (TxSingletonBean) assembler.getContainerSystem().getJNDIContext().lookup("java:global/tx-singleton/tx-singleton/TxSingletonBean");
        try {
            bean.sendMessage(rollback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void consumeMessagesFromQueue(final String brokerUrl, final String queueName) throws Exception {
        final ConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl);
        final Connection conn = cf.createConnection();
        conn.start();

        final Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Queue queue = session.createQueue(queueName);
        final MessageConsumer consumer = session.createConsumer(queue);

        while (consumer.receive(1000) != null) {
        }

        consumer.close();
        session.close();
        conn.close();
    }

    public static class TxSingletonBean {

        @Resource
        private ConnectionFactory cf;

        public void sendMessage(final boolean fail) throws Exception {
            try (final Connection connection = cf.createConnection(); final Session sess = connection.createSession(false, jakarta.jms.Session.AUTO_ACKNOWLEDGE)) {
                connection.start();

                final Queue queue = sess.createQueue("TEST");
                final MessageProducer producer = sess.createProducer(queue);
                final TextMessage message = sess.createTextMessage("Test Message");

                producer.send(message);

                if (fail) {
                    throw new RuntimeException("Operation failing");
                }
            }
        }

    }
}

