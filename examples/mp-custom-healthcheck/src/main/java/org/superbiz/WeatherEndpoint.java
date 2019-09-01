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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Weather Endpoint which provides:
 *      GET /weather/day/status
 */
@Path("/weather")
@RequestScoped
public class WeatherEndpoint {

    @Inject
    private WeatherGateway weatherService;

    @GET
    @Path("/day/status")
    public Response dayStatus() {
        try {
            return Response.ok().entity(weatherService.statusOfDay()).build();
        } catch (WeatherException e) {
            return Response.ok().entity(e.getMessage()).build();
        }
    }
}
