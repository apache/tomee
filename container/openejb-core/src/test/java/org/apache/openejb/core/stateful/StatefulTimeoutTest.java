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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.Timeout;

import jakarta.ejb.Local;
import jakarta.ejb.NoSuchEJBException;
import jakarta.ejb.Stateful;
import javax.naming.InitialContext;
import java.util.concurrent.TimeUnit;

public class StatefulTimeoutTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
            LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final StatefulSessionContainerInfo statefulContainerInfo = config
            .configureService(StatefulSessionContainerInfo.class);
        statefulContainerInfo.properties.setProperty("BulkPassivate", "1");
        // clear cache every 3 seconds
        statefulContainerInfo.properties.setProperty("Frequency", "3");
        assembler.createContainer(statefulContainerInfo);

        final EjbJar ejbJar = new EjbJar();
        Timeout timeout;

        final StatefulBean bean1 = new StatefulBean("BeanNegative", MyLocalBeanImpl.class);
        timeout = new Timeout();
        timeout.setTimeout(-1);
        timeout.setUnit(TimeUnit.SECONDS);
        bean1.setStatefulTimeout(timeout);

        final StatefulBean bean0 = new StatefulBean("BeanZero", MyLocalBeanImpl.class);
        timeout = new Timeout();
        timeout.setTimeout(0);
        timeout.setUnit(TimeUnit.SECONDS);
        bean0.setStatefulTimeout(timeout);

        final StatefulBean bean5 = new StatefulBean("Bean", MyLocalBeanImpl.class);
        timeout = new Timeout();
        timeout.setTimeout(5);
        timeout.setUnit(TimeUnit.SECONDS);
        bean5.setStatefulTimeout(timeout);

        ejbJar.addEnterpriseBean(bean1);
        ejbJar.addEnterpriseBean(bean0);
        ejbJar.addEnterpriseBean(bean5);

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void testZeroTimeout() throws Exception {
        final InitialContext ctx = new InitialContext();
        final MyLocalBean bean;

        // cache is cleared ever 3 seconds and bean timeout is 0 seconds
        bean = (MyLocalBean) ctx.lookup("BeanZeroLocal");
        bean.doNothing(0);

        // cache should be cleared by now and the bean should be removed
        Thread.sleep(5 * 1000);
        try {
            bean.doNothing(0);
            fail("Did not throw expected exception");
        } catch (final NoSuchEJBException e) {
            // that's what we expect
        }
    }

    public void testTimeout() throws Exception {
        final InitialContext ctx = new InitialContext();
        final MyLocalBean bean;

        // cache is cleared ever 3 seconds and bean timeout is 5 seconds
        bean = (MyLocalBean) ctx.lookup("BeanLocal");
        bean.doNothing(0);

        // cache should be cleared once by now but the bean is not expired yet
        Thread.sleep(5 * 1000);
        bean.doNothing(0);

        // cache should be cleared again and our bean should be removed
        // since the bean was idle for more than 5 seconds.
        Thread.sleep(10 * 1000);
        try {
            bean.doNothing(0);
            fail("Did not throw expected exception");
        } catch (final NoSuchEJBException e) {
            // that's what we expect
        }
    }

    public void testNegativeTimeout() throws Exception {
        final InitialContext ctx = new InitialContext();
        final MyLocalBean bean;

        // cache is cleared ever 3 seconds and bean timeout is -1 seconds
        bean = (MyLocalBean) ctx.lookup("BeanNegativeLocal");
        bean.doNothing(0);

        // cache should be cleared by now a few times but bean should remain
        // available.
        Thread.sleep(10 * 1000);
        bean.doNothing(0);
    }

    @Local
    public static interface MyLocalBean {
        void doNothing(long sleep);
    }

    @Stateful
    public static class MyLocalBeanImpl implements MyLocalBean {
        public void doNothing(final long sleep) {
            try {
                Thread.sleep(sleep);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
