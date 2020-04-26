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

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Path("/weather")
@Produces(MediaType.TEXT_PLAIN)
@RequestScoped
public class WeatherService {

    private static final Logger LOGGER = Logger.getLogger(WeatherService.class.getName());

    @GET
    @Path("/day/status")
    @CircuitBreaker(failOn = WeatherException.class)
    @Fallback(WeatherDayStatusFallbackHandler.class)
    public String dayStatus() {
        throw new WeatherException();
    }

    @GET
    @Path("/week/status")
    @Retry(maxRetries = 1)
    @Fallback(fallbackMethod = "fallbackForWeekStatus")
    public String weekStatus() {
        throw new WeatherException();
    }

    public String fallbackForWeekStatus() {
        LOGGER.log(Level.SEVERE, "Fallback was triggered due a fail");
        return "Hi, week will be mostly sunny!";
    }
}
