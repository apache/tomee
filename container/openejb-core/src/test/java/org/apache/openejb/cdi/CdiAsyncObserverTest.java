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
package org.apache.openejb.cdi;

import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@RunWith(ApplicationComposer.class)
public class CdiAsyncObserverTest {
    private static final AtomicInteger eventFired = new AtomicInteger(0);
    private static final AtomicInteger eventReceived = new AtomicInteger(0);

    @EJB
    private WorkBean workBean;

    @Test
    public void test() throws Exception {
        workBean.doWork();

        Assert.assertEquals(1, eventFired.get());

        retry(2, 1000, () -> {
            Assert.assertEquals(1, eventReceived.get());
        });
    }

    @Module
    @Classes(cdi = true, value = {WorkBean.class, MyEvent.class, Listener.class})
    public EjbJar jar() {
        return new EjbJar("cdi-async-observer");
    }


    @Singleton
    @Lock(LockType.READ)
    public static class WorkBean {
        private static final Logger LOGGER = Logger.getLogger(WorkBean.class.getName());

        @Inject
        private Event<MyEvent> event;

        public void doWork() {
            LOGGER.info("Executing doWork()");
            event.fireAsync(new MyEvent("Executed doWork()"));
            eventFired.incrementAndGet();
        }
    }


    public static class MyEvent {
        private final String details;

        public MyEvent(final String details) {
            this.details = details;
        }

        public String getDetails() {
            return details;
        }
    }


    @ApplicationScoped
    public static class Listener {
        private static final Logger LOGGER = Logger.getLogger(Listener.class.getName());

        public void observer(final @ObservesAsync MyEvent event) throws Exception{
            final TransactionManager txMgr = SystemInstance.get().getComponent(TransactionManager.class);
            Assert.assertNotNull(txMgr);
            Assert.assertNull(txMgr.getTransaction());
            LOGGER.info("Received async event: " + event.getDetails());
            eventReceived.incrementAndGet();
        }
    }

    private void retry(final int maxRetries, final int retryDelay, final NoArgExceptionFunction func) throws Exception {
        int retry = 0;
        while (true) {
            try {
                func.run();
                break;
            } catch (Error | Exception err) {
                retry++;
                if (retry >= maxRetries) {
                    throw err;
                }
            }

            try {
                Thread.sleep(retryDelay);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @FunctionalInterface
    public interface NoArgExceptionFunction {
        public abstract void run() throws Exception;
    }
}
