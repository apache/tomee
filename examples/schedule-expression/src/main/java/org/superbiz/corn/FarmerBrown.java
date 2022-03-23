/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.corn;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is where we schedule all of Farmer Brown's corn jobs
 *
 * @version $Revision$ $Date$
 */
@Singleton
@Lock(LockType.READ) // allows timers to execute in parallel
@Startup
public class FarmerBrown {

    private final AtomicInteger checks = new AtomicInteger();

    @Resource
    private TimerService timerService;

    @PostConstruct
    private void construct() {
        final TimerConfig plantTheCorn = new TimerConfig("plantTheCorn", false);
        timerService.createCalendarTimer(new ScheduleExpression().month(5).dayOfMonth("20-Last").minute(0).hour(8), plantTheCorn);
        timerService.createCalendarTimer(new ScheduleExpression().month(6).dayOfMonth("1-10").minute(0).hour(8), plantTheCorn);

        final TimerConfig harvestTheCorn = new TimerConfig("harvestTheCorn", false);
        timerService.createCalendarTimer(new ScheduleExpression().month(9).dayOfMonth("20-Last").minute(0).hour(8), harvestTheCorn);
        timerService.createCalendarTimer(new ScheduleExpression().month(10).dayOfMonth("1-10").minute(0).hour(8), harvestTheCorn);

        final TimerConfig checkOnTheDaughters = new TimerConfig("checkOnTheDaughters", false);
        timerService.createCalendarTimer(new ScheduleExpression().second("*").minute("*").hour("*"), checkOnTheDaughters);
    }

    @Timeout
    public void timeout(Timer timer) {
        if ("plantTheCorn".equals(timer.getInfo())) {
            plantTheCorn();
        } else if ("harvestTheCorn".equals(timer.getInfo())) {
            harvestTheCorn();
        } else if ("checkOnTheDaughters".equals(timer.getInfo())) {
            checkOnTheDaughters();
        }
    }

    private void plantTheCorn() {
        // Dig out the planter!!!
    }

    private void harvestTheCorn() {
        // Dig out the combine!!!
    }

    private void checkOnTheDaughters() {
        checks.incrementAndGet();
    }

    public int getChecks() {
        return checks.get();
    }
}
