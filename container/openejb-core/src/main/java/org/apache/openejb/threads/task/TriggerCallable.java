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
package org.apache.openejb.threads.task;

import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;

import jakarta.enterprise.concurrent.Trigger;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class TriggerCallable<V> extends TriggerTask<V> implements Callable<V> {
    private final Callable<V> delegate;

    public TriggerCallable(final ManagedScheduledExecutorServiceImpl es,
                           final Callable<V> original, final Callable<V> wrap,
                           final Trigger trigger, final Date taskScheduledTime, final String id,
                           final AtomicReference<Future<V>> ref) {
        super(original, es, trigger, taskScheduledTime, id, ref);
        this.delegate = wrap;
    }

    @Override
    public V call() throws Exception {
        return invoke();
    }

    @Override
    protected V doInvoke() throws Exception {
        return delegate.call();
    }
}
