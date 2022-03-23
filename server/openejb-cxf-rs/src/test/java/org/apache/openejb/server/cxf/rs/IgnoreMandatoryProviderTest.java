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
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
@EnableServices("jaxrs")
@Classes(innerClassesAsBean = true)
@ContainerProperties(@ContainerProperties.Property(name = "org.apache.cxf.jaxrs.validation.ValidationExceptionMapper.activated", value = "false"))
public class IgnoreMandatoryProviderTest {
    @RandomPort("http")
    private URL base;

    @Test
    public void noHandler() throws IOException {
        final Response response = WebClient.create(base.toExternalForm()).path("openejb/ignore-mandatory").get();
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getStatus()); // 400 with the handler
        assertTrue(IO.slurp(InputStream.class.cast(response.getEntity())).contains("<h3>Internal Server Error</h3>"));
    }

    @Path("ignore-mandatory")
    public static class Thrower {
        @GET
        public String throwIt() {
            throw new ConstraintViolationException(Collections.<ConstraintViolation<?>>emptySet());
        }
    }
}
