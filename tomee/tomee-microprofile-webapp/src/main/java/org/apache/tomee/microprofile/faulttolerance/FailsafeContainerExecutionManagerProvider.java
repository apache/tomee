package org.apache.tomee.microprofile.faulttolerance;
/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2018
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Specializes;
import javax.naming.InitialContext;
import javax.ws.rs.Produces;
import java.util.concurrent.ScheduledExecutorService;

@Specializes
@Alternative
@ApplicationScoped
public class FailsafeContainerExecutionManagerProvider extends FailsafeExecutionManagerProvider {

    @Produces
    @ApplicationScoped
    @Override
    public ExecutionManager createExecutionManager() throws Exception {

        ScheduledExecutorService executor = new InitialContext().doLookup(
                System.getProperty("apache.safeguard.executorservice.location",
                        "java:comp/DefaultManagedScheduledExecutorService"));

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