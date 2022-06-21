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


package org.superbiz.rest;

import org.eclipse.microprofile.metrics.annotation.Counted;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class WeatherService {

    @Path("/day/status")
    @Counted(name = "weather_day_status", absolute = true,
            displayName = "Weather Day Status",
            description = "This metric shows the weather status of the day.",
            tags = {"weather=day"})
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String dayStatus() {
        return "Hi, today is a sunny day!";
    }

    @Path("/week/status")
    @Counted(name = "weather_week_status", absolute = true)
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String weekStatus() {
        return "Hi, week will be mostly sunny!";
    }
}
