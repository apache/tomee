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

package org.apache.openejb.threads;

import org.apache.openejb.resource.thread.ManagedScheduledExecutorServiceImplFactory;
import org.apache.openejb.threads.impl.ManagedScheduledExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ManagedScheduledExecutorServiceImplFactoryTest {

    @Test
    public void createServiceTest() {
        final ManagedScheduledExecutorServiceImplFactory factory = new ManagedScheduledExecutorServiceImplFactory();
        factory.setThreadFactory(MyThreadFactory.class.getName());
        final ManagedScheduledExecutorServiceImpl executorService = factory.create();
        final ScheduledThreadPoolExecutor poolExecutor = (ScheduledThreadPoolExecutor) executorService.getDelegate();
        Assert.assertEquals(poolExecutor.getThreadFactory().getClass(), ManagedThreadFactoryImpl.class);
    }

    public static class MyThreadFactory {
        public MyThreadFactory() {
            throw new RuntimeException("Throwing test exception in MyThreadFactory - I expect to see this logged as a warning!");
        }
    }

}
