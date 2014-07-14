/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
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
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class JndiForPojoEndpointsTest {
    @Module
    @Classes(cdi = true, value = {JndiEndpoint.class})
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo")
            .addServlet(Application.class.getName(), null, "/api/*");
    }

    @Test
    public void injectionWorked() {
        assertEquals("1", WebClient.create("http://localhost:4204/foo/").path("/api/jndi").get(String.class));
    }

    @Path("jndi")
    public static class JndiEndpoint {
        @Inject
        private Validator val;

        @GET
        public int doIt() {
            return val.validate(new ToVal()).size();
        }
    }

    public static class ToVal {
        @NotNull
        private String val;

        public String getVal() {
            return val;
        }
    }
}
