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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.MessageProducer;
import javax.persistence.PersistenceUnit;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class LocalClientTest extends TestCase {

    @EJB
    private Everything local;

    @Resource
    private UserTransaction useerTransaction;

    @Resource
    private DataSource daataSource;

//    @Resource
//    private Queue quueue;
//
//    @Resource
//    private Topic toopic;
//
//    @Resource
//    private ConnectionFactory coonnectionFactory;
//
//    @Resource
//    private QueueConnectionFactory queueCoonnectionFactory;
//
//    @Resource
//    private TopicConnectionFactory topicCoonnectionFactory;

    @PersistenceUnit
    private EntityManagerFactory emf;

    @PersistenceContext
    private EntityManager em;

    public void setUp() throws OpenEJBException, NamingException, IOException {
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        AppModule app = new AppModule(this.getClass().getClassLoader(), "test-app");

        Persistence persistence = new Persistence(new org.apache.openejb.jee.jpa.unit.PersistenceUnit("foo-unit"));
        app.getPersistenceModules().add(new PersistenceModule("root", persistence));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(SuperBean.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        ClientModule clientModule = new ClientModule(null, app.getClassLoader(), app.getJarLocation(), null, null);
        clientModule.getLocalClients().add(this.getClass().getName());

        app.getClientModules().add(clientModule);
        
        assembler.createApplication(config.configureApplication(app));
    }

    public void test() throws Exception {

        Properties properties = new Properties();
        properties.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        InitialContext context = new InitialContext(properties);
        context.bind("inject", this);

        assertRefs();
    }

    protected void assertRefs() throws JMSException {
        // @EJB
        Reference reference = new Reference("test");

        assertNotNull("The EJB BusinessRemote is null", local);
        assertEquals(reference, local.echo(reference));
        assertSame(reference, local.echo(reference)); // pass by reference

        // @Resource DataSource
        assertNotNull("The DataSource is null", daataSource);

//        // @Resource Topic
//        assertNotNull("The Topic is null", toopic);
//
//        // @Resource Queue
//        assertNotNull("The Queue is null", quueue);
//
//        // @Resource ConnectionFactory
//        assertNotNull("The ConnectionFactory is null", coonnectionFactory);
//        testJmsConnection(coonnectionFactory.createConnection());
//
//        // @Resource QueueConnectionFactory
//        assertNotNull("The QueueConnectionFactory is null", queueCoonnectionFactory);
//        testJmsConnection(queueCoonnectionFactory.createConnection());
//
//        // @Resource TopicConnectionFactory
//        assertNotNull("The TopicConnectionFactory is null", queueCoonnectionFactory);
//        testJmsConnection(queueCoonnectionFactory.createConnection());

        // @Resource UserTransaction
        assertNotNull("The UserTransaction is null", useerTransaction);

        // @PersistenceUnit
        assertNotNull("The EntityManagerFactory is null", emf);

        // @PersistenceContext
        assertNotNull("The EntityManager is null", em);
    }

    public void lookupPersistenceContext() {
        assertNotNull("The EntityManager is null", em);

        try {
            // call a do nothing method to assure entity manager actually exists
            em.getFlushMode();
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
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

    @Local
    @Remote
    @WebService
    public static interface Everything {
        public Object echo(Object o);
    }

    public static class SuperBean implements Everything {
        public Object echo(Object o) {
            return o;
        }
    }

    public static class Reference implements Serializable {
        private final String value;

        public Reference(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Reference value1 = (Reference) o;

            if (!value.equals(value1.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

}
