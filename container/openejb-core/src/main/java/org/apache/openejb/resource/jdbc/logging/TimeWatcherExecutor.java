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
package org.apache.openejb.resource.jdbc.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public final class TimeWatcherExecutor {
    private TimeWatcherExecutor() {
        // no-op
    }

    public static TimerWatcherResult execute(final Method mtd, final Object instance, final Object[] args, boolean watch) throws Throwable {
        long start = 0, duration = 0;
        if (watch) {
            start = System.nanoTime();
        }

        final Object result;
        try {
            result = mtd.invoke(instance, args);
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }

        if (watch) {
            duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        }
        return new TimerWatcherResult(duration, result);
    }

    public static class TimerWatcherResult {
        private final Object result;
        private final long duration;

        public TimerWatcherResult(long duration, Object result) {
            this.duration = duration;
            this.result = result;
        }

        public Object getResult() {
            return result;
        }

        public long getDuration() {
            return duration;
        }
    }
}
