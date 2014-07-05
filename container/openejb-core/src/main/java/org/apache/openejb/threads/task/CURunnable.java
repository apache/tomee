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

import java.util.concurrent.Callable;

public class CURunnable extends CUTask<Void> implements Runnable {
    private final Runnable delegate;

    public CURunnable(final Runnable task) {
        super(task);
        delegate = task;
    }

    @Override
    public void run() {
        try {
            invoke(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    delegate.run();
                    return null;
                }
            });
        } catch (final RuntimeException re) {
            throw re;
        } catch (final Exception e) {
            // can't happen normally since not in the contract of Runnable
        }
    }
}
