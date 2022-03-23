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
package org.apache.openejb.core.singleton;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class AsyncPostContructTest {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @EJB
    private BuildMeAsync buildMeAsync;

    @Module
    public EnterpriseBean asyncConstructBean() {
        final SingletonBean singletonBean = new SingletonBean(BuildMeAsync.class);
        singletonBean.setInitOnStartup(true);
        return singletonBean.localBean();
    }

    @Test
    public void postConstructShouldEndsBeforeAsyncCall() {
        final long constructed = buildMeAsync.getStartEnd();
        final long async = buildMeAsync.getAsyncStart();
        assertTrue(async >= constructed);
        assertSame(buildMeAsync.getAsyncInstance(), buildMeAsync.getAsyncInstance());
    }

    @Startup
    @Singleton
    public static class BuildMeAsync {
        @Resource
        private SessionContext sc;

        private Future<Boolean> future;
        private final AtomicLong startEnd = new AtomicLong();
        private final AtomicLong asyncStart = new AtomicLong();
        private final AtomicReference<Object> startInstance = new AtomicReference<>();
        private final AtomicReference<Object> asyncInstance = new AtomicReference<>();

        @PostConstruct
        public void start() {
            startInstance.set(this);
            future = sc.getBusinessObject(BuildMeAsync.class).async();
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                // no-op
            }
            startEnd.set(System.nanoTime());
        }

        @Asynchronous
        public Future<Boolean> async() {
            asyncStart.set(System.nanoTime());
            asyncInstance.set(this);
            return new AsyncResult<>(true);
        }

        public void waitFuture() {
            try {
                future.get();
            } catch (final Exception e) {
                // no-op
            }
        }

        public long getStartEnd() {
            return startEnd.get();
        }

        public long getAsyncStart() {
            return asyncStart.get();
        }

        public Object getStartInstance() {
            return startInstance.get();
        }

        public Object getAsyncInstance() {
            return asyncInstance.get();
        }
    }
}
