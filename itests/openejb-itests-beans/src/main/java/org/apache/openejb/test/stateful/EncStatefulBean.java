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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.stateful;

import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.entity.bmp.BasicBmpObject;
import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import org.apache.openejb.test.stateless.BasicStatelessBusinessRemote;
import org.apache.openejb.test.stateless.BasicStatelessHome;
import org.apache.openejb.test.stateless.BasicStatelessObject;
import org.apache.openejb.test.stateless.BasicStatelessPojoBean;
import org.junit.Assert;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.SessionSynchronization;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.rmi.RemoteException;

public class EncStatefulBean implements jakarta.ejb.SessionBean, SessionSynchronization {


    private String name;
    private SessionContext ejbContext;

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
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final BasicBmpHome home = (BasicBmpHome) ctx.lookup("java:comp/env/stateful/beanReferences/bmp_entity");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                final BasicBmpObject object = home.createObject("Enc Bean");
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final BasicStatefulHome home = (BasicStatefulHome) ctx.lookup("java:comp/env/stateful/beanReferences/stateful");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                final BasicStatefulObject object = home.createObject("Enc Bean");
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final BasicStatelessHome home = (BasicStatelessHome) ctx.lookup("java:comp/env/stateful/beanReferences/stateless");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                final BasicStatelessObject object = home.createObject();
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocal() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Object o = ctx.lookup("java:comp/env/stateful/beanReferences/stateless-business-local");
                final BasicStatelessBusinessLocal object = (BasicStatelessBusinessLocal) o;
                Assert.assertNotNull("The EJB BusinessLocal is null", object);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocalBean() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Object o = ctx.lookup("java:comp/env/stateful/beanReferences/stateless-business-localbean");
                final BasicStatelessPojoBean object = (BasicStatelessPojoBean) o;
                Assert.assertNotNull("The EJB BusinessLocalBean is null", object);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessRemote() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final BasicStatelessBusinessRemote object = (BasicStatelessBusinessRemote) ctx.lookup("java:comp/env/stateful/beanReferences/stateless-business-remote");
                Assert.assertNotNull("The EJB BusinessRemote is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocal() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final BasicStatefulBusinessLocal object = (BasicStatefulBusinessLocal) ctx.lookup("java:comp/env/stateful/beanReferences/stateful-business-local");
                Assert.assertNotNull("The EJB BusinessLocal is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocalBean() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final BasicStatefulPojoBean object = (BasicStatefulPojoBean) ctx.lookup("java:comp/env/stateful/beanReferences/stateful-business-localbean");
                Assert.assertNotNull("The EJB BusinessLocalBean is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessRemote() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final BasicStatefulBusinessRemote object = (BasicStatefulBusinessRemote) ctx.lookup("java:comp/env/stateful/beanReferences/stateful-business-remote");
                Assert.assertNotNull("The EJB BusinessRemote is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final String expected = new String("1");
                final String actual = (String) ctx.lookup("java:comp/env/stateful/references/String");

                Assert.assertNotNull("The String looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Double expected = new Double(1.0D);
                final Double actual = (Double) ctx.lookup("java:comp/env/stateful/references/Double");

                Assert.assertNotNull("The Double looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Long expected = new Long(1L);
                final Long actual = (Long) ctx.lookup("java:comp/env/stateful/references/Long");

                Assert.assertNotNull("The Long looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Float expected = new Float(1.0F);
                final Float actual = (Float) ctx.lookup("java:comp/env/stateful/references/Float");

                Assert.assertNotNull("The Float looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Integer expected = new Integer(1);
                final Integer actual = (Integer) ctx.lookup("java:comp/env/stateful/references/Integer");

                Assert.assertNotNull("The Integer looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Short expected = new Short((short) 1);
                final Short actual = (Short) ctx.lookup("java:comp/env/stateful/references/Short");

                Assert.assertNotNull("The Short looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Boolean expected = Boolean.TRUE;
                final Boolean actual = (Boolean) ctx.lookup("java:comp/env/stateful/references/Boolean");

                Assert.assertNotNull("The Boolean looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Byte expected = new Byte((byte) 1);
                final Byte actual = (Byte) ctx.lookup("java:comp/env/stateful/references/Byte");

                Assert.assertNotNull("The Byte looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                final Character expected = new Character('D');
                final Character actual = (Character) ctx.lookup("java:comp/env/stateful/references/Character");

                Assert.assertNotNull("The Character looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                final Object obj = ctx.lookup("java:comp/env/datasource");
                Assert.assertNotNull("The DataSource is null", obj);
                Assert.assertTrue("Not an instance of DataSource", obj instanceof DataSource);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupJMSConnectionFactory() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                Object obj = ctx.lookup("java:comp/env/jms");
                Assert.assertNotNull("The JMS ConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of ConnectionFactory", obj instanceof ConnectionFactory);
                final ConnectionFactory connectionFactory = (ConnectionFactory) obj;
                testJmsConnection(connectionFactory.createConnection());

                obj = ctx.lookup("java:comp/env/TopicCF");
                Assert.assertNotNull("The JMS TopicConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of TopicConnectionFactory", obj instanceof TopicConnectionFactory);
                final TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) obj;
                testJmsConnection(topicConnectionFactory.createConnection());

                obj = ctx.lookup("java:comp/env/QueueCF");
                Assert.assertNotNull("The JMS QueueConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of QueueConnectionFactory", obj instanceof QueueConnectionFactory);
                final QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) obj;
                testJmsConnection(queueConnectionFactory.createConnection());
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    private void testJmsConnection(final Connection connection) throws JMSException {
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
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                final EntityManagerFactory emf = (EntityManagerFactory) ctx.lookup("java:comp/env/persistence/TestUnit");
                Assert.assertNotNull("The EntityManagerFactory is null", emf);

            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupPersistenceContext() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                final EntityManager em = (EntityManager) ctx.lookup("java:comp/env/persistence/TestContext");
                Assert.assertNotNull("The EntityManager is null", em);

                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }


    public void lookupSessionContext() throws TestFailureException {
        try {
            try {
                final InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                // lookup in enc
                final SessionContext sctx = (SessionContext) ctx.lookup("java:comp/env/sessioncontext");
                Assert.assertNotNull("The SessionContext got from java:comp/env/sessioncontext is null", sctx);

                // lookup using global name
                final EJBContext ejbCtx = (EJBContext) ctx.lookup("java:comp/EJBContext");
                Assert.assertNotNull("The SessionContext got from java:comp/EJBContext is null ", ejbCtx);

                // verify context was set via legacy set method
                Assert.assertNotNull("The SessionContext is null from setter method", ejbContext);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }

    }

    public String remove(final String arg) {
        return arg;
    }

    //
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    

    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(final SessionContext ctx) throws EJBException, RemoteException {
        ejbContext = ctx;
    }

    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException, RemoteException {
    }

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException, RemoteException {
    }

    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException, RemoteException {
    }
    //    
    // SessionBean interface methods
    //==================================


    //============================================
    // SessionSynchronization interface methods
    //

    /**
     * The afterBegin method notifies a session Bean instance that a new
     * transaction has started, and that the subsequent business methods on the
     * instance will be invoked in the context of the transaction.
     */
    public void afterBegin() throws EJBException, RemoteException {
    }

    /**
     * The beforeCompletion method notifies a session Bean instance that
     * a transaction is about to be committed. The instance can use this
     * method, for example, to write any cached data to a database.
     */
    public void beforeCompletion() throws EJBException, RemoteException {
    }

    /**
     * The afterCompletion method notifies a session Bean instance that a
     * transaction commit protocol has completed, and tells the instance
     * whether the transaction has been committed or rolled back.
     */
    public void afterCompletion(final boolean committed) throws EJBException, RemoteException {
    }
    //    
    // SessionSynchronization interface methods
    //============================================

}
