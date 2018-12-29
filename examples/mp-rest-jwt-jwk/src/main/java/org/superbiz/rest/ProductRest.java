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


import org.superbiz.entity.Product;
import org.superbiz.service.ProductService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductRest {

    @Inject
    private ProductService productService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String status() {
        return "running";
    }

    @GET
    @Path("/products")
    @RolesAllowed({"guest", "admin"})
    public List<Product> getListOfMovies() {
        return productService.getProducts();
    }

    @GET
    @Path("/products/{id}")
    @RolesAllowed({"guest", "admin"})
    public Product getMovie(@PathParam("id") int id) {
        return productService.getProduct(id);
    }

    @POST
    @Path("/products")
    @RolesAllowed({"admin"})
    public void addMovie(Product product) {
        productService.addProduct(product);
    }

    @DELETE
    @Path("/products/{id}")
    @RolesAllowed({"admin"})
    public void deleteMovie(@PathParam("id") int id) {
        productService.deleteProduct(id);
    }

    @PUT
    @Path("/products")
    @RolesAllowed({"admin"})
    public void updateMovie(Product product) {
        productService.updateProduct(product);
    }
}
