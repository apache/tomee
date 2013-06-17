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
package org.apache.openejb.concurrencyutilities.test;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class ManagedExecutorServiceTest {
    @Module
    public Class<?>[] bean() {
        return new Class<?>[] { ExecutorFacade.class, CdiExecutorFacade.class };
    }

    @EJB
    private ExecutorFacade facade;

    @Inject
    private CdiExecutorFacade cdiFacade;

    @Before
    public void cleanUpContext() {
        ThreadContext.exit(null);
    }

    @Test
    public void checkEjbContext() throws Exception {
        assertTrue(facade.submit().get());
    }

    @Test
    public void checkCdiContext() throws Exception {
        assertTrue(cdiFacade.submit().get());
    }

    @Singleton
    @Typed(ExecutorFacade.class)
    public static class ExecutorFacade extends CdiExecutorFacade {
        @Resource
        private ManagedExecutorService es;

        @Override
        protected boolean isValid() {
            return ThreadContext.getThreadContext().getBeanContext().getBeanClass() == ExecutorFacade.class;
        }
    }

    public static class CdiExecutorFacade {
        @Resource
        private ManagedExecutorService es;

        public Future<Boolean> submit() {
            return es.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return isValid();
                }
            });
        }

        protected boolean isValid() {
            return ThreadContext.getThreadContext() == null;
        }
    }
}
