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
package org.apache.tomee.microprofile.health;

import io.smallrye.health.SmallRyeHealth;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.function.Supplier;

@Path("health")
@ApplicationScoped
public class MicroProfileHealthChecksEndpoint {

    @Inject
    private MicroProfileHealthReporter reporter;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecks() {
        return toResponse(reporter::getHealth);
    }

    @GET
    @Path("live")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLiveChecks() {
        return toResponse(reporter::getLiveness);
    }

    @GET
    @Path("ready")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadyChecks() {
        return toResponse(reporter::getReadiness);
    }


    private Response toResponse(final Supplier<SmallRyeHealth> health) {
        return Response
            .status(health.get().isDown() ? Response.Status.SERVICE_UNAVAILABLE : Response.Status.OK)
            .entity(health.get().getPayload())
            .build();
    }
}
