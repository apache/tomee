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
import org.apache.openejb.test.TestFailureException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.sql.DataSource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.jms.JMSException;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.rmi.RemoteException;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
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

    public BasicStatefulBusinessLocal getStatefulBusinessLocal() {
        return statefulBusinessLocalField;
    }

    public void setStatefulBusinessLocal(BasicStatefulBusinessLocal statefulBusinessLocal) {
        this.statefulBusinessLocalField = statefulBusinessLocal;
    }

    public BasicStatefulBusinessRemote getStatefulBusinessRemote() {
        return statefulBusinessRemoteField;
    }

    public void setStatefulBusinessRemote(BasicStatefulBusinessRemote statefulBusinessRemote) {
        this.statefulBusinessRemoteField = statefulBusinessRemote;
    }

    public BasicStatelessBusinessLocal getStatelessBusinessLocal() {
        return statelessBusinessLocalField;
    }

    public void setStatelessBusinessLocal(BasicStatelessBusinessLocal statelessBusinessLocal) {
        this.statelessBusinessLocalField = statelessBusinessLocal;
    }

    public BasicStatelessBusinessRemote getStatelessBusinessRemote() {
        return statelessBusinessRemoteField;
    }

    public void setStatelessBusinessRemote(BasicStatelessBusinessRemote statelessBusinessRemote) {
        this.statelessBusinessRemoteField = statelessBusinessRemote;
    }



    public BasicBmpHome getBmpHome() {
        return bmpHomeField;
    }

    public void setBmpHome(BasicBmpHome bmpHome) {
        this.bmpHomeField = bmpHome;
    }

    public Boolean getBooolean() {
        return boooleanField;
    }

    public void setBooolean(Boolean booolean) {
        this.boooleanField = booolean;
    }

    public Byte getByyte() {
        return byyteField;
    }

    public void setByyte(Byte byyte) {
        this.byyteField = byyte;
    }

    public Character getChaaracter() {
        return chaaracterField;
    }

    public void setChaaracter(Character chaaracter) {
        this.chaaracterField = chaaracter;
    }

    public DataSource getDaataSource() {
        return daataSourceField;
    }

    public void setDaataSource(DataSource daataSource) {
        this.daataSourceField = daataSource;
    }

    public ConnectionFactory getCoonnectionFactory() {
        return coonnectionFactory;
    }

    public void setCoonnectionFactory(ConnectionFactory coonnectionFactory) {
        this.coonnectionFactory = coonnectionFactory;
    }

    public QueueConnectionFactory getQueueCoonnectionFactory() {
        return queueCoonnectionFactory;
    }

    public void setQueueCoonnectionFactory(QueueConnectionFactory queueCoonnectionFactory) {
        this.queueCoonnectionFactory = queueCoonnectionFactory;
    }

    public TopicConnectionFactory getTopicCoonnectionFactory() {
        return topicCoonnectionFactory;
    }

    public void setTopicCoonnectionFactory(TopicConnectionFactory topicCoonnectionFactory) {
        this.topicCoonnectionFactory = topicCoonnectionFactory;
    }

    public Double getDoouble() {
        return dooubleField;
    }

    public void setDoouble(Double doouble) {
        this.dooubleField = doouble;
    }

    public EntityManager getEem() {
        return eemField;
    }

    public void setEem(EntityManager eem) {
        this.eemField = eem;
    }

    public SessionContext getEjbContext() {
        return ejbContextField;
    }

    public void setEjbContext(SessionContext ejbContext) {
        this.ejbContextField = ejbContext;
    }

    public EntityManager getEm() {
        return emField;
    }

    public void setEm(EntityManager em) {
        this.emField = em;
    }

    public EntityManagerFactory getEmf() {
        return emfField;
    }

    public void setEmf(EntityManagerFactory emf) {
        this.emfField = emf;
    }

    public Float getFlooat() {
        return flooatField;
    }

    public void setFlooat(Float flooat) {
        this.flooatField = flooat;
    }

    public Integer getInteeger() {
        return inteegerField;
    }

    public void setInteeger(Integer inteeger) {
        this.inteegerField = inteeger;
    }

    public Long getLoong() {
        return loongField;
    }

    public void setLoong(Long loong) {
        this.loongField = loong;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityManager getPem() {
        return pemField;
    }

    public void setPem(EntityManager pem) {
        this.pemField = pem;
    }

    public Short getShoort() {
        return shoortField;
    }

    public void setShoort(Short shoort) {
        this.shoortField = shoort;
    }

    public BasicStatefulHome getStatefulHome() {
        return statefulHomeField;
    }

    public void setStatefulHome(BasicStatefulHome statefulHome) {
        this.statefulHomeField = statefulHome;
    }

    public BasicStatelessHome getStatelessHome() {
        return statelessHomeField;
    }

    public void setStatelessHome(BasicStatelessHome statelessHome) {
        this.statelessHomeField = statelessHome;
    }

    public String getStriing() {
        return striingField;
    }

    public void setStriing(String striing) {
        this.striingField = striing;
    }
    //=============================
    // Home interface methods
    //

    /**
     * Maps to EncStatefulHome.create
     *
     * @param name
     * @throws javax.ejb.CreateException
     * @see EncStatefulHome#create
     */
    public void ejbCreate(String name) throws CreateException {
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
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statefulHomeField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statelessHomeField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocal() throws TestFailureException{
        try{
            Assert.assertNotNull("The EJB BusinessLocal is null", statelessBusinessLocalField );
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessRemote() throws TestFailureException{
        try{
            Assert.assertNotNull("The EJB BusinessRemote is null", statelessBusinessRemoteField );
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocal() throws TestFailureException{
        try{
            Assert.assertNotNull("The EJB BusinessLocal is null", statefulBusinessLocalField );
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessRemote() throws TestFailureException{
        try{
            Assert.assertNotNull("The EJB BusinessRemote is null", statefulBusinessRemoteField );
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException {
        try {
            String expected = new String("1");
            Assert.assertNotNull("The String looked up is null", striingField);
            Assert.assertEquals(expected, striingField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            Double expected = new Double(1.0D);

            Assert.assertNotNull("The Double looked up is null", dooubleField);
            Assert.assertEquals(expected, dooubleField);

        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            Long expected = new Long(1L);

            Assert.assertNotNull("The Long looked up is null", loongField);
            Assert.assertEquals(expected, loongField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            Float expected = new Float(1.0F);

            Assert.assertNotNull("The Float looked up is null", flooatField);
            Assert.assertEquals(expected, flooatField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            Integer expected = new Integer(1);

            Assert.assertNotNull("The Integer looked up is null", inteegerField);
            Assert.assertEquals(expected, inteegerField);

        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            Short expected = new Short((short) 1);

            Assert.assertNotNull("The Short looked up is null", shoortField);
            Assert.assertEquals(expected, shoortField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            Boolean expected = new Boolean(true);

            Assert.assertNotNull("The Boolean looked up is null", boooleanField);
            Assert.assertEquals(expected, boooleanField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            Byte expected = new Byte((byte) 1);

            Assert.assertNotNull("The Byte looked up is null", byyteField);
            Assert.assertEquals(expected, byyteField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException {
        try {
            Character expected = new Character('D');

            Assert.assertNotNull("The Character looked up is null", chaaracterField);
            Assert.assertEquals(expected, chaaracterField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            Assert.assertNotNull("The DataSource is null", daataSourceField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupJMSConnectionFactory() throws TestFailureException{
        try{
            try{
                testJmsConnection(coonnectionFactory.createConnection());
                testJmsConnection(queueCoonnectionFactory.createConnection());
                testJmsConnection(topicCoonnectionFactory.createConnection());
            } catch (Exception e){
                e.printStackTrace();
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    private void testJmsConnection(javax.jms.Connection connection) throws JMSException {
        Session session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = session.createTopic("test");
        MessageProducer producer = session.createProducer(topic);
        producer.send(session.createMessage());
        producer.close();
        session.close();
        connection.close();
    }

    public void lookupPersistenceUnit() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManagerFactory is null", emfField);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupPersistenceContext() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManager is null", emField);

            try {
                // call a do nothing method to assure entity manager actually exists
                emField.getFlushMode();
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupSessionContext() throws TestFailureException {
        try {
// TODO: DMB: Can't seem to find where to make this work
//            Assert.assertNotNull("The SessionContext is null", ejbContext);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }

    }

    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
    }
    
    public String remove(String arg) {
        return arg;
    }    
}
