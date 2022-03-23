/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.mdb;

import org.junit.Assert;
import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.stateful.BasicStatefulBusinessLocal;
import org.apache.openejb.test.stateful.BasicStatefulBusinessRemote;
import org.apache.openejb.test.stateful.BasicStatefulHome;
import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import org.apache.openejb.test.stateless.BasicStatelessBusinessRemote;
import org.apache.openejb.test.stateless.BasicStatelessHome;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.MessageDrivenContext;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnectionFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import javax.sql.DataSource;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "AnnotatedFieldInjectionMdb")})
public class AnnotatedFieldInjectionMdbBean implements MessageListener {
    @Resource
    private MessageDrivenContext mdbContext;
    @EJB(beanName = "BasicBmpBean")
    private BasicBmpHome bmpHome;
    @EJB(beanName = "BasicStatefulBean")
    private BasicStatefulHome statefulHome;
    @EJB(beanName = "BasicStatelessBean")
    private BasicStatelessHome statelessHome;
    @Resource
    private String striing = "1";
    @Resource
    private Double doouble = 1.0D;
    @Resource
    private Long loong = 1L;
    @Resource
    private Float flooat = 1.0F;
    @Resource
    private Integer inteeger = 1;
    @Resource
    private Short shoort = (short) 1;
    @Resource
    private Boolean booolean = true;
    @Resource
    private Byte byyte = (byte) 1;
    @Resource
    private Character chaaracter = 'D';
    @Resource
    private DataSource daataSource;
    @Resource
    private ConnectionFactory coonnectionFactory;
    @Resource
    private QueueConnectionFactory queueCoonnectionFactory;
    @Resource
    private TopicConnectionFactory topicCoonnectionFactory;
    @PersistenceUnit(unitName = "openjpa-test-unit")
    private EntityManagerFactory emf;
    @PersistenceContext(unitName = "openjpa-test-unit")
    private EntityManager em;
    @EJB
    private BasicStatelessBusinessLocal statelessBusinessLocal;
    @EJB
    private BasicStatelessBusinessRemote statelessBusinessRemote;
    @EJB
    private BasicStatefulBusinessLocal statefulBusinessLocal;
    @EJB
    private BasicStatefulBusinessRemote statefulBusinessRemote;

    private MdbInvoker mdbInvoker;

    @PostConstruct
    public void start() {
        try {
            mdbInvoker = new MdbInvoker(coonnectionFactory, this);
        } catch (final Exception e) {
            throw new EJBException(e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (null != mdbInvoker) {
            try {
                mdbInvoker.destroy();
            } catch (final Throwable e) {
                //Ignore
            }
        }
    }

    @Override
    public void onMessage(final Message message) {
        try {
//            System.out.println("\n" +
//                    "***************************************\n" +
//                    "Got message: " + message + "\n" +
//                    "***************************************\n\n");
            try {
                message.acknowledge();
            } catch (final JMSException e) {
                e.printStackTrace();
            }
            mdbInvoker.onMessage(message);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    public void lookupEntityBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", bmpHome);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statefulHome);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statelessHome);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statelessBusinessLocal);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statelessBusinessRemote);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statefulBusinessLocal);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statefulBusinessRemote);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException {
        try {
            final String expected = "1";
            Assert.assertNotNull("The String looked up is null", striing);
            Assert.assertEquals(expected, striing);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            final Double expected = 1.0D;

            Assert.assertNotNull("The Double looked up is null", doouble);
            Assert.assertEquals(expected, doouble);

        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            final Long expected = 1L;

            Assert.assertNotNull("The Long looked up is null", loong);
            Assert.assertEquals(expected, loong);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            final Float expected = 1.0F;

            Assert.assertNotNull("The Float looked up is null", flooat);
            Assert.assertEquals(expected, flooat);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            final Integer expected = 1;

            Assert.assertNotNull("The Integer looked up is null", inteeger);
            Assert.assertEquals(expected, inteeger);

        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            final Short expected = (short) 1;

            Assert.assertNotNull("The Short looked up is null", shoort);
            Assert.assertEquals(expected, shoort);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            final Boolean expected = true;

            Assert.assertNotNull("The Boolean looked up is null", booolean);
            Assert.assertEquals(expected, booolean);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            final Byte expected = (byte) 1;

            Assert.assertNotNull("The Byte looked up is null", byyte);
            Assert.assertEquals(expected, byyte);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException {
        try {
            final Character expected = 'D';

            Assert.assertNotNull("The Character looked up is null", chaaracter);
            Assert.assertEquals(expected, chaaracter);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            Assert.assertNotNull("The DataSource is null", daataSource);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupJMSConnectionFactory() throws TestFailureException {
        try {
            try {
                testJmsConnection(coonnectionFactory.createConnection());
                testJmsConnection(queueCoonnectionFactory.createConnection());
                testJmsConnection(topicCoonnectionFactory.createConnection());
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    private void testJmsConnection(final jakarta.jms.Connection connection) throws JMSException {
        final Session session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        final Topic topic = session.createTopic("test");
        final MessageProducer producer = session.createProducer(topic);
        producer.send(session.createMessage());
        producer.close();
        session.close();
        connection.close();
    }

    public void lookupPersistenceUnit() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManagerFactory is null", emf);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupPersistenceContext() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManager is null", em);

            try {
                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupMessageDrivenContext() throws TestFailureException {
        try {
            Assert.assertNotNull("The MessageDrivenContext is null", mdbContext);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }

    }
}
