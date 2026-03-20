/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.tck.fault.tolerance;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.logging.Logger;

public class MicroProfileFaultToleranceDeploymentProcessor implements ApplicationArchiveProcessor {

    private static final Logger LOGGER = Logger.getLogger(MicroProfileFaultToleranceDeploymentProcessor.class.getName());

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (!(applicationArchive instanceof ClassContainer)) {
            LOGGER.warning("Unable to add additional classes - not a class/resource container: " + applicationArchive);
            return;
        }
        ClassContainer<?> classContainer = (ClassContainer<?>) applicationArchive;

        if (applicationArchive instanceof LibraryContainer) {
            JavaArchive additionalBeanArchive = ShrinkWrap.create(JavaArchive.class);
            additionalBeanArchive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            ((LibraryContainer<?>) applicationArchive).addAsLibrary(additionalBeanArchive);
        } else {
            classContainer.addAsResource(EmptyAsset.INSTANCE, "META-INF/beans.xml");
        }

        if (!applicationArchive.contains("META-INF/beans.xml")) {
            applicationArchive.add(EmptyAsset.INSTANCE, "META-INF/beans.xml");
        }

        // Reset GlobalOpenTelemetry before each deployment so the OTel SDK is
        // re-initialized with app-specific ServiceLoader providers
        if (applicationArchive instanceof WebArchive webapp) {
            webapp.addClass(GlobalOpenTelemetryResetListener.class);
            webapp.addAsResource(
                    new StringAsset(GlobalOpenTelemetryResetListener.class.getName()),
                    "META-INF/services/jakarta.servlet.ServletContainerInitializer");

            // Register the TCK's InMemoryMetricReader via AutoConfigurationCustomizerProvider
            // so the OTel SDK picks it up during auto-configuration. Without this, the
            // InMemoryMetricReader is not discovered on some platforms (e.g., Linux CI)
            // where class isolation prevents ServiceLoader from finding it on the parent classloader.
            try {
                final Class<?> customizerProvider = Class.forName(
                    "org.eclipse.microprofile.fault.tolerance.tck.telemetryMetrics.util.PullExporterAutoConfigurationCustomizerProvider");
                webapp.addClass(customizerProvider);
                // Also include InMemoryMetricReader which the customizer references
                final Class<?> metricReader = Class.forName(
                    "org.eclipse.microprofile.fault.tolerance.tck.telemetryMetrics.util.InMemoryMetricReader");
                webapp.addClass(metricReader);
                webapp.addAsResource(
                        new StringAsset(customizerProvider.getName()),
                        "META-INF/services/io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider");
            } catch (final ClassNotFoundException e) {
                // Telemetry metrics TCK classes not on classpath — skip
                LOGGER.fine("PullExporterAutoConfigurationCustomizerProvider not found, skipping InMemoryMetricReader registration");
            }
        }
    }
}
