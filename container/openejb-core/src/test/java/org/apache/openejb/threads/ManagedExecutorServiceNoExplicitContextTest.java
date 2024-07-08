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
package org.apache.openejb.threads;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.threads.impl.ManagedExecutorServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.NamingException;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class ManagedExecutorServiceNoExplicitContextTest {
    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder()

            // don't set concurrent/es.context, it needs to resolve to the default ContextService in this case

            .property("concurrent/es", "new://Resource?type=ManagedExecutorService")
            .property("concurrent/es.core", "2")
            .property("concurrent/es.max", "10")
            .property("concurrent/es.keepAlive", "5 s")
            .property("concurrent/es.queue", "3")
            .property("concurrent/es.lazy", "True")
            .property("concurrent/es.threadFactory", "org.apache.openejb.threads.impl.ManagedThreadFactoryImpl")

            .build();
    }

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(CustomCUBean.class).localBean();
    }

    @Resource(name = "concurrent/es")
    private ManagedExecutorService es;

    @EJB
    private CustomCUBean bean;

    @Test
    public void checkInjections() throws NamingException {
        assertNotNull(es);

        doCheck(es);
        bean.check();
    }

    private static void doCheck(final ManagedExecutorService es) {
        assertNotNull(es);

        assertThat(es, instanceOf(ManagedExecutorServiceImpl.class));
        assertEquals(2, pool(es).getCorePoolSize());
        assertEquals(10, pool(es).getMaximumPoolSize());
        assertEquals(5, pool(es).getKeepAliveTime(TimeUnit.SECONDS));
    }

    private static ThreadPoolExecutor pool(final ManagedExecutorService es) {
        return ThreadPoolExecutor.class.cast(ManagedExecutorServiceImpl.class.cast(es).getDelegate());
    }

    @Singleton
    public static class CustomCUBean {
        @Resource(name = "concurrent/es")
        private ManagedExecutorService es;

        public void check() {
            doCheck(es);
        }
    }
}
