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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class EJBExceptionMapperTest {
    @RandomPort("http")
    private int port;

    @Test
    public void security() {
        final Response response = WebClient.create("http://localhost:" + port + "/openejb").path("/ejbsecu/rest").get();
        assertEquals(403, response.getStatus());
    }

    @Test
    public void businessError() {
        final Response response = WebClient.create("http://localhost:" + port + "/openejb").path("/ejbsecu/oops").get();
        assertEquals(234, response.getStatus());
    }

    @Provider
    public static class IllegalMapper implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(final IllegalArgumentException exception) {
            return Response.status(234).build();
        }
    }

    @Singleton
    @Lock(LockType.READ)
    @Path("ejbsecu")
    public static class RESTIsCoolOne {
        @Path("rest")
        @RolesAllowed("Something that does not exit at all")
        @GET
        public boolean secu() {
            return true;
        }

        @Path("/oops")
        @GET
        public boolean err() {
            throw new IllegalArgumentException("oops");
        }
    }
}
