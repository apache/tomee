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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;

@RunWith(ApplicationComposer.class)
public class SingleActionTimerTest {

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(SingleActionTimer.class).localBean();
    }

    @EJB
    private SingleActionTimer bean;

    @Test
    public void test() throws InterruptedException {
        Thread.sleep(1000);
        assertTrue("TimerWithDelay: Failed to count more than once", this.bean.getCount() > 1);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @Singleton
    @Startup
    @Lock(LockType.READ)
    public static class SingleActionTimer {

        private static final String TIMER_NAME = "SingleActionTimer";
        private final AtomicInteger counter = new AtomicInteger(0);
        private Timer timer;

        @Resource
        private TimerService timerService;

        @PostConstruct
        public void postConstruct() {

            try {

                this.createTimer();
                System.out.println("SingleActionTimer: Started initial timer");

            } catch (final Exception e) {
                throw new RuntimeException("SingleActionTimer: Failed to start initial timer", e);
            }

        }

        @PreDestroy
        public void preDestroy() {

            if (null != this.timer) {
                try {
                    this.timer.cancel();
                } catch (final Throwable e) {
                    //Ignore
                }
            }
        }

        private void createTimer() {
            try {
                this.timer = this.timerService.createSingleActionTimer(100, new TimerConfig(TIMER_NAME, false));
            } catch (final Exception e) {
                throw new RuntimeException("SingleActionTimer: Failed to create timer", e);
            }
        }

        @Timeout
        public void programmaticTimeout(final Timer timer) {

            if (!TIMER_NAME.equals(timer.getInfo())) {
                return;
            }

            final int i = this.counter.incrementAndGet();
            System.out.println("SingleActionTimer: Timeout " + i);

            this.createTimer();
        }

        public int getCount() {
            return this.counter.get();
        }
    }
}
