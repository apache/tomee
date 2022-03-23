/*
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
package org.apache.openejb.timer;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class TransactionRegistryInTimeoutTest {
    @EJB
    private EjbTimerTx ejb;

    @Test
    public void run() {
        ejb.setTimerNow();
        try {
            ejb.latch().await(1, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            Thread.interrupted();
            fail(e.getMessage());
        }
        final TxListener sync = ejb.sync();
        assertNotNull(sync);
        assertTrue(sync.done > 0);
    }

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(EjbTimerTx.class).localBean();
    }

    @Singleton
    @Lock(LockType.READ)
    public static class EjbTimerTx {
        @Resource
        private TimerService timerService;

        @Resource
        private TransactionSynchronizationRegistry registry;

        @Resource
        private SessionContext context;

        private final CountDownLatch latch = new CountDownLatch(1);
        private final TxListener sync = new TxListener(latch);

        @Timeout
        @Lock(LockType.WRITE)
        public void timerFired() {
            try {
                final String jndi = "java:comp/TransactionSynchronizationRegistry";
                final TransactionSynchronizationRegistry txRegistry = (TransactionSynchronizationRegistry) new InitialContext().lookup(jndi);
                assertNotNull(txRegistry);
                assertNotNull(context.lookup(jndi));
                assertNotNull(registry);
                txRegistry.registerInterposedSynchronization(sync);
            } catch (final NamingException e) {
                throw new IllegalStateException(e);
            }
        }

        public TxListener sync() {
            return sync;
        }

        public CountDownLatch latch() {
            return latch;
        }

        public Timer setTimerNow() {
            final ScheduleExpression schedule = new ScheduleExpression().second("*/3").minute("*").hour("*");

            final TimerConfig timerConfig = new TimerConfig();
            timerConfig.setPersistent(false);

            return timerService.createCalendarTimer(schedule, timerConfig);
        }
    }

    public static class TxListener implements Synchronization {
        public volatile int done = 0;
        private final CountDownLatch latch;

        public TxListener(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void beforeCompletion() {
            // no-op
        }

        @Override
        public void afterCompletion(final int i) {
            try {
                final TransactionSynchronizationRegistry txRegistry = (TransactionSynchronizationRegistry) new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
                assertNotNull(txRegistry);
                done++;
                latch.countDown();
            } catch (final NamingException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
