package org.superbiz.executor;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.RequestScoped;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.Objects.nonNull;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@RequestScoped
public class ThreadFactoryService {

    private static final Logger LOGGER = Logger.getLogger(ThreadFactoryService.class.getSimpleName());

    @Resource
    private ManagedThreadFactory factory;

    public void asyncTask(final int value) {
        LOGGER.info("Create asyncTask");
        final Thread thread = factory.newThread(longRunnableTask(value, 100, null));
        thread.setName("pretty asyncTask");
        thread.start();
    }

    /**
     * Will simulate a long running operation
     *
     * @param value          The value to compute
     * @param taskDurationMs the time length of the operation
     * @param errorMessage   If not null an exception with be thrown with this message
     * @return a {@link Runnable}
     */
    private Runnable longRunnableTask(final int value,
                                      final int taskDurationMs,
                                      final String errorMessage) {
        return () -> {
            if (nonNull(errorMessage)) {
                LOGGER.severe("Exception will be thrown");
                throw new RuntimeException(errorMessage);
            }
            try {
                // Simulate a long processing task using TimeUnit to sleep.
                TimeUnit.MILLISECONDS.sleep(taskDurationMs);
            } catch (InterruptedException e) {
                throw new RuntimeException("Problem while waiting");
            }

            Integer result = value + 1;
            LOGGER.info("longRunnableTask complete. Value is " + result);
            // Cannot return result with a Runnable.
        };
    }

}
