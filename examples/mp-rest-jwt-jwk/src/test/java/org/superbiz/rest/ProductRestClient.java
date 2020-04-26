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
package org.superbiz.rest;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.superbiz.entity.Product;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Dependent
@RegisterRestClient
@Path("/test/rest/store")
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
public interface ProductRestClient {

    @GET
    String status();

    @GET
    @Path("/products/{id}")
    Response getProduct(@HeaderParam("Authorization") String authHeaderValue, @PathParam("id") int id);

    @GET
    @Path("/products")
    List<Product> getProductList(@HeaderParam("Authorization") String authHeaderValue);

    @POST
    @Path("/products")
    Response addProduct(@HeaderParam("Authorization") String authHeaderValue, Product newProduct);

    @PUT
    @Path("/products")
    Response updateProduct(@HeaderParam("Authorization") String authHeaderValue, Product updatedProduct);

    @DELETE
    @Path("/products/{id}")
    Response deleteProduct(@HeaderParam("Authorization") String authHeaderValue, @PathParam("id") int id);
}
