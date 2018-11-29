package org.apache.tomee.microprofile.faulttolerance;
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


import org.apache.safeguard.api.ExecutionManager;
import org.apache.safeguard.impl.FailsafeExecutionManager;
import org.apache.safeguard.impl.bulkhead.BulkheadManagerImpl;
import org.apache.safeguard.impl.cdi.FailsafeExecutionManagerProvider;
import org.apache.safeguard.impl.circuitbreaker.FailsafeCircuitBreakerManager;
import org.apache.safeguard.impl.config.MicroprofileAnnotationMapper;
import org.apache.safeguard.impl.executionPlans.ExecutionPlanFactory;
import org.apache.safeguard.impl.executorService.DefaultExecutorServiceProvider;
import org.apache.safeguard.impl.retry.FailsafeRetryManager;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.interceptor.Interceptor;

@Alternative
@Priority(Interceptor.Priority.APPLICATION + 10)
@ApplicationScoped
public class FailsafeContainerExecutionManagerProvider extends FailsafeExecutionManagerProvider {

    @Resource
    ManagedScheduledExecutorService executor;

    @Override
    @Produces
    @Specializes
    @ApplicationScoped
    public ExecutionManager createExecutionManager() {
        final MicroprofileAnnotationMapper mapper = MicroprofileAnnotationMapper.getInstance();
        final DefaultExecutorServiceProvider executorServiceProvider = new DefaultExecutorServiceProvider(executor);
        final BulkheadManagerImpl bulkheadManager = new BulkheadManagerImpl();
        final FailsafeCircuitBreakerManager circuitBreakerManager = new FailsafeCircuitBreakerManager();
        final FailsafeRetryManager retryManager = new FailsafeRetryManager();

        return new FailsafeExecutionManager(
                mapper,
                bulkheadManager,
                circuitBreakerManager,
                retryManager,
                new ExecutionPlanFactory(circuitBreakerManager, retryManager, bulkheadManager, mapper,
                        executorServiceProvider),
                executorServiceProvider);
    }
}