/**
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
package org.apache.openejb.test.stateful;

import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.stateless.BasicStatelessHome;
import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import org.apache.openejb.test.stateless.BasicStatelessBusinessRemote;
import org.apache.openejb.test.stateless.BasicStatelessPojoBean;
import org.apache.openejb.test.TestFailureException;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.MessageProducer;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.TopicConnectionFactory;
import jakarta.jms.JMSException;

import org.junit.Assert;
import junit.framework.AssertionFailedError;

import java.rmi.RemoteException;

public class SetterInjectionStatefulBean implements SessionBean {


    private String name;
    private SessionContext ejbContextField;
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
    private BasicStatefulPojoBean statefulBusinessLocalBeanField;
    private BasicStatelessPojoBean statelessBusinessLocalBeanField;

    public BasicStatefulBusinessLocal getStatefulBusinessLocal() {
        return statefulBusinessLocalField;
    }

    public void setStatefulBusinessLocal(final BasicStatefulBusinessLocal statefulBusinessLocal) {
        this.statefulBusinessLocalField = statefulBusinessLocal;
    }

    public BasicStatefulPojoBean getStatefulBusinessLocalBean() {
        return statefulBusinessLocalBeanField;
    }

    public void setStatefulBusinessLocalBean(final BasicStatefulPojoBean statefulBusinessLocalBean) {
        this.statefulBusinessLocalBeanField = statefulBusinessLocalBean;
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

    public BasicStatelessPojoBean getStatelessBusinessLocalBean() {
        return statelessBusinessLocalBeanField;
    }

    public void setStatelessBusinessLocalBean(final BasicStatelessPojoBean statelessBusinessLocalBean) {
        this.statelessBusinessLocalBeanField = statelessBusinessLocalBean;
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

    public SessionContext getEjbContext() {
        return ejbContextField;
    }

    public void setEjbContext(final SessionContext ejbContext) {
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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
    //=============================
    // Home interface methods
    //

    /**
     * Maps to EncStatefulHome.create
     *
     * @param name
     * @throws jakarta.ejb.CreateException
     * @see EncStatefulHome#create
     */
    public void ejbCreate(final String name) throws CreateException {
        this.name = name;
    }
    //
    // Home interface methods
    //=============================

    //=============================
    // Remote interface methods
    //

    public void lookupEntityBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", bmpHomeField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statefulHomeField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statelessHomeField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statelessBusinessLocalField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocalBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocalBean is null", statelessBusinessLocalBeanField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statelessBusinessRemoteField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statefulBusinessLocalField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocalBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocalBean is null", statefulBusinessLocalBeanField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statefulBusinessRemoteField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException {
        try {
            final String expected = new String("1");
            Assert.assertNotNull("The String looked up is null", striingField);
            Assert.assertEquals(expected, striingField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            final Double expected = new Double(1.0D);

            Assert.assertNotNull("The Double looked up is null", dooubleField);
            Assert.assertEquals(expected, dooubleField);

        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            final Long expected = new Long(1L);

            Assert.assertNotNull("The Long looked up is null", loongField);
            Assert.assertEquals(expected, loongField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            final Float expected = new Float(1.0F);

            Assert.assertNotNull("The Float looked up is null", flooatField);
            Assert.assertEquals(expected, flooatField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            final Integer expected = new Integer(1);

            Assert.assertNotNull("The Integer looked up is null", inteegerField);
            Assert.assertEquals(expected, inteegerField);

        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            final Short expected = new Short((short) 1);

            Assert.assertNotNull("The Short looked up is null", shoortField);
            Assert.assertEquals(expected, shoortField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            final Boolean expected = Boolean.TRUE;

            Assert.assertNotNull("The Boolean looked up is null", boooleanField);
            Assert.assertEquals(expected, boooleanField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            final Byte expected = new Byte((byte) 1);

            Assert.assertNotNull("The Byte looked up is null", byyteField);
            Assert.assertEquals(expected, byyteField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException {
        try {
            final Character expected = new Character('D');

            Assert.assertNotNull("The Character looked up is null", chaaracterField);
            Assert.assertEquals(expected, chaaracterField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            Assert.assertNotNull("The DataSource is null", daataSourceField);
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
            Assert.assertNotNull("The EntityManagerFactory is null", emfField);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

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

    public void lookupSessionContext() throws TestFailureException {
        try {
// TODO: DMB: Can't seem to find where to make this work
//            Assert.assertNotNull("The SessionContext is null", ejbContext);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }

    }

    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void setSessionContext(final SessionContext sessionContext) throws EJBException, RemoteException {
    }

    public String remove(final String arg) {
        return arg;
    }
}
