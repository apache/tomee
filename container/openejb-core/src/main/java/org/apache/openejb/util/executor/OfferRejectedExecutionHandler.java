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
package org.apache.openejb.util.executor;

import org.apache.openejb.util.Duration;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class OfferRejectedExecutionHandler implements RejectedExecutionHandler {

    private long timeout = 30;
    private TimeUnit seconds = TimeUnit.SECONDS;

    public OfferRejectedExecutionHandler(Duration duration) {
        this(duration.getTime(), duration.getUnit() == null ? TimeUnit.SECONDS : duration.getUnit());
    }

    public OfferRejectedExecutionHandler(long timeout, TimeUnit timeUnit) {
        if (timeout <= 0) throw new IllegalArgumentException("timeout must be greater than zero");
        if (timeUnit == null) throw new IllegalArgumentException("TimeUnit must not be null");

        this.timeout = timeout;
        this.seconds = timeUnit;
    }

    @Override
    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {

        if (null == r || null == tpe || tpe.isShutdown() || tpe.isTerminated() || tpe.isTerminating()) {
            return;
        }

        try {
            if (!tpe.getQueue().offer(r, timeout, seconds)) {
                throw new RejectedExecutionException("Timeout waiting for executor slot: waited " + timeout + " " + seconds.toString().toLowerCase());
            }
        } catch (InterruptedException e) {
            throw new RejectedExecutionException("Interrupted waiting for executor slot");
        }
    }
}
