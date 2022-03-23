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
package org.apache.openejb.core.timer;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateful;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(ApplicationComposer.class)
public class GetAllTimersTest {
    @Module
    public EjbJar jar() {
        return new EjbJar()
            .enterpriseBean(new StatefulBean(NoTimer.class).localBean())
            .enterpriseBean(new SingletonBean(Query.class).localBean())
            .enterpriseBean(new SingletonBean(Bean1.class).localBean())
            .enterpriseBean(new SingletonBean(Bean2.class).localBean());
    }

    @EJB
    private Query query;

    @Test
    public void noTimerForABeanWhichDidntCreatedAny() {
        assertNull(query.empty());
    }

    @Test
    public void findAllTimersInATx() {
        checkList(query.listInTx());
    }

    @Test
    public void findAllTimersWithoutTx() {
        checkList(query.list());
    }

    private void checkList(final Collection<Timer> in) {
        final List<Timer> list = new ArrayList<>(in); list.sort(new Comparator<Timer>() {
            @Override
            public int compare(final Timer o1, final Timer o2) {
                return o1.getInfo().toString().compareTo(o2.getInfo().toString());
            }
        });
        assertEquals(2, list.size());
        final Iterator<Timer> it = list.iterator();
        assertEquals(Bean1.class.getSimpleName(), it.next().getInfo());
        assertEquals(Bean2.class.getSimpleName(), it.next().getInfo());
    }

    @Stateful
    public static class NoTimer {
        public void justHereToEnsureWeDontFailIfABeanHasNoTimer() {
            // no-op
        }
    }

    @Startup
    public static class TimerBean {
        @Resource
        private TimerService ts;

        @PostConstruct
        public void run() {
            ts.createSingleActionTimer(new Date(System.currentTimeMillis() + 100000L), new TimerConfig(getClass().getSimpleName(), false));
        }

        @Timeout
        public void timeout(final Timer t) {
            System.out.println(t.getInfo());
        }
    }

    @Singleton
    public static class Bean1 extends TimerBean {
    }

    @Singleton
    public static class Bean2 extends TimerBean {
    }

    @Singleton
    public static class Query {
        @Resource
        private TimerService ts;

        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public Collection<Timer> listInTx() {
            return ts.getAllTimers();
        }

        @TransactionAttribute(TransactionAttributeType.NEVER)
        public Collection<Timer> list() {
            return ts.getAllTimers();
        }

        public Collection<Timer> empty() {
            return ts.getTimers();
        }
    }
}
