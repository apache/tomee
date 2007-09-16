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
package org.apache.openejb.test.stateless;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.MessageProducer;
import javax.jms.TopicConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.JMSException;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.entity.bmp.BasicBmpObject;
import org.apache.openejb.test.stateful.BasicStatefulHome;
import org.apache.openejb.test.stateful.BasicStatefulObject;
import org.apache.openejb.test.stateful.BasicStatefulBusinessRemote;
import org.apache.openejb.test.stateful.BasicStatefulBusinessLocal;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class EncStatelessBean implements javax.ejb.SessionBean{

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
    public void lookupEntityBean() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            BasicBmpHome home = (BasicBmpHome) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateless/beanReferences/bmp_entity"), BasicBmpHome.class );
            Assert.assertNotNull("The EJBHome looked up is null",home);

            BasicBmpObject object = home.createObject("Enc Bean");
            Assert.assertNotNull("The EJBObject is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            BasicStatefulHome home = (BasicStatefulHome) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateless/beanReferences/stateful"), BasicStatefulHome.class );
            Assert.assertNotNull("The EJBHome looked up is null",home);

            BasicStatefulObject object = home.createObject("Enc Bean");
            Assert.assertNotNull("The EJBObject is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            BasicStatelessHome home = (BasicStatelessHome) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateless/beanReferences/stateless"), BasicStatelessHome.class );
            Assert.assertNotNull("The EJBHome looked up is null",home);

            BasicStatelessObject object = home.createObject();
            Assert.assertNotNull("The EJBObject is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocal() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

                Object o = ctx.lookup("java:comp/env/stateless/beanReferences/stateless-business-local");
                BasicStatelessBusinessLocal object = (BasicStatelessBusinessLocal) o;
            Assert.assertNotNull("The EJB BusinessLocal is null", object );
            } catch (Exception e){
                e.printStackTrace();
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessRemote() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            BasicStatelessBusinessRemote object = (BasicStatelessBusinessRemote) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateless/beanReferences/stateless-business-remote"), BasicStatelessBusinessRemote.class );
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
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            BasicStatefulBusinessLocal object = (BasicStatefulBusinessLocal) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateless/beanReferences/stateful-business-local"), BasicStatefulBusinessLocal.class );
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
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            BasicStatefulBusinessRemote object = (BasicStatefulBusinessRemote) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateless/beanReferences/stateful-business-remote"), BasicStatefulBusinessRemote.class );
            Assert.assertNotNull("The EJB BusinessRemote is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            String expected = new String("1");
            String actual   = (String)ctx.lookup("java:comp/env/stateless/references/String");

            Assert.assertNotNull("The String looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            Double expected = new Double(1.0D);
            Double actual   = (Double)ctx.lookup("java:comp/env/stateless/references/Double");

            Assert.assertNotNull("The Double looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            Long expected = new Long(1L);
            Long actual   = (Long)ctx.lookup("java:comp/env/stateless/references/Long");

            Assert.assertNotNull("The Long looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            Float expected = new Float(1.0F);
            Float actual   = (Float)ctx.lookup("java:comp/env/stateless/references/Float");

            Assert.assertNotNull("The Float looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            Integer expected = new Integer(1);
            Integer actual   = (Integer)ctx.lookup("java:comp/env/stateless/references/Integer");

            Assert.assertNotNull("The Integer looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            Short expected = new Short((short)1);
            Short actual   = (Short)ctx.lookup("java:comp/env/stateless/references/Short");

            Assert.assertNotNull("The Short looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            Boolean expected = new Boolean(true);
            Boolean actual = (Boolean)ctx.lookup("java:comp/env/stateless/references/Boolean");

            Assert.assertNotNull("The Boolean looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            Byte expected = new Byte((byte)1);
            Byte actual   = (Byte)ctx.lookup("java:comp/env/stateless/references/Byte");

            Assert.assertNotNull("The Byte looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );

            Character expected = new Character('D');
            Character actual   = (Character)ctx.lookup("java:comp/env/stateless/references/Character");

            Assert.assertNotNull("The Character looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                Object obj = ctx.lookup("java:comp/env/datasource");
                Assert.assertNotNull("The DataSource is null", obj);
                Assert.assertTrue("Not an instance of DataSource", obj instanceof DataSource);
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupJMSConnectionFactory() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                Object obj = ctx.lookup("java:comp/env/jms");
                Assert.assertNotNull("The JMS ConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of ConnectionFactory", obj instanceof ConnectionFactory);
                ConnectionFactory connectionFactory = (ConnectionFactory) obj;
                testJmsConnection(connectionFactory.createConnection());

                obj = ctx.lookup("java:comp/env/TopicCF");
                Assert.assertNotNull("The JMS TopicConnectionFactory is null", obj);
                Assert.assertTrue("Not an instance of TopicConnectionFactory", obj instanceof TopicConnectionFactory);
                TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) obj;
                testJmsConnection(topicConnectionFactory.createConnection());

                obj = ctx.lookup("java:comp/env/QueueCF");
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

    private void testJmsConnection(javax.jms.Connection connection) throws JMSException {
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

    public void lookupSessionContext() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                // lookup in enc
                SessionContext sctx = (SessionContext)ctx.lookup("java:comp/env/sessioncontext");
                Assert.assertNotNull("The SessionContext got from java:comp/env/sessioncontext is null", sctx );

                // lookup using global name
                EJBContext ejbCtx = (EJBContext)ctx.lookup("java:comp/EJBContext");
                Assert.assertNotNull("The SessionContext got from java:comp/EJBContext is null ", ejbCtx );

                // verify context was set via legacy set method
                Assert.assertNotNull("The SessionContext is null from setter method", ejbContext );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
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
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }
    /**
     * 
     * @exception javax.ejb.CreateException
     */
    public void ejbCreate() throws javax.ejb.CreateException{
        this.name = "nameless automaton";
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
    }

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        // Should never called.
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
        // Should never called.
    }

    //    
    // SessionBean interface methods
    //================================
    public String remove(String arg) {
        return arg;
    }
}
