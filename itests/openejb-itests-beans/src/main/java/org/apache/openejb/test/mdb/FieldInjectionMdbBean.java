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

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.stateful.BasicStatefulBusinessLocal;
import org.apache.openejb.test.stateful.BasicStatefulBusinessRemote;
import org.apache.openejb.test.stateful.BasicStatefulHome;
import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import org.apache.openejb.test.stateless.BasicStatelessBusinessRemote;
import org.apache.openejb.test.stateless.BasicStatelessHome;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

public class FieldInjectionMdbBean implements EncMdbObject, MessageDrivenBean, MessageListener {
    private MessageDrivenContext ejbContext;
    private BasicBmpHome bmpHome;
    private BasicStatefulHome statefulHome;
    private BasicStatelessHome statelessHome;
    private String striing;
    private Double doouble;
    private Long loong;
    private Float flooat;
    private Integer inteeger;
    private Short shoort;
    private Boolean booolean;
    private Byte byyte;
    private Character chaaracter;
    private DataSource daataSource;
    private ConnectionFactory coonnectionFactory;
    private QueueConnectionFactory queueCoonnectionFactory;
    private TopicConnectionFactory topicCoonnectionFactory;
    private EntityManagerFactory emf;
    private EntityManager em;
    private EntityManager eem;
    private EntityManager pem;
    private BasicStatelessBusinessLocal statelessBusinessLocal;
    private BasicStatelessBusinessRemote statelessBusinessRemote;
    private BasicStatefulBusinessLocal statefulBusinessLocal;
    private BasicStatefulBusinessRemote statefulBusinessRemote;


    private MessageDrivenContext mdbContext = null;
    private MdbInvoker mdbInvoker;

    @Override
    public void setMessageDrivenContext(final MessageDrivenContext ctx) throws EJBException {
        this.mdbContext = ctx;
        try {
            final ConnectionFactory connectionFactory = (ConnectionFactory) new InitialContext().lookup("java:comp/env/jms");
            mdbInvoker = new MdbInvoker(connectionFactory, this);
        } catch (Exception e) {
            throw new EJBException(e);
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
            } catch (JMSException e) {
                e.printStackTrace();
            }
            mdbInvoker.onMessage(message);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    @Override
    public void lookupEntityBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", bmpHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatefulBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statefulHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatelessBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statelessHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatelessBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statelessBusinessLocal);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatelessBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statelessBusinessRemote);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatefulBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statefulBusinessLocal);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatefulBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statefulBusinessRemote);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStringEntry() throws TestFailureException {
        try {
            final String expected = "1";
            Assert.assertNotNull("The String looked up is null", striing);
            Assert.assertEquals(expected, striing);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupDoubleEntry() throws TestFailureException {
        try {
            final Double expected = 1.0D;

            Assert.assertNotNull("The Double looked up is null", doouble);
            Assert.assertEquals(expected, doouble);

        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupLongEntry() throws TestFailureException {
        try {
            final Long expected = 1L;

            Assert.assertNotNull("The Long looked up is null", loong);
            Assert.assertEquals(expected, loong);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupFloatEntry() throws TestFailureException {
        try {
            final Float expected = 1.0F;

            Assert.assertNotNull("The Float looked up is null", flooat);
            Assert.assertEquals(expected, flooat);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupIntegerEntry() throws TestFailureException {
        try {
            final Integer expected = 1;

            Assert.assertNotNull("The Integer looked up is null", inteeger);
            Assert.assertEquals(expected, inteeger);

        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupShortEntry() throws TestFailureException {
        try {
            final Short expected = (short) 1;

            Assert.assertNotNull("The Short looked up is null", shoort);
            Assert.assertEquals(expected, shoort);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupBooleanEntry() throws TestFailureException {
        try {
            final Boolean expected = true;

            Assert.assertNotNull("The Boolean looked up is null", booolean);
            Assert.assertEquals(expected, booolean);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupByteEntry() throws TestFailureException {
        try {
            final Byte expected = (byte) 1;

            Assert.assertNotNull("The Byte looked up is null", byyte);
            Assert.assertEquals(expected, byyte);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupCharacterEntry() throws TestFailureException {
        try {
            final Character expected = 'D';

            Assert.assertNotNull("The Character looked up is null", chaaracter);
            Assert.assertEquals(expected, chaaracter);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupResource() throws TestFailureException {
        try {
            Assert.assertNotNull("The DataSource is null", daataSource);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupJMSConnectionFactory() throws TestFailureException {
        try {
            try {
                testJmsConnection(coonnectionFactory.createConnection());
                testJmsConnection(queueCoonnectionFactory.createConnection());
                testJmsConnection(topicCoonnectionFactory.createConnection());
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    private void testJmsConnection(final javax.jms.Connection connection) throws JMSException {
        final Session session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        final Topic topic = session.createTopic("test");
        final MessageProducer producer = session.createProducer(topic);
        producer.send(session.createMessage());
        producer.close();
        session.close();
        connection.close();
    }

    @Override
    public void lookupPersistenceUnit() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManagerFactory is null", emf);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupPersistenceContext() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManager is null", em);

            try {
                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupMessageDrivenContext() throws TestFailureException {
        try {
            Assert.assertNotNull("The MessageDrivenContext is null", ejbContext);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }

    }

    @Override
    public void ejbRemove() throws EJBException {

        if (null != mdbInvoker) {
            mdbInvoker.destroy();
        }
    }
}
