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

import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ManagedExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.InitialContext;
import jakarta.transaction.UserTransaction;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class InjectionTest {
    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(CUBean.class).localBean();
    }

    @Resource
    private ManagedExecutorService es;

    @Resource
    private ManagedScheduledExecutorService ses;

    @Resource
    private ContextService ces;

    @Resource
    private ManagedThreadFactory tf;

    @EJB
    private CUBean bean;

    private ThreadContext ctx;

    @Before
    public void cleanUpContext() {
        ctx = ThreadContext.getThreadContext();
        ThreadContext.exit(null);
    }

    @After
    public void reset() {
        ThreadContext.enter(ctx);
    }

    @Test
    public void checkInjections() {
        doCheck(es, ses, ces, tf);

        bean.checkNotNull();
    }

    @Test
    public void checkUserTransactionIsAccessible() throws Exception {
        assertTrue(es.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    final Object ut = new InitialContext().lookup("java:comp/UserTransaction");
                    assertThat(ut, instanceOf(UserTransaction.class));
                    return true;
                } catch (final Exception e) {
                    return false;
                }
            }
        }).get());
    }

    private static void doCheck(final ManagedExecutorService es, final ManagedScheduledExecutorService ses,
                                final ContextService ces, final ManagedThreadFactory tf) {
        assertNotNull(es);
        assertNotNull(ses);
        assertNotNull(ces);
        assertNotNull(tf);

        assertThat(es, instanceOf(ManagedExecutorServiceImpl.class));
        assertThat(ses, instanceOf(ManagedScheduledExecutorServiceImpl.class));
        assertThat(ces, instanceOf(ContextServiceImpl.class));
        assertThat(tf, instanceOf(ManagedThreadFactoryImpl.class));
    }

    @Singleton
    public static class CUBean {
        @Resource
        private ManagedExecutorService es;

        @Resource
        private ManagedScheduledExecutorService ses;

        @Resource
        private ContextService ces;

        @Resource
        private ManagedThreadFactory tf;

        public void checkNotNull() {
            doCheck(es, ses, ces, tf);
        }
    }
}
