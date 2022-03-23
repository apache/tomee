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

import org.junit.Assert;
import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.MessageDrivenContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;

@Interceptors({MdbInterceptor.class})
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "InterceptorMdbBean")})
public class InterceptorMdbBean implements MessageListener, MessageDrivenBean {

    private boolean classLevelBusinessMethodInterception = false;
    private boolean methodLevelBusinessMethodInterception = false;
    protected static boolean classLevelCreateMethodInterception = false;
    private boolean methodLevelCreateMethodInterception = false;
    private MessageDrivenContext mdbContext;
    private Session session;
    private Connection connection;
    protected MdbInvoker mdbInvoker;
    @Resource(name = "jms", type = jakarta.jms.QueueConnectionFactory.class)
    private ConnectionFactory connectionFactory;

    @Override
    public void onMessage(final Message msg) {
        try {
            classLevelBusinessMethodInterception = msg.getBooleanProperty("ClassLevelBusinessMethodInterception");
            methodLevelBusinessMethodInterception = msg.getBooleanProperty("MethodLevelBusinessMethodInterception");
            try {
                msg.acknowledge();
            } catch (final JMSException e) {
                e.printStackTrace();
            }
            mdbInvoker.onMessage(msg);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {
        final Object[] objArr = ctx.getParameters();
        final Message msg = (Message) objArr[0];
        msg.setBooleanProperty("MethodLevelBusinessMethodInterception", true);
        ctx.setParameters(objArr);
        return ctx.proceed();
    }

    @PostConstruct
    public void ejbCreate() throws EJBException {
        methodLevelCreateMethodInterception = true;
    }


    public void checkMethodLevelBusinessMethodInterception() throws TestFailureException {
        try {
            Assert.assertTrue("Method Level Business Method Interception failed for Mdb", methodLevelBusinessMethodInterception);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void checkMethodLevelCreateMethodInterception() throws TestFailureException {
        try {
            Assert.assertTrue("Method Level Business Method Interception failed for Mdb", methodLevelCreateMethodInterception);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }


    public void checkClassLevelBusinessMethodInterception() throws TestFailureException {
        try {
            Assert.assertTrue("Class Level Business Method Interception failed for Mdb", classLevelBusinessMethodInterception);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void checkClassLevelCreateMethodInterception() throws TestFailureException {
        try {
            Assert.assertTrue("Class Level Business Method Interception failed for Mdb", classLevelCreateMethodInterception);
        } catch (final AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }


    @Override
    public void ejbRemove() throws EJBException {
        // TODO Auto-generated method stub

        if (null != mdbInvoker) {
            mdbInvoker.destroy();
        }
    }

    @Override
    public void setMessageDrivenContext(final MessageDrivenContext ctx) throws EJBException {
        this.mdbContext = ctx;
        try {
            mdbInvoker = new MdbInvoker(connectionFactory, this);
        } catch (final Exception e) {
            throw new EJBException(e);
        }
    }

}
