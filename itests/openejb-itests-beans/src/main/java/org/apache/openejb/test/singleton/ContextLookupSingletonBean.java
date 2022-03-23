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
package org.apache.openejb.test.singleton;

import org.junit.Assert;
import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.entity.bmp.BasicBmpObject;
import org.apache.openejb.test.stateful.*;

import jakarta.ejb.EJBContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Connection;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.MessageProducer;
import jakarta.jms.TopicConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import java.rmi.RemoteException;

public class ContextLookupSingletonBean implements jakarta.ejb.SessionBean {

    private String name;
    private SessionContext ejbContext;

    //=============================
    // Home interface methods
    //
    //
    // Home interface methods
    //=============================


    //=============================
    // Remote interface methods
    //
    public void lookupEntityBean() throws TestFailureException {
        try {
            try {
                final BasicBmpHome home = (BasicBmpHome) ejbContext.lookup("singleton/beanReferences/bmp_entity");
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
                final BasicStatefulHome home = (BasicStatefulHome) ejbContext.lookup("singleton/beanReferences/stateful");
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

    public void lookupSingletonBean() throws TestFailureException {
        try {
            try {
                final BasicSingletonHome home = (BasicSingletonHome) ejbContext.lookup("singleton/beanReferences/singleton");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                final BasicSingletonObject object = home.createObject();
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupSingletonBusinessLocal() throws TestFailureException {
        try {
            try {
                final BasicSingletonBusinessLocal object = (BasicSingletonBusinessLocal) ejbContext.lookup("singleton/beanReferences/singleton-business-local");
                Assert.assertNotNull("The EJB BusinessLocal is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupSingletonBusinessLocalBean() throws TestFailureException {
        try {
            try {
                final BasicSingletonPojoBean object = (BasicSingletonPojoBean) ejbContext.lookup("singleton/beanReferences/singleton-business-localbean");
                Assert.assertNotNull("The EJB BusinessLocalBean is null", object);
            } catch (final Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupSingletonBusinessRemote() throws TestFailureException {
        try {
            try {
                final BasicSingletonBusinessRemote object = (BasicSingletonBusinessRemote) ejbContext.lookup("singleton/beanReferences/singleton-business-remote");
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
                final BasicStatefulBusinessLocal object = (BasicStatefulBusinessLocal) ejbContext.lookup("singleton/beanReferences/stateful-business-local");
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
                final BasicStatefulPojoBean object = (BasicStatefulPojoBean) ejbContext.lookup("singleton/beanReferences/stateful-business-localbean");
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
                final BasicStatefulBusinessRemote object = (BasicStatefulBusinessRemote) ejbContext.lookup("singleton/beanReferences/stateful-business-remote");
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
                final String expected = new String("1");
                final String actual = (String) ejbContext.lookup("singleton/references/String");

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
                final Double expected = new Double(1.0D);
                final Double actual = (Double) ejbContext.lookup("singleton/references/Double");

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
                final Long expected = new Long(1L);
                final Long actual = (Long) ejbContext.lookup("singleton/references/Long");

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
                final Float expected = new Float(1.0F);
                final Float actual = (Float) ejbContext.lookup("singleton/references/Float");

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
                final Integer expected = new Integer(1);
                final Integer actual = (Integer) ejbContext.lookup("singleton/references/Integer");

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
                final Short expected = new Short((short) 1);
                final Short actual = (Short) ejbContext.lookup("singleton/references/Short");

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
                final Boolean expected = Boolean.TRUE;
                final Boolean actual = (Boolean) ejbContext.lookup("singleton/references/Boolean");

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
                final Byte expected = new Byte((byte) 1);
                final Byte actual = (Byte) ejbContext.lookup("singleton/references/Byte");

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
                final Character expected = new Character('D');
                final Character actual = (Character) ejbContext.lookup("singleton/references/Character");

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
                final Object obj = ejbContext.lookup("datasource");
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
                Object obj = ejbContext.lookup("jms");
                Assert.assertNotNull("The JMS ConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of ConnectionFactory", obj instanceof ConnectionFactory);
                final ConnectionFactory connectionFactory = (ConnectionFactory) obj;
                testJmsConnection(connectionFactory.createConnection());

                obj = ejbContext.lookup("TopicCF");
                Assert.assertNotNull("The JMS TopicConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of TopicConnectionFactory", obj instanceof TopicConnectionFactory);
                final TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) obj;
                testJmsConnection(topicConnectionFactory.createConnection());

                obj = ejbContext.lookup("QueueCF");
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
                final EntityManagerFactory emf = (EntityManagerFactory) ejbContext.lookup("persistence/TestUnit");
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
                final EntityManager em = (EntityManager) ejbContext.lookup("persistence/TestContext");
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


    //
    // Remote interface methods
    //=============================

    //================================
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
     * @throws jakarta.ejb.CreateException
     */
    public void ejbCreate() throws jakarta.ejb.CreateException {
        this.name = "nameless automaton";
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
        // Should never called.
    }

    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException, RemoteException {
        // Should never called.
    }

    //
    // SessionBean interface methods
    //================================

    public String remove(final String arg) {
        return arg;
    }
}
