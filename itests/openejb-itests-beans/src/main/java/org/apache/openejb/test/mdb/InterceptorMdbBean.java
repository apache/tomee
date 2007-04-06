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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.openejb.test.TestFailureException;

@Interceptors ({MdbInterceptor.class})
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination", propertyValue = "InterceptorMdbBean")})
public class InterceptorMdbBean implements MessageListener, MessageDrivenBean {

    private boolean classLevelBusinessMethodInterception = false;
    private boolean methodLevelBusinessMethodInterception = false;
    protected static boolean classLevelCreateMethodInterception = false;
    private boolean methodLevelCreateMethodInterception = false;
    private MessageDrivenContext mdbContext;
    private Session session;
    private Connection connection;
    protected MdbInvoker mdbInvoker;
    @Resource(name="jms", type=javax.jms.QueueConnectionFactory.class)
    private ConnectionFactory connectionFactory;

    public void onMessage(Message msg) {
        try {
            classLevelBusinessMethodInterception = msg.getBooleanProperty("ClassLevelBusinessMethodInterception");
            methodLevelBusinessMethodInterception = msg.getBooleanProperty("MethodLevelBusinessMethodInterception");
            try {
                msg.acknowledge();
            } catch (JMSException e) {
              e.printStackTrace();
            }
            mdbInvoker.onMessage(msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    public Object mdbInterceptor(InvocationContext ctx) throws Exception
    {
       Object[] objArr = ctx.getParameters();
       Message msg = (Message)objArr[0];
       msg.setBooleanProperty("MethodLevelBusinessMethodInterception",true);
       ctx.setParameters(objArr);
       return ctx.proceed();
    }

    @PostConstruct
    public void ejbCreate() throws EJBException
    {
        methodLevelCreateMethodInterception = true;
    }


    public void checkMethodLevelBusinessMethodInterception() throws TestFailureException{
        try {
            Assert.assertTrue("Method Level Business Method Interception failed for Mdb", methodLevelBusinessMethodInterception);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void checkMethodLevelCreateMethodInterception() throws TestFailureException{
        try {
            Assert.assertTrue("Method Level Business Method Interception failed for Mdb", methodLevelCreateMethodInterception);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }


    public void checkClassLevelBusinessMethodInterception() throws TestFailureException{
        try {
            Assert.assertTrue("Class Level Business Method Interception failed for Mdb", classLevelBusinessMethodInterception);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void checkClassLevelCreateMethodInterception() throws TestFailureException{
        try {
            Assert.assertTrue("Class Level Business Method Interception failed for Mdb", classLevelCreateMethodInterception);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }


    public void ejbRemove() throws EJBException {
        // TODO Auto-generated method stub

    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        this.mdbContext = ctx;
        try {
            mdbInvoker = new MdbInvoker(connectionFactory, this);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

}
