/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.superbiz.rest;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Outputs are copied because of the enhancement of OpenJPA.
 */
@Singleton
@Lock(LockType.WRITE)
@Path("/user")
@Produces(MediaType.APPLICATION_XML)
public class UserService {

    @PersistenceContext
    private EntityManager em;

    @Path("/create")
    @PUT
    public User create(@QueryParam("name") String name,
                       @QueryParam("pwd") String pwd,
                       @QueryParam("mail") String mail) {
        User user = new User();
        user.setFullname(name);
        user.setPassword(pwd);
        user.setEmail(mail);
        em.persist(user);
        return user.copy();
    }

    @Path("/list")
    @GET
    public List<User> list(@QueryParam("first") @DefaultValue("0") int first,
                           @QueryParam("max") @DefaultValue("20") int max) {
        List<User> users = new ArrayList<User>();
        List<User> found = em.createNamedQuery("user.list", User.class).setFirstResult(first).setMaxResults(max).getResultList();
        for (User u : found) {
            users.add(u.copy());
        }
        return users;
    }

    @Path("/show/{id}")
    @GET
    public User find(@PathParam("id") long id) {
        User user = em.find(User.class, id);
        if (user == null) {
            return null;
        }
        return user.copy();

    }

    @Path("/delete/{id}")
    @DELETE
    public void delete(@PathParam("id") long id) {
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
        }
    }

    @Path("/update/{id}")
    @POST
    public Response update(@PathParam("id") long id,
                           @QueryParam("name") String name,
                           @QueryParam("pwd") String pwd,
                           @QueryParam("mail") String mail) {
        User user = em.find(User.class, id);
        if (user == null) {
            throw new IllegalArgumentException("user id " + id + " not found");
        }

        user.setFullname(name);
        user.setPassword(pwd);
        user.setEmail(mail);
        em.merge(user);

        return Response.ok(user.copy()).build();
    }
}
