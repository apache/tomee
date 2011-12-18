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

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Outputs are copied because of the enhancement of OpenJPA.
 *
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
        return user;
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
