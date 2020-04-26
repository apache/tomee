/**
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
package org.superbiz.service;

import org.superbiz.dao.PersonDAO;
import org.superbiz.domain.Person;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;

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
