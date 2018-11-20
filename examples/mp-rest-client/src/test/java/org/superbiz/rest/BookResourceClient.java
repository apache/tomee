package org.superbiz.rest;


import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import javax.enterprise.context.Dependent;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
