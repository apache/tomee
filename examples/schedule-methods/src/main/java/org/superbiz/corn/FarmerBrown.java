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

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Schedules;
import jakarta.ejb.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is where we schedule all of Farmer Brown's corn jobs
 *
 * @version $Revision$ $Date$
 */
@Singleton
@Lock(LockType.READ) // allows timers to execute in parallel
public class FarmerBrown {

    private final AtomicInteger checks = new AtomicInteger();

    @Schedules({
            @Schedule(month = "5", dayOfMonth = "20-Last", minute = "0", hour = "8"),
            @Schedule(month = "6", dayOfMonth = "1-10", minute = "0", hour = "8")
    })
    private void plantTheCorn() {
        // Dig out the planter!!!
    }

    @Schedules({
            @Schedule(month = "9", dayOfMonth = "20-Last", minute = "0", hour = "8"),
            @Schedule(month = "10", dayOfMonth = "1-10", minute = "0", hour = "8")
    })
    private void harvestTheCorn() {
        // Dig out the combine!!!
    }

    @Schedule(second = "*", minute = "*", hour = "*")
    private void checkOnTheDaughters() {
        checks.incrementAndGet();
    }

    public int getChecks() {
        return checks.get();
    }
}
