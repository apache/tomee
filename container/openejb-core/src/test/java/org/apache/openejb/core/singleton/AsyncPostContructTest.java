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

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.concurrent.Future;

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class AsyncPostContructTest {
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
        final long start = buildMeAsync.getStartEnd();
        final long async = buildMeAsync.getAsyncStart();
        assertTrue(async > start);
        assertSame(buildMeAsync.getAsyncInstance(), buildMeAsync.getAsyncInstance());
    }

    @Startup
    @Singleton
    public static class BuildMeAsync {
        @Resource
        private SessionContext sc;

        private Future<Boolean> future;
        private long startEnd;
        private long asyncStart;
        private Object startInstance;
        private Object asyncInstance;

        @PostConstruct
        public void start() {
            startInstance = this;
            future = sc.getBusinessObject(BuildMeAsync.class).async();
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                // no-op
            }
            startEnd = System.currentTimeMillis();
        }

        @Asynchronous
        public Future<Boolean> async() {
            asyncStart = System.currentTimeMillis();
            asyncInstance = this;
            return new AsyncResult<Boolean>(true);
        }

        public void waitFuture() {
            try {
                future.get();
            } catch (final Exception e) {
                // no-op
            }
        }

        public long getStartEnd() {
            return startEnd;
        }

        public long getAsyncStart() {
            return asyncStart;
        }

        public Object getStartInstance() {
            return startInstance;
        }

        public Object getAsyncInstance() {
            return asyncInstance;
        }
    }
}
