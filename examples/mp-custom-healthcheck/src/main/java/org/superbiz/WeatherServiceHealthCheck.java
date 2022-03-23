/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Custom Health Check for OpenWeatherMap API Service.
 */
@Health
@ApplicationScoped
public class WeatherServiceHealthCheck implements HealthCheck {

    @Inject WeatherGateway weatherGateway;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("OpenWeatherMap");
        try {
            WeatherApiStatus status = weatherGateway.getApiStatus();
            return responseBuilder.withData("weatherServiceApiUrl", status.getUrl())
                    .withData("weatherServiceApiVersion", status.getVersion())
                    .withData("weatherServiceMessage", status.getMessage())
                    .up().build();
        } catch (WeatherException e) {
            return responseBuilder.withData("weatherServiceErrorMessage", e.getMessage()).down().build();
        }
    }
}
