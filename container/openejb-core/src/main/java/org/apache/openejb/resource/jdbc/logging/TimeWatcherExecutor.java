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

    public static String inlineStack(final String[] acceptedPackages) {
        if (acceptedPackages == null) {
            return "";
        }

        final Throwable t = new Exception().fillInStackTrace();
        final StringBuilder inlinedStack = new StringBuilder();
        for (final StackTraceElement elt : t.getStackTrace()) {
            final String className = elt.getClassName();
            for (final String p : acceptedPackages) {
                if (className.startsWith(p)) {
                    inlinedStack.append(" -> ")
                            .append(className).append('.')
                            .append(elt.getMethodName()).append(':').append(elt.getLineNumber());
                    break;
                }
            }
        }
        return inlinedStack.toString();
    }

    public static TimerWatcherResult execute(final Method mtd, final Object instance, final Object[] args, final boolean watch) {
        final long start = (watch) ? System.nanoTime() : 0;

        try {

            final Object result = mtd.invoke(instance, args);

            return new TimerWatcherResult(start, result, null);

        } catch (final InvocationTargetException ite) {

            return new TimerWatcherResult(start, null, ite.getCause());

        } catch (final Throwable throwable) {

            return new TimerWatcherResult(start, null, throwable);
        }
    }

    public static class TimerWatcherResult {
        private final Object result;
        private final Throwable throwable;
        private final long duration;

        public TimerWatcherResult(final long start, final Object result, final Throwable throwable) {
            this.duration = (start == 0) ? 0 : TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            this.result = result;
            this.throwable = throwable;
        }

        public String format(final String query) {
            String message = query + " --> " + this.getDuration() + "ms";

            if (throwable != null) {
                message += " - FAILED";
            }

            return message;
        }

        public Object getResult() {
            return result;
        }

        public long getDuration() {
            return duration;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }
}
