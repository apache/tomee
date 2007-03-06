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

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.jms.ConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.MessageProducer;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class EncMdbBean implements EncMdbObject, MessageDrivenBean, MessageListener {
	private MessageDrivenContext mdbContext = null;
    private MdbInvoker mdbInvoker;

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        this.mdbContext = ctx;
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

    public void lookupMessageDrivenContext() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                // lookup in enc
                MessageDrivenContext messageDrivenContext = (MessageDrivenContext)ctx.lookup("java:comp/env/mdbcontext");
                Assert.assertNotNull("The SessionContext got from java:comp/env/mdbcontext is null", messageDrivenContext );

                // lookup using global name
                EJBContext ejbCtx = (EJBContext)ctx.lookup("java:comp/EJBContext");
                Assert.assertNotNull("The SessionContext got from java:comp/EJBContext is null ", ejbCtx );

                // verify context was set via legacy set method
                Assert.assertNotNull("The MdbContext is null from setter method", mdbContext );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }

    }

    public void ejbCreate() throws javax.ejb.CreateException{
    }

    public void ejbRemove() throws EJBException {
    }
}
