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


import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Dependent
@RegisterRestClient
@Path("/test/api/library")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BookResourceClient {

    @GET
    String status();

    @POST
    @Path("/books")
    void addBook(Book newBook);

    @DELETE
    @Path("/books/{id}")
    void deleteBook(@PathParam("id") int id);

    @PUT
    @Path("/books")
    void updateBook(Book updatedBook);

    @GET
    @Path("/books/{id}")
    Book getBook(@PathParam("id") int id);

    @GET
    @Path("/books")
    List<Book> getListOfBooks();

}
