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
package org.superbiz.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongGauge;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A JAX-RS weather service demonstrating OpenTelemetry metrics through
 * MicroProfile Telemetry. Replaces the old MicroProfile Metrics examples
 * (mp-metrics-counted, mp-metrics-gauge, mp-metrics-histogram, mp-metrics-timed,
 * mp-metrics-metered) with the OpenTelemetry API equivalents.
 *
 * <ul>
 *   <li><b>Counter</b> — counts how many times an endpoint is called</li>
 *   <li><b>Gauge</b> — reports the current temperature (an observable value)</li>
 *   <li><b>Histogram</b> — records a distribution of temperature readings</li>
 * </ul>
 *
 * OpenTelemetry does not have direct equivalents for MP Metrics {@code @Timed}
 * and {@code @Metered}. Histograms can record durations (replacing @Timed),
 * and counters can track throughput (replacing @Metered).
 */
@Path("/weather")
@ApplicationScoped
public class WeatherService {

    private static final int[] RECENT_NEW_YORK_TEMPS = {46, 45, 50, 46, 45, 27, 30, 48, 55, 54, 45, 41, 45, 43, 46};

    private final AtomicLong currentTemperature = new AtomicLong(30);

    private LongCounter requestCounter;
    private LongHistogram temperatureHistogram;

    @Inject
    private OpenTelemetry openTelemetry;

    @PostConstruct
    public void init() {
        final Meter meter = openTelemetry.getMeter("weather-service");

        // Counter: tracks total number of weather status requests
        requestCounter = meter
            .counterBuilder("weather_requests_total")
            .setDescription("Total number of weather status requests")
            .build();

        // Gauge: reports the current temperature as an observable value
        @SuppressWarnings("unused")
        final ObservableLongGauge gauge = meter
            .gaugeBuilder("weather_current_temperature")
            .setDescription("Current day temperature")
            .setUnit("celsius")
            .ofLongs()
            .buildWithCallback(measurement ->
                measurement.record(currentTemperature.get(),
                    Attributes.of(AttributeKey.stringKey("location"), "new_york")));

        // Histogram: records a distribution of temperature readings
        temperatureHistogram = meter
            .histogramBuilder("weather_temperature_distribution")
            .setDescription("Distribution of recent temperature readings")
            .setUnit("fahrenheit")
            .ofLongs()
            .build();
    }

    /**
     * Returns today's weather status. Each call increments the request counter.
     */
    @Path("/day/status")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String dayStatus() {
        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("endpoint"), "day_status"));
        return "Hi, today is a sunny day!";
    }

    /**
     * Returns the weekly weather status. Each call increments the request counter.
     */
    @Path("/week/status")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String weekStatus() {
        requestCounter.add(1, Attributes.of(AttributeKey.stringKey("endpoint"), "week_status"));
        return "Hi, week will be mostly sunny!";
    }

    /**
     * Returns the current temperature (gauge value).
     */
    @Path("/day/temperature")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String dayTemperature() {
        return currentTemperature.get() + " celsius";
    }

    /**
     * Records recent NYC temperatures into the histogram and returns a summary.
     */
    @Path("/histogram")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String temperatureHistogram() {
        for (final int temp : RECENT_NEW_YORK_TEMPS) {
            temperatureHistogram.record(temp,
                Attributes.of(AttributeKey.stringKey("location"), "new_york"));
        }
        return "Recorded " + RECENT_NEW_YORK_TEMPS.length + " temperature readings.";
    }
}
