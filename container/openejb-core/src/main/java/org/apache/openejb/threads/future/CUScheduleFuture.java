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
package org.apache.openejb.threads.future;

import org.apache.openejb.threads.task.ManagedTaskListenerTask;

import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CUScheduleFuture<V> extends CUFuture<V, Delayed> implements ScheduledFuture<V> {
    public CUScheduleFuture(final ScheduledFuture<V> delegate, final ManagedTaskListenerTask listener) {
        super(delegate, listener);
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return ScheduledFuture.class.cast(delegate).getDelay(unit);
    }

    @Override
    public int compareTo(final Delayed o) {
        return ScheduledFuture.class.cast(delegate).compareTo(o);
    }
}
