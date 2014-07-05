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
package org.apache.openejb.threads.reject;

import org.apache.openejb.threads.task.ManagedTaskListenerTask;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class CURejectHandler extends ThreadPoolExecutor.AbortPolicy {
    public static final RejectedExecutionHandler INSTANCE = new CURejectHandler();

    @Override
    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
        if (ManagedTaskListenerTask.class.isInstance(r)) {
            ManagedTaskListenerTask.class.cast(r).taskAborted(new RejectedExecutionException());
        }
        super.rejectedExecution(r, executor);
    }
}
