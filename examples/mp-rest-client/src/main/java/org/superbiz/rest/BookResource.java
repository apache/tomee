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


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class BookResource {

    @Inject
    BookBean bookBean;

    @GET
    public String status() {
        return "ok";
    }

    @POST
    @Path("/books")
    public void addBook(Book newBook) {
        bookBean.addBook(newBook);
    }

    @DELETE
    @Path("/books/{id}")
    public void deleteBook(@PathParam("id") int id) {
        bookBean.deleteBook(id);
    }

    @PUT
    @Path("/books")
    public void updateBook(Book updatedBook) {
        bookBean.updateBook(updatedBook);
    }

    @GET
    @Path("/books/{id}")
    public Book getBook(@PathParam("id") int id) {
        return bookBean.getBook(id);
    }

    @GET
    @Path("/books")
    public List<Book> getListOfBooks() {
        return bookBean.getBooks();
    }

}
