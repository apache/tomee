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

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.stateless.BasicStatelessHome;
import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import org.apache.openejb.test.stateless.BasicStatelessBusinessRemote;

import javax.ejb.CreateException;
import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.jms.JMSException;
import java.rmi.RemoteException;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class FieldInjectionStatefulBean implements SessionBean {


    private String name;
    private SessionContext ejbContext;
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
            Assert.assertNotNull("The EJBObject is null", bmpHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statefulHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statelessHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocal() throws TestFailureException{
        try{
            Assert.assertNotNull("The EJB BusinessLocal is null", statelessBusinessLocal );
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessRemote() throws TestFailureException{
        try{
            Assert.assertNotNull("The EJB BusinessRemote is null", statelessBusinessRemote );
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocal() throws TestFailureException{
        try{
            Assert.assertNotNull("The EJB BusinessLocal is null", statefulBusinessLocal );
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessRemote() throws TestFailureException{
        try{
            Assert.assertNotNull("The EJB BusinessRemote is null", statefulBusinessRemote );
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException {
        try {
            String expected = new String("1");
            Assert.assertNotNull("The String looked up is null", striing);
            Assert.assertEquals(expected, striing);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            Double expected = new Double(1.0D);

            Assert.assertNotNull("The Double looked up is null", doouble);
            Assert.assertEquals(expected, doouble);

        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            Long expected = new Long(1L);

            Assert.assertNotNull("The Long looked up is null", loong);
            Assert.assertEquals(expected, loong);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            Float expected = new Float(1.0F);

            Assert.assertNotNull("The Float looked up is null", flooat);
            Assert.assertEquals(expected, flooat);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            Integer expected = new Integer(1);

            Assert.assertNotNull("The Integer looked up is null", inteeger);
            Assert.assertEquals(expected, inteeger);

        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            Short expected = new Short((short) 1);

            Assert.assertNotNull("The Short looked up is null", shoort);
            Assert.assertEquals(expected, shoort);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            Boolean expected = new Boolean(true);

            Assert.assertNotNull("The Boolean looked up is null", booolean);
            Assert.assertEquals(expected, booolean);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            Byte expected = new Byte((byte) 1);

            Assert.assertNotNull("The Byte looked up is null", byyte);
            Assert.assertEquals(expected, byyte);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException {
        try {
            Character expected = new Character('D');

            Assert.assertNotNull("The Character looked up is null", chaaracter);
            Assert.assertEquals(expected, chaaracter);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            Assert.assertNotNull("The DataSource is null", daataSource);
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
            Assert.assertNotNull("The EntityManagerFactory is null", emf);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

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
