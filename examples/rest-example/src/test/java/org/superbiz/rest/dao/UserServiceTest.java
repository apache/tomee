/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.rest.dao;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.tomee.embedded.EmbeddedTomEEContainer;
import org.apache.ziplock.Archive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbiz.rest.model.User;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserServiceTest {

    private static EJBContainer container;

    @BeforeClass
    public static void start() throws IOException {
        final File webApp = Archive.archive().copyTo("WEB-INF/classes", jarLocation(UserDAO.class)).asDir();
        final Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "rest-example");
        p.setProperty(EJBContainer.PROVIDER, "tomee-embedded"); // need web feature
        p.setProperty(EJBContainer.MODULES, webApp.getAbsolutePath());
        p.setProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "-1"); // random port
        container = EJBContainer.createEJBContainer(p);
    }

    @AfterClass
    public static void stop() {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void create() throws NamingException {
        final UserDAO dao = (UserDAO) container.getContext().lookup("java:global/rest-example/UserDAO");
        final User user = dao.create("foo", "dummy", "foo@dummy.org");
        assertNotNull(dao.find(user.getId()));

        final String uri = "http://127.0.0.1:" + System.getProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT) + "/rest-example";
        final UserServiceClientAPI client = JAXRSClientFactory.create(uri, UserServiceClientAPI.class);
        final User retrievedUser = client.show(user.getId());
        assertNotNull(retrievedUser);
        assertEquals("foo", retrievedUser.getFullname());
        assertEquals("dummy", retrievedUser.getPassword());
        assertEquals("foo@dummy.org", retrievedUser.getEmail());
    }

    /**
     * a simple copy of the unique method i want to use from my service.
     * It allows to use cxf proxy to call remotely our rest service.
     * Any other way to do it is good.
     */
    @Path("/api/user")
    @Produces({"text/xml", "application/json"})
    public static interface UserServiceClientAPI {

        @Path("/show/{id}")
        @GET
        User show(@PathParam("id") long id);
    }
}
