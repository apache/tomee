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

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDrivenBean;
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
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

public class SetterInjectionMdbBean implements EncMdbObject, MessageDrivenBean, MessageListener {
    private MessageDrivenContext ejbContextField;
    private BasicBmpHome bmpHomeField;
    private BasicStatefulHome statefulHomeField;
    private BasicStatelessHome statelessHomeField;
    private String striingField;
    private Double dooubleField;
    private Long loongField;
    private Float flooatField;
    private Integer inteegerField;
    private Short shoortField;
    private Boolean boooleanField;
    private Byte byyteField;
    private Character chaaracterField;
    private DataSource daataSourceField;
    private ConnectionFactory coonnectionFactory;
    private QueueConnectionFactory queueCoonnectionFactory;
    private TopicConnectionFactory topicCoonnectionFactory;
    private EntityManagerFactory emfField;
    private EntityManager emField;
    private EntityManager eemField;
    private EntityManager pemField;
    private BasicStatelessBusinessLocal statelessBusinessLocalField;
    private BasicStatelessBusinessRemote statelessBusinessRemoteField;
    private BasicStatefulBusinessLocal statefulBusinessLocalField;
    private BasicStatefulBusinessRemote statefulBusinessRemoteField;

    private MessageDrivenContext mdbContext = null;
    private MdbInvoker mdbInvoker;

    @Override
    public void setMessageDrivenContext(final MessageDrivenContext ctx) throws EJBException {
        this.mdbContext = ctx;
        try {
            final ConnectionFactory connectionFactory = (ConnectionFactory) new InitialContext().lookup("java:comp/env/jms");
            mdbInvoker = new MdbInvoker(connectionFactory, this);
        } catch (final Exception e) {
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
            mdbInvoker.onMessage(message);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    public BasicStatefulBusinessLocal getStatefulBusinessLocal() {
        return statefulBusinessLocalField;
    }

    public void setStatefulBusinessLocal(final BasicStatefulBusinessLocal statefulBusinessLocal) {
        this.statefulBusinessLocalField = statefulBusinessLocal;
    }

    public BasicStatefulBusinessRemote getStatefulBusinessRemote() {
        return statefulBusinessRemoteField;
    }

    public void setStatefulBusinessRemote(final BasicStatefulBusinessRemote statefulBusinessRemote) {
        this.statefulBusinessRemoteField = statefulBusinessRemote;
    }

    public BasicStatelessBusinessLocal getStatelessBusinessLocal() {
        return statelessBusinessLocalField;
    }

    public void setStatelessBusinessLocal(final BasicStatelessBusinessLocal statelessBusinessLocal) {
        this.statelessBusinessLocalField = statelessBusinessLocal;
    }

    public BasicStatelessBusinessRemote getStatelessBusinessRemote() {
        return statelessBusinessRemoteField;
    }

    public void setStatelessBusinessRemote(final BasicStatelessBusinessRemote statelessBusinessRemote) {
        this.statelessBusinessRemoteField = statelessBusinessRemote;
    }

    public BasicBmpHome getBmpHome() {
        return bmpHomeField;
    }

    public void setBmpHome(final BasicBmpHome bmpHome) {
        this.bmpHomeField = bmpHome;
    }

    public Boolean getBooolean() {
        return boooleanField;
    }

    public void setBooolean(final Boolean booolean) {
        this.boooleanField = booolean;
    }

    public Byte getByyte() {
        return byyteField;
    }

    public void setByyte(final Byte byyte) {
        this.byyteField = byyte;
    }

    public Character getChaaracter() {
        return chaaracterField;
    }

    public void setChaaracter(final Character chaaracter) {
        this.chaaracterField = chaaracter;
    }

    public DataSource getDaataSource() {
        return daataSourceField;
    }

    public void setDaataSource(final DataSource daataSource) {
        this.daataSourceField = daataSource;
    }

    public ConnectionFactory getCoonnectionFactory() {
        return coonnectionFactory;
    }

    public void setCoonnectionFactory(final ConnectionFactory coonnectionFactory) {
        this.coonnectionFactory = coonnectionFactory;
    }

    public QueueConnectionFactory getQueueCoonnectionFactory() {
        return queueCoonnectionFactory;
    }

    public void setQueueCoonnectionFactory(final QueueConnectionFactory queueCoonnectionFactory) {
        this.queueCoonnectionFactory = queueCoonnectionFactory;
    }

    public TopicConnectionFactory getTopicCoonnectionFactory() {
        return topicCoonnectionFactory;
    }

    public void setTopicCoonnectionFactory(final TopicConnectionFactory topicCoonnectionFactory) {
        this.topicCoonnectionFactory = topicCoonnectionFactory;
    }

    public Double getDoouble() {
        return dooubleField;
    }

    public void setDoouble(final Double doouble) {
        this.dooubleField = doouble;
    }

    public EntityManager getEem() {
        return eemField;
    }

    public void setEem(final EntityManager eem) {
        this.eemField = eem;
    }

    public MessageDrivenContext getEjbContext() {
        return ejbContextField;
    }

    public void setEjbContext(final MessageDrivenContext ejbContext) {
        this.ejbContextField = ejbContext;
    }

    public EntityManager getEm() {
        return emField;
    }

    public void setEm(final EntityManager em) {
        this.emField = em;
    }

    public EntityManagerFactory getEmf() {
        return emfField;
    }

    public void setEmf(final EntityManagerFactory emf) {
        this.emfField = emf;
    }

    public Float getFlooat() {
        return flooatField;
    }

    public void setFlooat(final Float flooat) {
        this.flooatField = flooat;
    }

    public Integer getInteeger() {
        return inteegerField;
    }

    public void setInteeger(final Integer inteeger) {
        this.inteegerField = inteeger;
    }

    public Long getLoong() {
        return loongField;
    }

    public void setLoong(final Long loong) {
        this.loongField = loong;
    }

    public EntityManager getPem() {
        return pemField;
    }

    public void setPem(final EntityManager pem) {
        this.pemField = pem;
    }

    public Short getShoort() {
        return shoortField;
    }

    public void setShoort(final Short shoort) {
        this.shoortField = shoort;
    }

    public BasicStatefulHome getStatefulHome() {
        return statefulHomeField;
    }

    public void setStatefulHome(final BasicStatefulHome statefulHome) {
        this.statefulHomeField = statefulHome;
    }

    public BasicStatelessHome getStatelessHome() {
        return statelessHomeField;
    }

    public void setStatelessHome(final BasicStatelessHome statelessHome) {
        this.statelessHomeField = statelessHome;
    }

    public String getStriing() {
        return striingField;
    }

    public void setStriing(final String striing) {
        this.striingField = striing;
    }

    public void ejbCreate() throws CreateException {
    }

    @Override
    public void lookupEntityBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", bmpHomeField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatefulBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statefulHomeField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatelessBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statelessHomeField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatelessBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statelessBusinessLocalField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatelessBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statelessBusinessRemoteField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatefulBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statefulBusinessLocalField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupStatefulBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statefulBusinessRemoteField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }


    @Override
    public void lookupStringEntry() throws TestFailureException {
        try {
            final String expected = "1";
            Assert.assertNotNull("The String looked up is null", striingField);
            Assert.assertEquals(expected, striingField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupDoubleEntry() throws TestFailureException {
        try {
            final Double expected = 1.0D;

            Assert.assertNotNull("The Double looked up is null", dooubleField);
            Assert.assertEquals(expected, dooubleField);

        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupLongEntry() throws TestFailureException {
        try {
            final Long expected = 1L;

            Assert.assertNotNull("The Long looked up is null", loongField);
            Assert.assertEquals(expected, loongField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupFloatEntry() throws TestFailureException {
        try {
            final Float expected = 1.0F;

            Assert.assertNotNull("The Float looked up is null", flooatField);
            Assert.assertEquals(expected, flooatField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupIntegerEntry() throws TestFailureException {
        try {
            final Integer expected = 1;

            Assert.assertNotNull("The Integer looked up is null", inteegerField);
            Assert.assertEquals(expected, inteegerField);

        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupShortEntry() throws TestFailureException {
        try {
            final Short expected = (short) 1;

            Assert.assertNotNull("The Short looked up is null", shoortField);
            Assert.assertEquals(expected, shoortField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupBooleanEntry() throws TestFailureException {
        try {
            final Boolean expected = true;

            Assert.assertNotNull("The Boolean looked up is null", boooleanField);
            Assert.assertEquals(expected, boooleanField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupByteEntry() throws TestFailureException {
        try {
            final Byte expected = (byte) 1;

            Assert.assertNotNull("The Byte looked up is null", byyteField);
            Assert.assertEquals(expected, byyteField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupCharacterEntry() throws TestFailureException {
        try {
            final Character expected = 'D';

            Assert.assertNotNull("The Character looked up is null", chaaracterField);
            Assert.assertEquals(expected, chaaracterField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupResource() throws TestFailureException {
        try {
            Assert.assertNotNull("The DataSource is null", daataSourceField);
        } catch (final AssertionFailedError afe) {
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

    @Override
    public void lookupPersistenceUnit() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManagerFactory is null", emfField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupPersistenceContext() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManager is null", emField);

            try {
                // call a do nothing method to assure entity manager actually exists
                emField.getFlushMode();
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    @Override
    public void lookupMessageDrivenContext() throws TestFailureException {
        try {
            Assert.assertNotNull("The MessageDrivenContext is null", mdbContext);
        } catch (final AssertionFailedError afe) {
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
