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
package org.apache.openejb.core.mdb;

import org.junit.Assert;
import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ConnectorModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.InboundResourceadapter;
import org.apache.openejb.jee.MessageAdapter;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.resource.quartz.JobSpec;
import org.apache.openejb.resource.quartz.QuartzResourceAdapter;
import org.apache.openejb.quartz.Job;
import org.apache.openejb.quartz.JobExecutionContext;
import org.apache.openejb.quartz.JobExecutionException;
import org.junit.AfterClass;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.MessageDrivenContext;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class QuartzMdbContainerTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Setup the descriptor information

        CronBean.lifecycle.clear();


        final AppModule app = new AppModule(this.getClass().getClassLoader(), "testapp");

        final Connector connector = new Connector("email-ra");
        final ResourceAdapter adapter = new ResourceAdapter(QuartzResourceAdapter.class);
        connector.setResourceAdapter(adapter);
        final InboundResourceadapter inbound = adapter.setInboundResourceAdapter(new InboundResourceadapter());
        final MessageAdapter messageAdapter = inbound.setMessageAdapter(new MessageAdapter());
        final MessageListener listener = messageAdapter.addMessageListener(new MessageListener(Job.class, JobSpec.class));
        listener.getActivationSpec().addRequiredConfigProperty("cronExpression");
        app.getConnectorModules().add(new ConnectorModule(connector));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(CronBean.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        final AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);


        assertTrue(CronBean.latch.await(5, TimeUnit.SECONDS));

        final Stack<Lifecycle> lifecycle = CronBean.lifecycle;

        final List expected = Arrays.asList(Lifecycle.values());

        Assert.assertEquals(expected.get(0), lifecycle.get(0));
        Assert.assertEquals(expected.get(1), lifecycle.get(1));

    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, ON_MESSAGE
    }

    @MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "cronExpression", propertyValue = "* * * * * ?")})
    public static class CronBean implements Job {

        public static CountDownLatch latch = new CountDownLatch(1);

        private static final Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public CronBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        @Resource
        public void setMessageDrivenContext(final MessageDrivenContext messageDrivenContext) {
            lifecycle.push(Lifecycle.INJECTION);
        }

        @PostConstruct
        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
            lifecycle.push(Lifecycle.ON_MESSAGE);
            latch.countDown();
        }
    }
}