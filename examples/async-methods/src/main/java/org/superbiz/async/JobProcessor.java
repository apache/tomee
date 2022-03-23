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
package org.superbiz.async;

import jakarta.ejb.AccessTimeout;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Lock;
import jakarta.ejb.Singleton;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static jakarta.ejb.LockType.READ;

/**
 * @version $Revision$ $Date$
 */
@Singleton
public class JobProcessor {

    @Asynchronous
    @Lock(READ)
    @AccessTimeout(-1)
    public Future<String> addJob(String jobName) {

        // Pretend this job takes a while
        doSomeHeavyLifting();

        // Return our result
        return new AsyncResult<String>(jobName);
    }

    private void doSomeHeavyLifting() {
        try {
            Thread.sleep(SECONDS.toMillis(10));
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new IllegalStateException(e);
        }
    }
}
