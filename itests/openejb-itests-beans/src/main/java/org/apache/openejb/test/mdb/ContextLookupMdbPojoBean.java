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

import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.stateless.BasicStatelessHome;
import org.apache.openejb.test.stateless.BasicStatelessObject;
import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import org.apache.openejb.test.stateless.BasicStatelessBusinessRemote;
import org.apache.openejb.test.stateful.BasicStatefulHome;
import org.apache.openejb.test.stateful.BasicStatefulObject;
import org.apache.openejb.test.stateful.BasicStatefulBusinessLocal;
import org.apache.openejb.test.stateful.BasicStatefulBusinessRemote;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.entity.bmp.BasicBmpObject;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenContext;
import javax.jms.MessageListener;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.TopicConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.MessageProducer;
import java.rmi.RemoteException;

public class ContextLookupMdbPojoBean implements MessageListener {
    private MdbInvoker mdbInvoker;

    public ContextLookupMdbPojoBean() {
        try {
            ConnectionFactory connectionFactory = (ConnectionFactory) new InitialContext().lookup("java:comp/env/jms");
            mdbInvoker = new MdbInvoker(connectionFactory, this);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    public void onMessage(Message message) {
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


    public void lookupEntityBean() throws TestFailureException {
        try {
            try {
                BasicBmpHome home = (BasicBmpHome) getMessageDrivenContext().lookup("stateless/beanReferences/bmp_entity");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicBmpObject object = home.createObject("Enc Bean");
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            try {
                BasicStatefulHome home = (BasicStatefulHome) getMessageDrivenContext().lookup("stateless/beanReferences/stateful");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicStatefulObject object = home.createObject("Enc Bean");
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            try {
                BasicStatelessHome home = (BasicStatelessHome) getMessageDrivenContext().lookup("stateless/beanReferences/stateless");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicStatelessObject object = home.createObject();
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocal() throws TestFailureException{
        try{
            try{
            BasicStatelessBusinessLocal object = (BasicStatelessBusinessLocal) getMessageDrivenContext().lookup("stateless/beanReferences/stateless-business-local");
            Assert.assertNotNull("The EJB BusinessLocal is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessRemote() throws TestFailureException{
        try{
            try{
            BasicStatelessBusinessRemote object = (BasicStatelessBusinessRemote) getMessageDrivenContext().lookup("stateless/beanReferences/stateless-business-remote");
            Assert.assertNotNull("The EJB BusinessRemote is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocal() throws TestFailureException{
        try{
            try{
            BasicStatefulBusinessLocal object = (BasicStatefulBusinessLocal) getMessageDrivenContext().lookup("stateless/beanReferences/stateful-business-local");
            Assert.assertNotNull("The EJB BusinessLocal is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessRemote() throws TestFailureException{
        try{
            try{
            BasicStatefulBusinessRemote object = (BasicStatefulBusinessRemote) getMessageDrivenContext().lookup("stateless/beanReferences/stateful-business-remote");
            Assert.assertNotNull("The EJB BusinessRemote is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }


    public void lookupStringEntry() throws TestFailureException {
        try {
            try {
                String expected = new String("1");
                String actual = (String) getMessageDrivenContext().lookup("stateless/references/String");

                Assert.assertNotNull("The String looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            try {
                Double expected = new Double(1.0D);
                Double actual = (Double) getMessageDrivenContext().lookup("stateless/references/Double");

                Assert.assertNotNull("The Double looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            try {
                Long expected = new Long(1L);
                Long actual = (Long) getMessageDrivenContext().lookup("stateless/references/Long");

                Assert.assertNotNull("The Long looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            try {
                Float expected = new Float(1.0F);
                Float actual = (Float) getMessageDrivenContext().lookup("stateless/references/Float");

                Assert.assertNotNull("The Float looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            try {
                Integer expected = new Integer(1);
                Integer actual = (Integer) getMessageDrivenContext().lookup("stateless/references/Integer");

                Assert.assertNotNull("The Integer looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            try {
                Short expected = new Short((short) 1);
                Short actual = (Short) getMessageDrivenContext().lookup("stateless/references/Short");

                Assert.assertNotNull("The Short looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            try {
                Boolean expected = new Boolean(true);
                Boolean actual = (Boolean) getMessageDrivenContext().lookup("stateless/references/Boolean");

                Assert.assertNotNull("The Boolean looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            try {
                Byte expected = new Byte((byte) 1);
                Byte actual = (Byte) getMessageDrivenContext().lookup("stateless/references/Byte");

                Assert.assertNotNull("The Byte looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException {
        try {
            try {
                Character expected = new Character('D');
                Character actual = (Character) getMessageDrivenContext().lookup("stateless/references/Character");

                Assert.assertNotNull("The Character looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            try {
                Object obj = getMessageDrivenContext().lookup("datasource");
                Assert.assertNotNull("The DataSource is null", obj);
                Assert.assertTrue("Not an instance of DataSource", obj instanceof DataSource);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupJMSConnectionFactory() throws TestFailureException{
        try{
            try{
                Object obj = getMessageDrivenContext().lookup("jms");
                Assert.assertNotNull("The JMS ConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of ConnectionFactory", obj instanceof ConnectionFactory);
                ConnectionFactory connectionFactory = (ConnectionFactory) obj;
                testJmsConnection(connectionFactory.createConnection());

                obj = getMessageDrivenContext().lookup("TopicCF");
                Assert.assertNotNull("The JMS TopicConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of TopicConnectionFactory", obj instanceof TopicConnectionFactory);
                TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) obj;
                testJmsConnection(topicConnectionFactory.createConnection());

                obj = getMessageDrivenContext().lookup("QueueCF");
                Assert.assertNotNull("The JMS QueueConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of QueueConnectionFactory", obj instanceof QueueConnectionFactory);
                QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) obj;
                testJmsConnection(queueConnectionFactory.createConnection());
            } catch (Exception e){
                e.printStackTrace();
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    private void testJmsConnection(Connection connection) throws JMSException {
        Session session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = session.createTopic("test");
        MessageProducer producer = session.createProducer(topic);
        producer.send(session.createMessage());
        producer.close();
        session.close();
        connection.close();
    }

    public void lookupPersistenceUnit() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                EntityManagerFactory emf = (EntityManagerFactory)ctx.lookup("java:comp/env/persistence/TestUnit");
                Assert.assertNotNull("The EntityManagerFactory is null", emf );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupMessageDrivenContext() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                // lookup in enc
                MessageDrivenContext sctx = (MessageDrivenContext)ctx.lookup("java:comp/env/mdbcontext");
                Assert.assertNotNull("The MessageDrivenContext got from java:comp/env/mdbcontext is null", sctx );

                // lookup using global name
                EJBContext ejbCtx = (EJBContext)ctx.lookup("java:comp/EJBContext");
                Assert.assertNotNull("The MessageDrivenContext got from java:comp/EJBContext is null ", ejbCtx );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }

    }

    public void lookupPersistenceContext() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                EntityManager em = (EntityManager)ctx.lookup("java:comp/env/persistence/TestContext");
                Assert.assertNotNull("The EntityManager is null", em);

                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    /**
     * Set the associated message driven context. The container calls this method
     * after the instance creation.
     */
    public MessageDrivenContext getMessageDrivenContext() throws EJBException, RemoteException {
        MessageDrivenContext ejbContext = null;
        try {
            ejbContext = (MessageDrivenContext) new InitialContext().lookup("java:comp/EJBContext");
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ejbContext;
    }
}
