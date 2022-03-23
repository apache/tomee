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
package org.superbiz.rest;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.faulttolerance.Retry;


@RequestScoped
public class WeatherGateway {

    private static Logger LOGGER = Logger.getLogger(WeatherGateway.class.getName());

    private static final String FORECAST_TIMEOUT_MESSAGE_ATTEMPTS =
            "Timeout when accessing AccuWeather Forecast Service. Max of Attempts: (%d), Attempts: (%d)";

    private static final String FORECAST_TIMEOUT_MESSAGE =
            "Timeout when accessing AccuWeather Forecast Service.";

    private static final String FORECAST_TIMEOUT_MESSAGE_DELAY =
            "Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (%d)";

    private static final String FORECAST_BUSY_MESSAGE =
            "Error AccuWeather Forecast Service is busy. Number of Attempts: (%d) \n";

    /**
     * {@link Retry#maxRetries()}
     */
    private static final int DEFAULT_MAX_RETRY = 3;

    private final AtomicInteger counterStatusOfDay = new AtomicInteger();
    private final AtomicInteger counterStatusOfWeek = new AtomicInteger();
    private final AtomicInteger counterStatusOfWeekend = new AtomicInteger();

    private Instant statusOfWeekendInstant = null;
    private Instant statusOfMonthInstant = null;
    private Instant statusOfYearInstant = null;

    @Retry(maxRetries = 3, retryOn = WeatherGatewayTimeoutException.class)
    public String statusOfDay(){
        if(counterStatusOfDay.addAndGet(1) <= DEFAULT_MAX_RETRY){
            LOGGER.warning(String.format(FORECAST_TIMEOUT_MESSAGE_ATTEMPTS, DEFAULT_MAX_RETRY, counterStatusOfDay.get()));
            throw new WeatherGatewayTimeoutException();
        }
        return "Hi, today is a sunny day!";
    }

    @Retry(maxRetries = 3, retryOn = WeatherGatewayTimeoutException.class, abortOn = WeatherGatewayBusyServiceException.class)
    public String statusOfWeek(){
        if(counterStatusOfWeek.addAndGet(1) <= DEFAULT_MAX_RETRY){
            LOGGER.warning(String.format(FORECAST_TIMEOUT_MESSAGE_ATTEMPTS, DEFAULT_MAX_RETRY, counterStatusOfWeek.get()));
            throw new WeatherGatewayTimeoutException();
        }
        LOGGER.log(Level.SEVERE, String.format(FORECAST_BUSY_MESSAGE, counterStatusOfWeek.get()));
        throw new WeatherGatewayBusyServiceException();
    }

    @Retry(retryOn = WeatherGatewayTimeoutException.class, maxRetries = 5, delay = 500, jitter = 0)
    public String statusOfWeekend() {
        if (counterStatusOfWeekend.addAndGet(1) <= 5) {
            logTimeoutMessage(statusOfWeekendInstant);
            statusOfWeekendInstant = Instant.now();
            throw new WeatherGatewayTimeoutException();
        }
        return "The Forecast for the Weekend is Scattered Showers.";
    }

    @Retry(retryOn = WeatherGatewayTimeoutException.class, delay = 500, jitter = 500)
    public String statusOfMonth() {
        if (counterStatusOfWeekend.addAndGet(1) <= DEFAULT_MAX_RETRY) {
            logTimeoutMessage(statusOfMonthInstant);
            statusOfMonthInstant = Instant.now();
            throw new WeatherGatewayTimeoutException();
        }
        return "The Forecast for the Month is Sunny for most of the days";
    }

    @Retry(maxDuration = 1000)
    public String statusOfYear(){
        if (counterStatusOfWeekend.addAndGet(1) <= 5) {
            logTimeoutMessage(statusOfYearInstant);
            statusOfYearInstant = Instant.now();
            throw new RuntimeException();
        }
        return "WeatherGateway Service Error";
    }

    private void logTimeoutMessage(Instant instant) {
        if(instant == null){
            LOGGER.warning(FORECAST_TIMEOUT_MESSAGE);
        }else{
            LOGGER.warning(String.format(FORECAST_TIMEOUT_MESSAGE_DELAY,
                    Duration.between(instant, Instant.now()).toMillis()));
        }
    }
}
