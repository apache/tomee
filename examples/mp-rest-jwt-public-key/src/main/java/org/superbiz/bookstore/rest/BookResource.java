/*
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
package org.superbiz.bookstore.rest;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.superbiz.bookstore.model.Book;
import org.superbiz.bookstore.model.BooksBean;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/bookstore")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class BookResource {

    @Inject
    private BooksBean booksBean;

    @Inject
    @Claim(standard = Claims.preferred_username)
    private String userName;

    @GET
    @Path("/me")
    public String getLoggedUser() {
        return userName;
    }

    @GET
    @RolesAllowed({"manager", "reader"})
    public List<Book> getAllBooks() {
        return booksBean.getAll();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"manager", "reader"})
    public Book getBook(@PathParam("id") int id) {
        return booksBean.getBook(id);
    }

    @POST
    @RolesAllowed("manager")
    public void addBook(Book newBook) {
        booksBean.addBook(newBook);
    }

    @PUT
    @RolesAllowed("manager")
    public void updateBook(Book updatedBook) {
        booksBean.updateBook(updatedBook);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("manager")
    public void deleteBook(@PathParam("id") int id) {
        booksBean.deleteBook(id);
    }


}
