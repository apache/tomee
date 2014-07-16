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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class InitialIntervalTimerTest {
    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(TimerWithDelay.class).localBean();
    }

    @EJB
    private TimerWithDelay bean;

    @Test
    public void test() throws InterruptedException {
        Thread.sleep(5400);
        assertEquals(3, bean.getOk());
    }

    @Singleton
    @Startup
    @Lock(LockType.READ)
    public static class TimerWithDelay {
        @Resource
        private TimerService ts;

        private Timer timer;
        private int ok = 0;

        @PostConstruct
        public void start() {
            timer = ts.createIntervalTimer(3000, 1000, new TimerConfig(System.currentTimeMillis(), false));
        }

        @Timeout
        public void timeout(final Timer timer) {
            final long actual = System.currentTimeMillis() - ((Long) timer.getInfo() + 1000 * ok + 3000);
            assertEquals(0, actual, 500);
            ok++;
        }

        public int getOk() {
            return ok;
        }

        @PreDestroy
        public void stop() {
            timer.cancel();
        }
    }
}
