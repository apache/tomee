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

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class ManagedExecutorServiceTest {
    @EJB
    private ExecutorFacade facade;
    @Inject
    private CdiExecutorFacade cdiFacade;

    @Module
    public Class<?>[] bean() {
        return new Class<?>[]{ExecutorFacade.class, CdiExecutorFacade.class, RequestBean.class, MyCallable.class};
    }

    private ThreadContext ctx;

    @Before
    public void cleanUpContext() {
        ctx = ThreadContext.getThreadContext();
        ThreadContext.exit(null);
        RequestBean.ID = 0;
    }

    @After
    public void reset() {
        ThreadContext.enter(ctx);
    }

    @Test
    public void checkEjbContext() throws Exception {
        assertTrue(facade.submit().get());
        // assertEquals(1, RequestBean.ID); // CDI is opposed to it in the spirit
    }

    @Test
    public void checkCdiContext() throws Exception {
        assertTrue(cdiFacade.submit().get());
        // assertEquals(1, RequestBean.ID); // CDI is opposed to it in the spirit
    }

    @Test
    public void runnable() throws Exception {
        assertTrue(cdiFacade.submitRunnable());
    }

    @Singleton
    @Typed(ExecutorFacade.class)
    public static class ExecutorFacade extends CdiExecutorFacade {
        @Resource
        private ManagedExecutorService es;

        @Override
        public boolean isValid() {
            return ThreadContext.getThreadContext().getBeanContext().getBeanClass() == ExecutorFacade.class;
        }
    }

    public static class CdiExecutorFacade {
        protected static long id = -1;
        protected static CdiExecutorFacade current = null;

        @Resource
        private ManagedExecutorService es;

        @Inject
        private RequestBean bean;

        @Inject
        private MyCallable callable;

        public Future<Boolean> submit() {
            setContext();
            return es.submit(callable);
        }

        public boolean submitRunnable() {
            final CountDownLatch done = new CountDownLatch(1);
            es.submit(done::countDown);
            try {
                done.await();
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
            return true;
        }

        protected void setContext() {
            id = bean.getId();
            current = this;
        }

        public boolean isValid() {
            return ThreadContext.getThreadContext() == null;
        }
    }

    @Typed(MyCallable.class)
    public static class MyCallable implements Callable<Boolean> {
        /*@Inject
        private RequestBean bean;*/

        @Inject
        private BeanManager bm;

        @Override
        public Boolean call() throws Exception {
/*            final RequestBean currentRequestBean =
 *            RequestBean.class.cast(bm.getReference(bm.resolve(bm.getBeans(RequestBean.class)),
 *            RequestBean.class, null));*/
            return /*CdiExecutorFacade.id == bean.getId()
                    && CdiExecutorFacade.id == currentRequestBean.getId()
                    && */CdiExecutorFacade.current.isValid();
        }
    }

    @RequestScoped
    public static class RequestBean {
        private static int ID = 0;
        private final int id = ID++;

        public int getId() {
            return id;
        }
    }
}
