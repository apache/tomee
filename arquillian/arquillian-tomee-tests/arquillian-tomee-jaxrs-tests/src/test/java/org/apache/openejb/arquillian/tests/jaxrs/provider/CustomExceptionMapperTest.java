/**
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

package org.apache.openejb.arquillian.tests.jaxrs.provider;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CustomExceptionMapperTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "CustomExceptionMapperTest.war")
            .addClass(CustomExceptionMapperTest.class)
            .addAsWebInfResource(new StringAsset(
                "<resources>\n" +
                "  <Service class-name=\"" + EM.class.getName() + "\" id=\"em\" />\n" +
                "</resources>\n"), "resources.xml")
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <pojo-deployment class-name=\"jaxrs-application\">\n" +
                "    <properties>\n" +
                "      cxf.jaxrs.providers = em\n" +
                "      cxf.jaxrs.skip-provider-scanning = true\n" +
                "    </properties>\n" +
                "  </pojo-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void exceptionMapper() {
        final String response = WebClient.create(base.toExternalForm())
            .path("/exception-mapper/throw").get(String.class);
        assertEquals(FooException.class.getName(), response);
    }

    @Singleton
    @Path("/exception-mapper")
    public static class RestWithExceptionMapper {
        @GET
        @Path("/throw")
        public String go() {
            throw new FooException();
        }
    }

    public static class EM implements ExceptionMapper<FooException> {
        @Override
        public Response toResponse(final FooException t) {
            return Response.ok(t.getClass().getName()).build();
        }
    }

    public static class FooException extends RuntimeException {

    }
}
