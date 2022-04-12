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
package org.apache.tomee.microprofile.health;

import io.smallrye.health.SmallRyeHealthReporter;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.util.AnnotationLiteral;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MPHealthCDIExtension implements Extension {

    private final String MP_HEALTH_DISABLE_DEFAULT_PROCEDURES = "mp.health.disable-default-procedures";



    // Use a single Jakarta Contexts and Dependency Injection instance to select and destroy all HealthCheck probes instances
    private Instance<Object> instance;
    private final List<HealthCheck> livenessChecks = new ArrayList<>();
    private final List<HealthCheck> readinessChecks = new ArrayList<>();
    private final List<HealthCheck> startupChecks = new ArrayList<>();
    private HealthCheck defaultReadinessCheck;
    private HealthCheck defaultStartupCheck;

    private MicroProfileHealthReporter reporter;

    public MPHealthCDIExtension() {
    }

    /**
     * Get some beans registered like the reporter
     * @param bbd
     * @param beanManager
     */
    public void observeBeforeBeanDiscovery(@Observes final BeforeBeanDiscovery bbd, final BeanManager beanManager) {
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MicroProfileHealthReporterProducer.class));
    }

    /**
     * Get Jakarta Contexts and Dependency Injection <em>instances</em> of HealthCheck and
     * add them to the {@link MicroProfileHealthReporter}.
     */
    private void afterDeploymentValidation(@Observes final AfterDeploymentValidation avd, BeanManager bm) {
        instance = bm.createInstance();

        final Instance<MicroProfileHealthReporter> reporters = instance.select(MicroProfileHealthReporter.class);
        final Optional<MicroProfileHealthReporter> microProfileHealthReporter = reporters.stream().findFirst();
        if (microProfileHealthReporter.isEmpty()) {
            throw new IllegalStateException("Most likely a bug. No reporter found in the bean manager");
        }
        this.reporter = microProfileHealthReporter.get();

        addHealthChecks(Liveness.Literal.INSTANCE, reporter::addLivenessCheck, livenessChecks);
        addHealthChecks(Readiness.Literal.INSTANCE, reporter::addReadinessCheck, readinessChecks);
        addHealthChecks(Startup.Literal.INSTANCE, reporter::addStartupCheck, startupChecks);
        reporter.setUserChecksProcessed(true);
        if (readinessChecks.isEmpty()) {
            final Config config = ConfigProvider.getConfig(MPHealthCDIExtension.class.getClassLoader());
            boolean disableDefaultprocedure = config.getOptionalValue(MP_HEALTH_DISABLE_DEFAULT_PROCEDURES, Boolean.class).orElse(false);
            if (!disableDefaultprocedure) {
                // no readiness probe are present in the deployment. register a readiness check so that the deployment is considered ready
                defaultReadinessCheck = new DefaultReadinessHealthCheck("Apache TomEE Server");
                reporter.addReadinessCheck(defaultReadinessCheck, MPHealthCDIExtension.class.getClassLoader());
            }
        }
        if (startupChecks.isEmpty()) {
            Config config = ConfigProvider.getConfig(MPHealthCDIExtension.class.getClassLoader());
            boolean disableDefaultprocedure = config.getOptionalValue(MP_HEALTH_DISABLE_DEFAULT_PROCEDURES, Boolean.class).orElse(false);
            if (!disableDefaultprocedure) {
                // no startup probes are present in the deployment. register a startup check so that the deployment is considered started
                defaultStartupCheck = new DefaultStartupHealthCheck("Apache TomEE Server");
                reporter.addStartupCheck(defaultStartupCheck, MPHealthCDIExtension.class.getClassLoader());
            }
        }
    }

    private void addHealthChecks(
        AnnotationLiteral qualifier,
        BiConsumer<HealthCheck, ClassLoader> healthFunction, List<HealthCheck> healthChecks) {
        for (HealthCheck healthCheck : instance.select(HealthCheck.class, qualifier)) {
            healthFunction.accept(healthCheck, MPHealthCDIExtension.class.getClassLoader());
            healthChecks.add(healthCheck);
        }
    }

    /**
     * Called when the deployment is undeployed.
     * <p>
     * Remove all the instances of {@link HealthCheck} from the {@link MicroProfileHealthReporter}.
     */
    public void beforeShutdown(@Observes final BeforeShutdown bs) {
        removeHealthCheck(livenessChecks, reporter::removeLivenessCheck);
        removeHealthCheck(readinessChecks, reporter::removeReadinessCheck);
        removeHealthCheck(startupChecks, reporter::removeStartupCheck);

        if (defaultReadinessCheck != null) {
            reporter.removeReadinessCheck(defaultReadinessCheck);
            defaultReadinessCheck = null;
        }

        if (defaultStartupCheck != null) {
            reporter.removeStartupCheck(defaultStartupCheck);
            defaultStartupCheck = null;
        }

        instance = null;
    }

    private void removeHealthCheck(List<HealthCheck> healthChecks,
                                   Consumer<HealthCheck> healthFunction) {
        for (HealthCheck healthCheck : healthChecks) {
            healthFunction.accept(healthCheck);
            instance.destroy(healthCheck);
        }
        healthChecks.clear();
    }

    public void vetoSmallryeHealthReporter(@Observes ProcessAnnotatedType<SmallRyeHealthReporter> pat) {
        pat.veto();
    }

    private static final class DefaultReadinessHealthCheck implements HealthCheck {

        private final String deploymentName;

        DefaultReadinessHealthCheck(String deploymentName) {
            this.deploymentName = deploymentName;
        }

        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named("ready-" + deploymentName)
                    .up()
                    .build();
        }
    }

    private static final class DefaultStartupHealthCheck implements HealthCheck {

        private final String deploymentName;

        DefaultStartupHealthCheck(String deploymentName) {
            this.deploymentName = deploymentName;
        }

        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named("started-" + deploymentName)
                .up()
                .build();
        }
    }
}