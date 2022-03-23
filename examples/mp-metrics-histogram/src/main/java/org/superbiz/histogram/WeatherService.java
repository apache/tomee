/**
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
package org.superbiz.histogram;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.Metric;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/weather")
@ApplicationScoped
public class WeatherService {

    final static int[] RECENT_NEW_YORK_TEMPS = { 46, 45, 50, 46, 45, 27, 30, 48, 55, 54, 45, 41, 45, 43, 46 };

    @Inject
    private MetricRegistry registry;

    @Inject
    @Metric(name = "temperatures", description = "A histogram metrics example.",
        displayName = "Histogram of Recent New York Temperatures")
    private Histogram histogram;

    @Path("/histogram")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Histogram getTemperatures() {
        Metadata metadata = new Metadata("temperatures", MetricType.HISTOGRAM, "degrees F");
        metadata.setDescription("A histogram of recent New York temperatures.");
        histogram = registry.histogram(metadata);
        for(int temp : RECENT_NEW_YORK_TEMPS) {
            histogram.update(temp);
        }
        return histogram;
    }

    @Path("/histogram/status")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String histogramStatus() {
        return "Here are the most recent New York City temperatures.";
    }
}
