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
package org.superbiz.accesstimeout;

import jakarta.ejb.AccessTimeout;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Lock;
import jakarta.ejb.Singleton;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static jakarta.ejb.LockType.WRITE;

/**
 * @version $Revision$ $Date$
 */
@Singleton
@Lock(WRITE)
public class BusyBee {

    @Asynchronous
    public Future stayBusy(CountDownLatch ready) {
        ready.countDown();

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        return null;
    }

    @AccessTimeout(0)
    public void doItNow() {
        // do something
    }

    @AccessTimeout(value = 5, unit = TimeUnit.SECONDS)
    public void doItSoon() {
        // do something
    }

    @AccessTimeout(-1)
    public void justDoIt() {
        // do something
    }

}
