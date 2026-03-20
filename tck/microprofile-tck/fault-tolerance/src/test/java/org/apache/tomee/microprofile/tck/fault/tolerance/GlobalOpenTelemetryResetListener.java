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
package org.apache.tomee.microprofile.tck.fault.tolerance;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resets GlobalOpenTelemetry and rebuilds the OTel SDK using the webapp's
 * classloader so that ServiceLoader discovers the TCK's
 * {@code PullExporterAutoConfigurationCustomizerProvider} (which registers
 * the {@code InMemoryMetricReader} for telemetry metric tests).
 *
 * This is needed because TomEE initializes the OTel SDK once at the server
 * level with the server classloader. The TCK's ServiceLoader entries are
 * in the WAR and are not visible to the server classloader.
 */
public class GlobalOpenTelemetryResetListener implements ServletContainerInitializer {

    private static final Logger LOGGER = Logger.getLogger(GlobalOpenTelemetryResetListener.class.getName());

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        try {
            GlobalOpenTelemetry.resetForTest();

            // Rebuild the SDK using the webapp classloader so ServiceLoader picks up
            // the TCK's AutoConfigurationCustomizerProvider from the WAR
            AutoConfiguredOpenTelemetrySdk.builder()
                .setServiceClassLoader(Thread.currentThread().getContextClassLoader())
                .setResultAsGlobal()
                .build();

            LOGGER.info("Rebuilt GlobalOpenTelemetry SDK with webapp classloader");
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Failed to rebuild GlobalOpenTelemetry SDK", e);
        }
    }
}
