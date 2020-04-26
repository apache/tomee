/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.store.rest;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.superbiz.store.entity.Order;
import org.superbiz.store.service.OrderService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderRest {

    @Inject
    private OrderService orderService;

    @Inject
    private JsonWebToken jwtPrincipal;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String status() throws Exception {
        return "running";
    }

    @GET
    @Path("/userinfo")
    @Produces(MediaType.TEXT_PLAIN)
    public String userInfo() {
        return "User: " + jwtPrincipal.getName() + " is in groups " + jwtPrincipal.getGroups();
    }

    @GET
    @Path("/orders")
    @RolesAllowed({"merchant", "buyer"})
    public List<Order> getListOfOrders() {
        return orderService.getOrders();
    }

    @GET
    @Path("/orders/{id}")
    @RolesAllowed({"merchant", "buyer"})
    public Order getOrder(@PathParam("id") int id) {
        return orderService.getOrder(id);
    }

    @POST
    @Path("/orders")
    @RolesAllowed({"merchant", "buyer"})
    public Response addOrder(Order order) {
        Order createdOrder = orderService.addOrder(order, jwtPrincipal.getName());

        return Response
                .status(Response.Status.CREATED)
                .entity(createdOrder)
                .build();
    }

    @DELETE
    @Path("/orders/{id}")
    @RolesAllowed({"merchant"})
    public Response deleteOrder(@PathParam("id") int id) {
        orderService.deleteOrder(id);

        return Response
                .status(Response.Status.NO_CONTENT)
                .build();
    }

    @PUT
    @Path("/orders")
    @RolesAllowed({"merchant", "buyer"})
    public Response updateOrder(Order order) {
        Order updatedOrder = orderService.updateOrder(order, jwtPrincipal.getName());

        return Response
                .status(Response.Status.OK)
                .entity(updatedOrder)
                .build();
    }
}
