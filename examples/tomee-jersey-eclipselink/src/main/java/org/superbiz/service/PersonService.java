package org.superbiz.service;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.superbiz.dao.PersonDAO;
import org.superbiz.domain.Person;

@Path("/person")
@RequestScoped
public class PersonService {
    @Inject
    private PersonDAO dao;

    public PersonService() {
        System.out.println();
    }

    @GET
    @Path("/create/{name}")
    public Person create(@PathParam("name") final String name) {
        return dao.save(name);
    }

    @GET
    @Path("/all")
    public List<Person> list() {
        return dao.findAll();
    }
}
