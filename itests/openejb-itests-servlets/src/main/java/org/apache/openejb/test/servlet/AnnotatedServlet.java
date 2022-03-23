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
package org.apache.openejb.test.servlet;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnectionFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.junit.Assert;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.stateful.BasicStatefulBusinessLocal;
import org.apache.openejb.test.stateful.BasicStatefulBusinessRemote;
import org.apache.openejb.test.stateful.BasicStatefulHome;
import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import org.apache.openejb.test.stateless.BasicStatelessBusinessRemote;
import org.apache.openejb.test.stateless.BasicStatelessHome;

public class AnnotatedServlet extends HttpServlet implements JndiTestServlet {
    @EJB(beanName = "BasicBmpBean")
    private BasicBmpHome bmpHome;
    @EJB(beanName = "BasicStatefulBean")
    private BasicStatefulHome statefulHome;
    @EJB(beanName = "BasicStatelessBean")
    private BasicStatelessHome statelessHome;
    @Resource
    private String striing;
    @Resource
    private Double doouble;
    @Resource
    private Long loong;
    @Resource
    private Float flooat;
    @Resource
    private Integer inteeger;
    @Resource
    private Short shoort;
    @Resource
    private Boolean booolean;
    @Resource
    private Byte byyte;
    @Resource
    private Character chaaracter;
    @Resource
    private DataSource daataSource;
    @Resource
    private ConnectionFactory coonnectionFactory;
    @Resource
    private QueueConnectionFactory queueCoonnectionFactory;
    @Resource
    private TopicConnectionFactory topicCoonnectionFactory;
    @PersistenceUnit(unitName = "openjpa-test-unit")
    private EntityManagerFactory emf;
    @PersistenceContext(unitName = "openjpa-test-unit")
    private EntityManager em;
    @EJB
    private BasicStatelessBusinessLocal statelessBusinessLocal;
    @EJB
    private BasicStatelessBusinessRemote statelessBusinessRemote;
    @EJB
    private BasicStatefulBusinessLocal statefulBusinessLocal;
    @EJB
    private BasicStatefulBusinessRemote statefulBusinessRemote;

    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        final ServletOutputStream out = response.getOutputStream();
        final PrintStream printStream = new PrintStream(out);

        final String methodName = request.getParameter("method");
        if (methodName == null) {
            testAll(printStream);
        } else {
            try {
                final Method method = getClass().getMethod(methodName);
                method.invoke(this);
            } catch (final Throwable e) {
                // response.setStatus(580);
                printStream.println("FAILED");
                e.printStackTrace(printStream);
            }
        }
        printStream.flush();
    }

    public void testAll(final PrintStream printStream) {
        for (final Method method : JndiTestServlet.class.getMethods()) {
            try {
                method.invoke(this);
                printStream.println(method.getName() + " PASSED");
            } catch (final Throwable e) {
                printStream.println(method.getName() + " FAILED");
                e.printStackTrace(printStream);
                printStream.flush();
            }
            printStream.println();
        }
    }

    public void lookupEntityBean() {
        Assert.assertNotNull("The EJBObject is null", bmpHome);
    }

    public void lookupStatefulBean() {
        Assert.assertNotNull("The EJBObject is null", statefulHome);
    }

    public void lookupStatelessBean() {
        Assert.assertNotNull("The EJBObject is null", statelessHome);
    }

    public void lookupStatelessBusinessLocal() {
        Assert.assertNotNull("The EJB BusinessLocal is null", statelessBusinessLocal);
    }

    public void lookupStatelessBusinessRemote() {
        Assert.assertNotNull("The EJB BusinessRemote is null", statelessBusinessRemote);
    }

    public void lookupStatefulBusinessLocal() {
        Assert.assertNotNull("The EJB BusinessLocal is null", statefulBusinessLocal);
    }

    public void lookupStatefulBusinessRemote() {
        Assert.assertNotNull("The EJB BusinessRemote is null", statefulBusinessRemote);
    }

    public void lookupStringEntry() {
        final String expected = "1";
        Assert.assertNotNull("The String looked up is null", striing);
        Assert.assertEquals(expected, striing);
    }

    public void lookupDoubleEntry() {
        final Double expected = 1.0D;

        Assert.assertNotNull("The Double looked up is null", doouble);
        Assert.assertEquals(expected, doouble);
    }

    public void lookupLongEntry() {
        final Long expected = 1L;

        Assert.assertNotNull("The Long looked up is null", loong);
        Assert.assertEquals(expected, loong);
    }

    public void lookupFloatEntry() {
        final Float expected = 1.0F;

        Assert.assertNotNull("The Float looked up is null", flooat);
        Assert.assertEquals(expected, flooat);
    }

    public void lookupIntegerEntry() {
        final Integer expected = 1;

        Assert.assertNotNull("The Integer looked up is null", inteeger);
        Assert.assertEquals(expected, inteeger);
    }

    public void lookupShortEntry() {
        final Short expected = (short) 1;

        Assert.assertNotNull("The Short looked up is null", shoort);
        Assert.assertEquals(expected, shoort);
    }

    public void lookupBooleanEntry() {
        final Boolean expected = true;

        Assert.assertNotNull("The Boolean looked up is null", booolean);
        Assert.assertEquals(expected, booolean);
    }

    public void lookupByteEntry() {
        final Byte expected = (byte) 1;

        Assert.assertNotNull("The Byte looked up is null", byyte);
        Assert.assertEquals(expected, byyte);
    }

    public void lookupCharacterEntry() {
        final Character expected = 'D';

        Assert.assertNotNull("The Character looked up is null", chaaracter);
        Assert.assertEquals(expected, chaaracter);
    }

    public void lookupResource() {
        Assert.assertNotNull("The DataSource is null", daataSource);
    }

    public void lookupJMSConnectionFactory() {
        try {
            testJmsConnection(coonnectionFactory.createConnection());
            testJmsConnection(queueCoonnectionFactory.createConnection());
            testJmsConnection(topicCoonnectionFactory.createConnection());
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
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

    public void lookupPersistenceUnit() {
        Assert.assertNotNull("The EntityManagerFactory is null", emf);
    }

    public void lookupPersistenceContext() {
        Assert.assertNotNull("The EntityManager is null", em);

        try {
            // call a do nothing method to assure entity manager actually exists
            em.getFlushMode();
        } catch (final Exception e) {
            Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
}
