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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.schedule.events;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.ejb.AccessTimeout;
import jakarta.ejb.EJB;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.enterprise.event.Observes;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @version $Revision$ $Date$
 */
public class SchedulerTest {

    public static final CountDownLatch events = new CountDownLatch(3);

    @EJB
    private Scheduler scheduler;

    @Test
    public void test() throws Exception {

        final ScheduleExpression schedule = new ScheduleExpression()
                .hour("*")
                .minute("*")
                .second("*/5");

        scheduler.scheduleEvent(schedule, new TestEvent("five"));

        Assert.assertTrue(events.await(1, TimeUnit.MINUTES));
    }

    @AccessTimeout(value = 1, unit = TimeUnit.MINUTES)
    public void observe(@Observes TestEvent event) {
        if ("five".equals(event.getMessage())) {
            events.countDown();
        }
    }

    public static class TestEvent {

        private final String message;

        public TestEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Before
    public void setup() throws Exception {
        EJBContainer.createEJBContainer().getContext().bind("inject", this);
    }
}
