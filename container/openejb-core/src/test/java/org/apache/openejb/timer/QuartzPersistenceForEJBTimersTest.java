/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.timer;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.openejb.quartz.impl.jdbcjobstore.HSQLDBDelegate;
import org.apache.openejb.quartz.impl.jdbcjobstore.JobStoreCMT;
import org.apache.openejb.quartz.simpl.SimpleThreadPool;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Singleton;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import java.util.Calendar;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class QuartzPersistenceForEJBTimersTest {
    @EJB
    private MyTimedEjb bean;

    @Test(timeout = 60 * 1000 * 5) /* Timeout the test after 5min - should only require ~5-10sec to succeed */
    public void doTest() {
        assertEquals(1, bean.timers().size());
        bean.newTimer();
        assertEquals(2, bean.timers().size());
        while (!bean.awaitTimeout()) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                // no-op
            }
        }
        assertEquals(2, bean.timers().size());
    }

    @Module
    public AppModule application() {
        final EjbModule ejbModule = new EjbModule(new EjbJar());
        ejbModule.getEjbJar().addEnterpriseBean(new SingletonBean(MyTimedEjb.class).localBean());

        final Properties quartzConfig = new PropertiesBuilder()
            .p("org.apache.openejb.quartz.scheduler.instanceName", "TestScheduler")
            .p("org.apache.openejb.quartz.scheduler.instanceId", "AUTO")
            .p("org.apache.openejb.quartz.threadPool.class", SimpleThreadPool.class.getName())
            .p("org.apache.openejb.quartz.threadPool.threadCount", "4")
            .p("org.apache.openejb.quartz.threadPool.threadPriority", "5")
            .p("org.apache.openejb.quartz.jobStore.class", JobStoreCMT.class.getName())
            .p("org.apache.openejb.quartz.jobStore.driverDelegateClass", HSQLDBDelegate.class.getName())
            .p("org.apache.openejb.quartz.jobStore.dataSource", "QUARTZ")
            .p("org.apache.openejb.quartz.jobStore.nonManagedTXDataSource", "QUARTZ_NOTX")
            .p("org.apache.openejb.quartz.jobStore.tablePrefix", "qrtz_")
            .p("org.apache.openejb.quartz.jobStore.isClustered", "true")
            .p("org.apache.openejb.quartz.jobStore.clusterCheckinInterval", "60000")
            .p("org.apache.openejb.quartz.jobStore.txIsolationLevelSerializable", "true")
            .p("org.apache.openejb.quartz.jobStore.maxMisfiresToHandleAtATime", "100")
            .p("org.apache.openejb.quartz.dataSource.QUARTZ.jndiURL", "openejb:Resource/QuartzPersistenceForEJBTimersDB")
            .p("org.apache.openejb.quartz.dataSource.QUARTZ_NOTX.jndiURL", "openejb:Resource/QuartzPersistenceForEJBTimersDBNoTx")
            .build();


        final AppModule appModule = new AppModule(Thread.currentThread().getContextClassLoader(), null);
        appModule.getEjbModules().add(ejbModule);
        appModule.getProperties().putAll(quartzConfig);
        return appModule;
    }

    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder()
            // see src/test/resources/import-QuartzPersistenceForEJBTimersDB.sql for the init script
            .p("QuartzPersistenceForEJBTimersDB", "new://Resource?type=DataSource")
            .p("QuartzPersistenceForEJBTimersDB.JtaManaged", "true")
            .p("QuartzPersistenceForEJBTimersDB.JdbcUrl", "jdbc:hsqldb:mem:QuartzPersistenceForEJBTimersDB")
            .p("QuartzPersistenceForEJBTimersDB.UserName", "SA")
            .p("QuartzPersistenceForEJBTimersDB.Password", "")

                // see src/test/resources/import-QuartzPersistenceForEJBTimersDBNoTx-.sql for the init script
            .p("QuartzPersistenceForEJBTimersDBNoTx", "new://Resource?type=DataSource")
            .p("QuartzPersistenceForEJBTimersDBNoTx.JtaManaged", "false")
            .p("QuartzPersistenceForEJBTimersDBNoTx.JdbcUrl", "jdbc:hsqldb:mem:QuartzPersistenceForEJBTimersDB")
            .p("QuartzPersistenceForEJBTimersDBNoTx.UserName", "SA")
            .p("QuartzPersistenceForEJBTimersDBNoTx.Password", "")
            .build();
    }

    @Singleton
    public static class MyTimedEjb {
        @Resource
        private TimerService timerService;

        private Timer timer = null;
        private final Semaphore sema = new Semaphore(0);

        @Timeout
        public void timeout(final Timer timer) {
            System.out.println("@Timeout on " + timer.getInfo());
            sema.release();
        }

        public boolean awaitTimeout() {
            try {
                return sema.tryAcquire(1, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                // no-op
            }
            return false;
        }

        public Collection<Timer> timers() {
            return timerService.getTimers();
        }

        @PreDestroy
        public void stop() {
            if (timer != null) {
                timer.cancel();
            }
        }

        @Schedule
        public void justToCheckZeroTimersInListAtStartup() {
            // no-op
        }

        public void newTimer() {
            final TimerConfig tc = new TimerConfig("my-timer", true);
            final ScheduleExpression se = new ScheduleExpression();
            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 2);
            se.second(calendar.get(Calendar.SECOND) + "/3");
            se.minute("*");
            se.hour("*");
            se.dayOfMonth("*");
            se.dayOfWeek("*");
            se.month("*");
            timer = timerService.createCalendarTimer(se, tc);
        }
    }
}
